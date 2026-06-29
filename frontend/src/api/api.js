import axios from 'axios';

// In local development, the vite proxy handles /api requests.
const baseURL = ' https://smartconvert-backend-ge44.onrender.com';

const api = axios.create({
  baseURL: baseURL
});

// For multipart uploads, we'll override the header where needed
export default api;
export { baseURL };
