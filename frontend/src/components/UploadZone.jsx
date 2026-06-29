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
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      handleFiles(Array.from(e.dataTransfer.files));
    }
  };

  const handleChange = (e) => {
    e.preventDefault();
    if (e.target.files && e.target.files.length > 0) {
      handleFiles(Array.from(e.target.files));
    }
  };

  const handleFiles = (files) => {
    const file = files[0];
    if (file.size <= 50 * 1024 * 1024) {
        setError('');
        onUpload([file]);
    } else {
        setError("File exceeds 50MB limit.");
    }
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
        <BsCloudUpload className="upload-zone-icon mb-3" />
        <h3 className="mb-2 fw-semibold">Click or drag & drop to upload</h3>
        <p className="text-muted">
            Supports PDF, Images, Excel, Word (Max 50MB)
        </p>
      </label>
      {error && <div className="text-danger mt-2 fw-semibold">{error}</div>}
    </div>
  );
};

export default UploadZone;
