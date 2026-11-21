// Cliente da API REST de tarefas.

const BASE_URL = '/api/tasks';

export class ApiError extends Error {
    constructor(message, fieldErrors = {}) {
        super(message);
        this.name = 'ApiError';
        this.fieldErrors = fieldErrors;
    }
}

async function request(url, { method = 'GET', body } = {}) {
    const headers = { Accept: 'application/json' };
    if (body !== undefined) {
        headers['Content-Type'] = 'application/json';
    }

    let response;
    try {
        response = await fetch(url, {
            method,
            headers,
            body: body === undefined ? undefined : JSON.stringify(body)
        });
    } catch {
        throw new ApiError('Não foi possível conectar ao servidor. Verifique se a aplicação está em execução.');
    }

    if (response.status === 204) {
        return null;
    }

    const data = await response.json().catch(() => null);
    if (!response.ok) {
        const message = data?.detail || `Não foi possível concluir a operação (erro ${response.status}).`;
        throw new ApiError(message, data?.errors ?? {});
    }
    return data;
}

export const taskApi = {
    listByDate: (isoDate) => request(`${BASE_URL}?date=${encodeURIComponent(isoDate)}`),
    summarizeMonth: (isoMonth) => request(`${BASE_URL}/summary?month=${encodeURIComponent(isoMonth)}`),
    create: (task) => request(BASE_URL, { method: 'POST', body: task }),
    update: (id, task) => request(`${BASE_URL}/${id}`, { method: 'PUT', body: task }),
    setCompleted: (id, completed) => request(`${BASE_URL}/${id}/completion`, { method: 'PATCH', body: { completed } }),
    remove: (id) => request(`${BASE_URL}/${id}`, { method: 'DELETE' })
};
