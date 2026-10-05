import axios from 'axios';

const api = axios.create({
    baseURL: import.meta.env.VITE_BACKEND_URL,
    withCredentials: true, // * Crucial: Allows cookies to be sent and received
    xsrfCookieName: 'XSRF-TOKEN', // * Axios looks for this cookie
    xsrfHeaderName: 'X-XSRF-TOKEN' // * Axios sends the cookie value in this header
});

export default api;