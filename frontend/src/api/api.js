import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:8081/api',
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export const authApi = {
    login: (username, password) => api.post('/auth/login', { username, password }),
};

export const bookApi = {
    getAll: (params) => api.get('/books', { params }),
    add: (book) => api.post('/books', book),
    update: (id, book) => api.put(`/books/${id}`, book),
    delete: (id) => api.delete(`/books/${id}`),
};

export const memberApi = {
    getMe: () => api.get('/members/me'),
    getProfile: (id) => api.get(`/members/${id}`),
    getHistory: (id) => api.get(`/members/${id}/history`),
    getFines: (id) => api.get(`/fines/${id}`),
};

export const borrowApi = {
    borrow: (bookId, memberId) => api.post(`/borrow/${bookId}?memberId=${memberId}`),
    return: (bookId, memberId) => api.post(`/return/${bookId}?memberId=${memberId}`),
};

export default api;
