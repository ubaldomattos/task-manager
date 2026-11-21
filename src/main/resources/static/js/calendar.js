// Calendário mensal: desenha os dias, destaca hoje e o dia selecionado,
// marca os dias com tarefas e permite navegar por mouse, toque e teclado.

import { addDays, formatDayLabel, formatMonth, isSameDay, toIsoDate, today } from './dates.js';

const DAYS_IN_GRID = 42; // 6 semanas, para a altura do calendário não variar entre meses.
const KEY_OFFSETS = { ArrowLeft: -1, ArrowRight: 1, ArrowUp: -7, ArrowDown: 7 };

export class Calendar {
    /**
     * @param {object} elements  título do mês, grade e botões de navegação
     * @param {object} callbacks onSelect(date) e onMonthChange(year, month)
     */
    constructor({ title, grid, prevButton, nextButton, todayButton }, { onSelect, onMonthChange }) {
        this.title = title;
        this.grid = grid;
        this.onSelect = onSelect;
        this.onMonthChange = onMonthChange;
        this.summary = new Map();

        this.selected = today();
        this.year = this.selected.getFullYear();
        this.month = this.selected.getMonth();

        prevButton.addEventListener('click', () => this.showMonth(this.year, this.month - 1));
        nextButton.addEventListener('click', () => this.showMonth(this.year, this.month + 1));
        todayButton.addEventListener('click', () => this.select(today()));
        grid.addEventListener('click', (event) => this.handleClick(event));
        grid.addEventListener('keydown', (event) => this.handleKeydown(event));
    }

    start() {
        this.render();
        this.onMonthChange(this.year, this.month);
        this.onSelect(this.selected);
    }

    /** Exibe um mês sem alterar o dia selecionado. Aceita mês fora de 0..11. */
    showMonth(year, month) {
        const first = new Date(year, month, 1);
        this.year = first.getFullYear();
        this.month = first.getMonth();
        this.summary = new Map();
        this.render();
        this.onMonthChange(this.year, this.month);
    }

    select(date, { focus = false } = {}) {
        this.selected = date;
        if (date.getFullYear() !== this.year || date.getMonth() !== this.month) {
            this.showMonth(date.getFullYear(), date.getMonth());
        } else {
            this.render();
        }
        if (focus) {
            this.grid.querySelector(`[data-date="${toIsoDate(date)}"]`)?.focus();
        }
        this.onSelect(date);
    }

    /** Recebe a lista devolvida por /api/tasks/summary para o mês exibido. */
    setSummary(days) {
        this.summary = new Map(days.map((day) => [day.date, day]));
        this.render();
    }

    render() {
        const focusedDate = this.grid.contains(document.activeElement)
            ? document.activeElement.dataset.date
            : null;
        this.title.textContent = formatMonth(this.year, this.month);

        const first = new Date(this.year, this.month, 1);
        const start = addDays(first, -first.getDay());
        const current = today();
        const cells = [];

        for (let i = 0; i < DAYS_IN_GRID; i++) {
            cells.push(this.createDay(addDays(start, i), current));
        }
        // Apenas um dia entra na ordem de tabulação; as setas movem entre os dias.
        if (!cells.some((cell) => cell.tabIndex === 0)) {
            cells[first.getDay()].tabIndex = 0;
        }
        this.grid.replaceChildren(...cells);
        if (focusedDate) {
            this.grid.querySelector(`[data-date="${focusedDate}"]`)?.focus();
        }
    }

    createDay(date, current) {
        const iso = toIsoDate(date);
        const info = this.summary.get(iso);
        const outside = date.getMonth() !== this.month;
        const selected = isSameDay(date, this.selected);
        const isToday = isSameDay(date, current);

        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'day';
        button.dataset.date = iso;
        button.textContent = String(date.getDate());
        button.tabIndex = selected ? 0 : -1;
        button.setAttribute('aria-pressed', String(selected));
        button.setAttribute('aria-label', this.describeDay(date, info, isToday));

        button.classList.toggle('day--outside', outside);
        button.classList.toggle('day--today', isToday);
        button.classList.toggle('day--selected', selected);
        if (info && info.total > 0) {
            button.classList.add('day--has-tasks');
            button.classList.toggle('day--all-done', info.pending === 0);
        }
        if (isToday) {
            button.setAttribute('aria-current', 'date');
        }
        return button;
    }

    describeDay(date, info, isToday) {
        const parts = [formatDayLabel(date)];
        if (isToday) {
            parts.push('hoje');
        }
        if (info && info.total > 0) {
            parts.push(info.pending === 0
                ? `${info.total} ${info.total === 1 ? 'tarefa concluída' : 'tarefas concluídas'}`
                : `${info.pending} ${info.pending === 1 ? 'tarefa pendente' : 'tarefas pendentes'}`);
        }
        return parts.join(', ');
    }

    handleClick(event) {
        const button = event.target.closest('.day');
        if (button) {
            this.select(parseIso(button.dataset.date), { focus: true });
        }
    }

    handleKeydown(event) {
        const offset = KEY_OFFSETS[event.key];
        const button = event.target.closest('.day');
        if (offset === undefined || !button) {
            return;
        }
        event.preventDefault();
        this.select(addDays(parseIso(button.dataset.date), offset), { focus: true });
    }
}

function parseIso(iso) {
    const [year, month, day] = iso.split('-').map(Number);
    return new Date(year, month - 1, day);
}
