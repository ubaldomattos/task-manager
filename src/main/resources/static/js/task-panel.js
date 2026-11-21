// Painel com as tarefas do dia selecionado: listagem, cadastro, edição,
// exclusão e marcação de conclusão.

import { formatLongDate, toIsoDate } from './dates.js';

const ICON_EDIT = '<svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M4 20h4L19 9l-4-4L4 16v4z"/><path d="M14 6l4 4"/></svg>';
const ICON_DELETE = '<svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M4 7h16M10 11v6M14 11v6M6 7l1 13h10l1-13M9 7V4h6v3"/></svg>';

/** Pendentes primeiro; em cada grupo, na ordem de criação (mesma regra da API). */
const byPendingThenCreation = (a, b) => Number(a.completed) - Number(b.completed) || a.id - b.id;

export class TaskPanel {
    constructor(elements, { api, notify, confirm, onChange }) {
        this.el = elements;
        this.api = api;
        this.notify = notify;
        this.confirm = confirm;
        this.onChange = onChange;
        this.date = null;
        this.tasks = [];
        this.loadId = 0;
        this.editingId = null;

        this.el.form.addEventListener('submit', (event) => this.handleCreate(event));
        this.el.list.addEventListener('change', (event) => this.handleToggle(event));
        this.el.list.addEventListener('click', (event) => this.handleAction(event));
        this.el.editForm.addEventListener('submit', (event) => this.handleEdit(event));
        this.el.editDialog.querySelector('[data-close]')
            .addEventListener('click', () => this.el.editDialog.close());
        this.el.titleInput.addEventListener('input', () => this.el.titleInput.removeAttribute('aria-invalid'));
        this.el.editTitle.addEventListener('input', () => this.el.editTitle.removeAttribute('aria-invalid'));
    }

    async show(date) {
        this.date = date;
        this.el.title.textContent = formatLongDate(date);
        const loadId = ++this.loadId;
        this.el.list.setAttribute('aria-busy', 'true');

        try {
            const tasks = await this.api.listByDate(toIsoDate(date));
            if (loadId === this.loadId) {
                this.tasks = tasks;
                this.render();
            }
        } catch (error) {
            if (loadId === this.loadId) {
                this.tasks = [];
                this.render();
                this.notify.error(error.message);
            }
        } finally {
            if (loadId === this.loadId) {
                this.el.list.removeAttribute('aria-busy');
            }
        }
    }

    render() {
        this.tasks.sort(byPendingThenCreation);
        this.el.list.replaceChildren(...this.tasks.map((task) => this.createItem(task)));
        this.el.empty.hidden = this.tasks.length > 0;
        this.el.counter.textContent = this.describeCount();
    }

    describeCount() {
        const total = this.tasks.length;
        if (total === 0) {
            return 'Sem tarefas';
        }
        const done = this.tasks.filter((task) => task.completed).length;
        const tasksLabel = total === 1 ? '1 tarefa' : `${total} tarefas`;
        const doneLabel = done === 1 ? '1 concluída' : `${done} concluídas`;
        return `${tasksLabel} · ${doneLabel}`;
    }

    createItem(task) {
        const item = document.createElement('li');
        item.className = 'task';
        item.classList.toggle('task--completed', task.completed);
        item.dataset.id = String(task.id);

        const checkboxId = `task-${task.id}`;
        const checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.id = checkboxId;
        checkbox.className = 'task__check';
        checkbox.checked = task.completed;

        const body = document.createElement('div');
        body.className = 'task__body';
        const title = document.createElement('label');
        title.className = 'task__title';
        title.htmlFor = checkboxId;
        title.textContent = task.title;
        body.append(title);
        if (task.description) {
            const description = document.createElement('p');
            description.className = 'task__description';
            description.textContent = task.description;
            body.append(description);
        }

        const actions = document.createElement('div');
        actions.className = 'task__actions';
        actions.append(
            createIconButton('edit', `Editar tarefa: ${task.title}`, ICON_EDIT),
            createIconButton('delete', `Excluir tarefa: ${task.title}`, ICON_DELETE, 'icon-button--danger'));

        item.append(checkbox, body, actions);
        return item;
    }

