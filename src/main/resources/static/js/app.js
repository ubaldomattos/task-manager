// Ponto de entrada da interface: liga calendário, painel de tarefas, tema e API.

import { taskApi } from './api.js';
import { Calendar } from './calendar.js';
import { toIsoMonth } from './dates.js';
import { createConfirm, createNotifier } from './feedback.js';
import { TaskPanel } from './task-panel.js';
import { initThemeToggle } from './theme.js';

const byId = (id) => document.getElementById(id);

const notify = createNotifier(byId('toasts'));
initThemeToggle(byId('theme-toggle'));

let summaryRequest = 0;

async function refreshSummary() {
    const requestId = ++summaryRequest;
    const { year, month } = calendar;
    try {
        const days = await taskApi.summarizeMonth(toIsoMonth(year, month));
        // Ignora respostas de um mês que o usuário já deixou de exibir.
        if (requestId === summaryRequest) {
            calendar.setSummary(days);
        }
    } catch (error) {
        notify.error(error.message);
    }
}

const panel = new TaskPanel({
    title: byId('selected-date'),
    counter: byId('task-counter'),
    form: byId('task-form'),
    titleInput: byId('task-title'),
    descriptionInput: byId('task-description'),
    list: byId('task-list'),
    empty: byId('task-empty'),
    editDialog: byId('edit-dialog'),
    editForm: byId('edit-form'),
    editTitle: byId('edit-title'),
    editDescription: byId('edit-description')
}, {
    api: taskApi,
    notify,
    confirm: createConfirm(byId('confirm-dialog')),
    onChange: refreshSummary
});

const calendar = new Calendar({
    title: byId('month-title'),
    grid: byId('calendar-grid'),
    prevButton: byId('prev-month'),
    nextButton: byId('next-month'),
    todayButton: byId('today-button')
}, {
    onSelect: (date) => panel.show(date),
    onMonthChange: refreshSummary
});

calendar.start();
