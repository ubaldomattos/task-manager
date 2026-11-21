// Utilitários de data. As datas são tratadas no fuso local do navegador e
// trafegam para a API como texto ISO (yyyy-MM-dd), sem conversão de fuso.

const LOCALE = 'pt-BR';

const monthFormat = new Intl.DateTimeFormat(LOCALE, { month: 'long', year: 'numeric' });
const longDateFormat = new Intl.DateTimeFormat(LOCALE, {
    weekday: 'long', day: 'numeric', month: 'long', year: 'numeric'
});
const dayLabelFormat = new Intl.DateTimeFormat(LOCALE, { day: 'numeric', month: 'long', year: 'numeric' });

const pad = (value) => String(value).padStart(2, '0');

export function today() {
    const now = new Date();
    return new Date(now.getFullYear(), now.getMonth(), now.getDate());
}

export function toIsoDate(date) {
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

export function toIsoMonth(year, month) {
    return `${year}-${pad(month + 1)}`;
}

export function addDays(date, days) {
    return new Date(date.getFullYear(), date.getMonth(), date.getDate() + days);
}

export function isSameDay(a, b) {
    return a.getFullYear() === b.getFullYear()
        && a.getMonth() === b.getMonth()
        && a.getDate() === b.getDate();
}

export const formatMonth = (year, month) => monthFormat.format(new Date(year, month, 1));
export const formatLongDate = (date) => longDateFormat.format(date);
export const formatDayLabel = (date) => dayLabelFormat.format(date);