    async handleCreate(event) {
        event.preventDefault();
        const title = this.el.titleInput.value.trim();
        if (!title) {
            this.el.titleInput.setAttribute('aria-invalid', 'true');
            this.el.titleInput.focus();
            this.notify.error('Informe o título da tarefa.');
            return;
        }

        const submit = this.el.form.querySelector('[type="submit"]');
        submit.disabled = true;
        try {
            const created = await this.api.create({
                title,
                description: this.el.descriptionInput.value,
                scheduledDate: toIsoDate(this.date)
            });
            if (created.scheduledDate === toIsoDate(this.date)) {
                this.tasks.push(created);
                this.render();
            }
            this.el.form.reset();
            this.el.titleInput.focus();
            this.notify.success('Tarefa adicionada.');
            this.onChange();
        } catch (error) {
            this.notify.error(error.message);
        } finally {
            submit.disabled = false;
        }
    }

    async handleToggle(event) {
        const checkbox = event.target;
        if (!checkbox.classList.contains('task__check')) {
            return;
        }
        const task = this.findTask(checkbox);
        const completed = checkbox.checked;
        checkbox.disabled = true;

        try {
            const updated = await this.api.setCompleted(task.id, completed);
            this.replaceTask(updated);
            this.notify.success(completed ? 'Tarefa concluída.' : 'Tarefa marcada como pendente.');
            this.onChange();
            this.el.list.querySelector(`#task-${updated.id}`)?.focus();
        } catch (error) {
            checkbox.checked = !completed;
            checkbox.disabled = false;
            this.notify.error(error.message);
        }
    }

    handleAction(event) {
        const button = event.target.closest('[data-action]');
        if (!button) {
            return;
        }
        const task = this.findTask(button);
        if (button.dataset.action === 'edit') {
            this.openEditor(task);
        } else if (button.dataset.action === 'delete') {
            this.deleteTask(task);
        }
    }

    openEditor(task) {
        this.editingId = task.id;
        this.el.editTitle.value = task.title;
        this.el.editDescription.value = task.description ?? '';
        this.el.editTitle.removeAttribute('aria-invalid');
        this.el.editDialog.showModal();
        this.el.editTitle.focus();
    }

    async handleEdit(event) {
        event.preventDefault();
        const title = this.el.editTitle.value.trim();
        if (!title) {
            this.el.editTitle.setAttribute('aria-invalid', 'true');
            this.el.editTitle.focus();
            this.notify.error('Informe o título da tarefa.');
            return;
        }

        const submit = this.el.editForm.querySelector('[type="submit"]');
        submit.disabled = true;
        try {
            const updated = await this.api.update(this.editingId, {
                title,
                description: this.el.editDescription.value
            });
            this.replaceTask(updated);
            this.el.editDialog.close();
            this.notify.success('Tarefa atualizada.');
        } catch (error) {
            this.notify.error(error.message);
        } finally {
            submit.disabled = false;
        }
    }

    async deleteTask(task) {
        const confirmed = await this.confirm(`Excluir a tarefa “${task.title}”? Esta ação não pode ser desfeita.`);
        if (!confirmed) {
            return;
        }
        try {
            await this.api.remove(task.id);
            this.tasks = this.tasks.filter((item) => item.id !== task.id);
            this.render();
            this.notify.success('Tarefa excluída.');
            this.onChange();
        } catch (error) {
            this.notify.error(error.message);
        }
    }

    findTask(element) {
        const id = Number(element.closest('.task').dataset.id);
        return this.tasks.find((task) => task.id === id);
    }

    replaceTask(updated) {
        this.tasks = this.tasks.map((task) => (task.id === updated.id ? updated : task));
        this.render();
    }
}

function createIconButton(action, label, icon, extraClass) {
    const button = document.createElement('button');
    button.type = 'button';
    button.className = extraClass ? `icon-button ${extraClass}` : 'icon-button';
    button.dataset.action = action;
    button.setAttribute('aria-label', label);
    button.title = label;
    button.innerHTML = icon; // SVG fixo, sem dados do usuário.
    return button;
}
