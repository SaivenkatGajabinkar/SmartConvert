import axios from 'axios';

// In production, the API is served from the same host as the frontend.
// In development, we use localhost:8080.
const baseURL = import.meta.env.PROD 
  ? window.location.origin 
  : 'http://localhost:8080';

const api = axios.create({
  baseURL: baseURL,
  headers: {
    'Content-Type': 'application/json'
  }
});

// For multipart uploads, we'll override the header where needed
export default api;
export { baseURL };
