// Alternância entre tema claro e escuro, com a escolha salva no navegador.
// O tema inicial é aplicado por um script no <head> de index.html.

const STORAGE_KEY = 'taskmanager.theme';

function currentTheme() {
    return document.documentElement.dataset.theme === 'dark' ? 'dark' : 'light';
}

function saveTheme(theme) {
    try {
        localStorage.setItem(STORAGE_KEY, theme);
    } catch {
        // Armazenamento indisponível (por exemplo, modo privado): o tema vale só para esta sessão.
    }
}

function updateButton(button) {
    const dark = currentTheme() === 'dark';
    const label = dark ? 'Ativar modo claro' : 'Ativar modo escuro';
    button.setAttribute('aria-label', label);
    button.title = label;
}

export function initThemeToggle(button) {
    updateButton(button);
    button.addEventListener('click', () => {
        const next = currentTheme() === 'dark' ? 'light' : 'dark';
        document.documentElement.dataset.theme = next;
        saveTheme(next);
        updateButton(button);
    });
}
