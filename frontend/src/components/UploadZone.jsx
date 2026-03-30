import React, { useState } from 'react';
import { BsCloudUpload } from 'react-icons/bs';

const UploadZone = ({ onUpload }) => {
  const [dragActive, setDragActive] = useState(false);
  const [error, setError] = useState('');

  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === "dragenter" || e.type === "dragover") {
      setDragActive(true);
    } else if (e.type === "dragleave") {
      setDragActive(false);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFile(e.dataTransfer.files[0]);
    }
  };

  const handleChange = (e) => {
    e.preventDefault();
    if (e.target.files && e.target.files[0]) {
      handleFile(e.target.files[0]);
    }
  };

  const handleFile = (file) => {
    if (file.size > 10 * 1024 * 1024) {
      setError("File size exceeds 10MB limit.");
      return;
    }
    setError('');
    onUpload(file);
  };

  return (
    <div 
        className={`upload-zone ${dragActive ? "drag-active" : ""}`}
        onDragEnter={handleDrag}
        onDragLeave={handleDrag}
        onDragOver={handleDrag}
        onDrop={handleDrop}
    >
      <input 
        type="file" 
        multiple={false} 
        onChange={handleChange} 
        style={{ display: "none" }} 
        id="file-upload" 
      />
      <label htmlFor="file-upload" style={{ cursor: 'pointer', display: 'block' }}>
        <BsCloudUpload className="upload-icon bounce mb-3" />
        <h3 className="mb-2 fw-semibold">Click or drag & drop to upload</h3>
        <p className="text-muted">Supports PDF, Images, Excel, Word, CSV (Max 10MB)</p>
      </label>
      {error && <div className="text-danger mt-2 fw-semibold">{error}</div>}
    </div>
  );
};

export default UploadZone;
