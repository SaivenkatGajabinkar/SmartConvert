import axios from 'axios';

// Support VITE_API_URL if configured, or default to production Render backend
const rawBaseURL = import.meta.env.VITE_API_URL || 'https://smartconvert-backend-ge44.onrender.com';
const baseURL = rawBaseURL ? rawBaseURL.trim().replace(/\/+$/, '') : '';

const api = axios.create({
  baseURL: baseURL,
  timeout: 60000, // 60s timeout to accommodate Render cold starts
});

export default api;
export { baseURL };

