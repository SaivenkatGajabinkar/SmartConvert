import React, { useState } from 'react';
import { BsCloudUpload } from 'react-icons/bs';

/**
 * UploadZone - Interactive Drag-and-Drop / File Picker Component
 * 
 * Features:
 * - HTML5 Drag and Drop event handlers (dragenter, dragover, dragleave, drop).
 * - Client-side validation enforcing the 50MB file size limit.
 * - Loading indicator with informative messaging during upload analysis.
 */
const UploadZone = ({ onUpload, loading }) => {
  const [dragActive, setDragActive] = useState(false);
  const [error, setError] = useState('');

  // Handle visual drag state transitions
  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (loading) return;
    if (e.type === "dragenter" || e.type === "dragover") {
      setDragActive(true);
    } else if (e.type === "dragleave") {
      setDragActive(false);
    }
  };

  // Handle files dropped directly into the zone
  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    if (loading) return;
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      handleFiles(Array.from(e.dataTransfer.files));
    }
  };

  // Handle files selected via file dialog
  const handleChange = (e) => {
    e.preventDefault();
    if (loading) return;
    if (e.target.files && e.target.files.length > 0) {
      handleFiles(Array.from(e.target.files));
    }
  };

  // Validate file size and invoke parent upload callback
  const handleFiles = (files) => {
    const file = files[0];
    if (!file) return;
    if (file.size <= 50 * 1024 * 1024) {
        setError('');
        onUpload([file]);
    } else {
        setError("File exceeds 50MB limit.");
    }
  };


  return (
    <div 
        className={`upload-zone ${dragActive ? "drag-active" : ""} ${loading ? "opacity-75" : ""}`}
        onDragEnter={handleDrag}
        onDragLeave={handleDrag}
        onDragOver={handleDrag}
        onDrop={handleDrop}
    >
      <input 
        type="file" 
        multiple={false} 
        onChange={handleChange} 
        disabled={loading}
        style={{ display: "none" }} 
        id="file-upload" 
      />
      <label htmlFor={loading ? "" : "file-upload"} style={{ cursor: loading ? 'wait' : 'pointer', display: 'block' }}>
        {loading ? (
          <div className="py-4">
            <div className="spinner-border text-primary mb-3" style={{ width: '3rem', height: '3rem' }} role="status">
              <span className="visually-hidden">Loading...</span>
            </div>
            <h4 className="mb-2 fw-semibold text-white">Uploading & Analyzing...</h4>
            <p className="text-muted small mb-0">
              Sending file to server. If the cloud server is waking up from sleep, this may take ~20-30 seconds.
            </p>
          </div>
        ) : (
          <>
            <BsCloudUpload className="upload-zone-icon mb-3" />
            <h3 className="mb-2 fw-semibold">Click or drag & drop to upload</h3>
            <p className="text-muted">
                Supports PDF, Images, Excel, Word, CSV, Markdown & Text (Max 50MB)
            </p>
          </>
        )}
      </label>
      {error && <div className="text-danger mt-2 fw-semibold">{error}</div>}
    </div>
  );
};

export default UploadZone;
