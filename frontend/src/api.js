import axios from 'axios';

// In production, the API is served from the Vercel environment variable VITE_API_BASE_URL.
// In development, we use localhost:8080.
const baseURL = import.meta.env.VITE_API_BASE_URL || 
                (import.meta.env.PROD ? window.location.origin : 'http://localhost:8080');

const api = axios.create({
  baseURL: baseURL,
  headers: {
    'Content-Type': 'application/json'
  }
});

// For multipart uploads, we'll override the header where needed
export default api;
export { baseURL };
