# SmartConvert Frontend

The client-side interface for **SmartConvert**, a modern multi-file conversion and AI document summarization platform.

## Tech Stack
- **Framework:** React 19 + Vite
- **Styling:** Vanilla CSS design system with dark glassmorphism (`index.css`) + React Bootstrap
- **Icons:** React Icons (`react-icons/bs`)
- **HTTP Client:** Axios (`api.js`)

## Project Structure
- `src/components/SmartNavbar.jsx`: Glassmorphic navigation bar with Home and History navigation.
- `src/components/UploadZone.jsx`: Drag-and-drop file upload zone with 50MB validation.
- `src/components/ResultsDashboard.jsx`: Converted file cards, single download, and batch download actions.
- `src/components/SummarizerBox.jsx`: AI document summarizer (Google Gemini) and offline fallback display.
- `src/components/HistoryDashboard.jsx`: Real-time audit trail and table of recent conversions.
- `src/api/api.js`: Centralized Axios instance with automatic Render backend fallback and timeout handling.

## Available Scripts
- `npm run dev`: Starts the local development server (http://localhost:5173).
- `npm run build`: Bundles the production build into the `dist/` directory.
- `npm run lint`: Runs ESLint to verify code quality.
- `npm run preview`: Previews the production build locally.

