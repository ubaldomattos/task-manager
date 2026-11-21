// Mensagens temporárias (toasts) e diálogo de confirmação.

const DURATION_MS = { success: 3500, info: 3500, error: 6000 };

export function createNotifier(container) {
    function show(message, type) {
        const toast = document.createElement('div');
        toast.className = `toast toast--${type}`;
        toast.textContent = message;
        container.append(toast);
        setTimeout(() => toast.remove(), DURATION_MS[type]);
    }

    return {
        success: (message) => show(message, 'success'),
        info: (message) => show(message, 'info'),
        error: (message) => show(message, 'error')
    };
}

/** Abre o diálogo de confirmação e resolve com true se o usuário confirmar. */
export function createConfirm(dialog) {
    const message = dialog.querySelector('#confirm-message');

    return (text) => new Promise((resolve) => {
        message.textContent = text;
        dialog.returnValue = '';
        dialog.addEventListener('close', () => resolve(dialog.returnValue === 'confirm'), { once: true });
        dialog.showModal();
    });
}
