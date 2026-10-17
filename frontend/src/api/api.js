import axios from 'axios';

/**
 * Axios HTTP Client Configuration
 * 
 * Key Features:
 * 1. Base URL Resolution:
 *    - Reads import.meta.env.VITE_API_URL when configured locally or in Vercel.
 *    - Defaults to the deployed Render backend URL for seamless out-of-the-box operation.
 *    - Strips whitespace and trailing slashes to guarantee clean API request paths.
 * 
 * 2. Extended Timeout:
 *    - Configured with a 60,000ms (60s) timeout to accommodate Render free-tier instance cold starts.
 */
const rawBaseURL = import.meta.env.VITE_API_URL || 'https://smartconvert-backend-ge44.onrender.com';
const baseURL = rawBaseURL ? rawBaseURL.trim().replace(/\/+$/, '') : '';

const api = axios.create({
  baseURL: baseURL,
  timeout: 60000,
});

export default api;
export { baseURL };


