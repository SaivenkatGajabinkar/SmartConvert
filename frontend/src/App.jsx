import React, { useState } from 'react';
import { Container, Row, Col, Spinner } from 'react-bootstrap';
import UploadZone from './components/UploadZone';
import ResultsDashboard from './components/ResultsDashboard';
import SummarizerBox from './components/SummarizerBox';
import api from './api';
import { BsMagic, BsStars } from 'react-icons/bs';

function App() {
  const [fileData, setFileData] = useState(null);
  const [converting, setConverting] = useState(false);
  const [summarizing, setSummarizing] = useState(false);
  const [summary, setSummary] = useState(null);
  const [results, setResults] = useState(null);
  const [error, setError] = useState('');

  const handleUpload = async (file) => {
    setError('');
    const formData = new FormData();
    formData.append('file', file);

    try {
      const res = await api.post('/api/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      setFileData(res.data);
      setResults(null);
    } catch (err) {
      console.error('Upload error:', err);
      const errorData = err.response?.data;
      const errorMessage = typeof errorData === 'object' 
        ? (errorData.message || JSON.stringify(errorData)) 
        : (errorData || 'Failed to upload file');
      setError(errorMessage);
    }
  };

  const handleConvert = async (formats) => {
    if (!fileData) return;
    setConverting(true);
    setError('');
    
    try {
      const res = await api.post(`/api/convert/${fileData.fileId}`, {
        formats: formats
      });
      setResults(res.data);
    } catch (err) {
      setError('Conversion failed. Please try again.');
    } finally {
      setConverting(false);
    }
  };

  const handleSummarize = async () => {
    if (!fileData) return;
    setSummarizing(true);
    setSummary(null);
    setError('');
    try {
      const res = await api.post(`/api/summarize/${fileData.fileId}`);
      setSummary(res.data.summary);
    } catch (err) {
      setError('AI Summarization failed. Please check your API key.');
    } finally {
      setSummarizing(false);
    }
  };

  const handleReset = () => {
    setFileData(null);
    setResults(null);
    setSummary(null);
    setConverting(false);
    setSummarizing(false);
    setError('');
  };

  return (
    <div className="app-container">
      <Container>
        <Row className="justify-content-center mb-5 text-center">
          <Col md={8}>
            <h1 className="display-4 fw-bold mb-3">
              <span className="brand-text">SmartFile Converter.com</span>
            </h1>
            <p className="lead text-muted">The intelligent, multi-format file conversion engine.</p>
          </Col>
        </Row>

        <Row className="justify-content-center">
          <Col md={10} lg={8}>
            <div className="glass-card">
              {error && <div className="alert alert-danger">{error}</div>}
              
              {!fileData && !results && (
                <UploadZone onUpload={handleUpload} />
              )}

              {fileData && !results && !converting && (
                 <div className="text-center">
                    <h4 className="mb-4">File Ready: <strong>{fileData.originalName}</strong></h4>
                    {fileData.availableFormats && fileData.availableFormats.length > 0 ? (
                      <>
                        <h5 className="mb-3 text-secondary">Recommended output: <span className="badge bg-success">{fileData.recommendedFormat}</span></h5>
                        
                        <div className="format-grid">
                           {fileData.availableFormats.map(fmt => (
                             <button key={fmt} className="btn-format" onClick={() => handleConvert([fmt])}>
                               <span style={{fontSize: '0.8rem', color: 'var(--text-muted)'}}>Convert to</span>
                               <span style={{fontSize: '1.2rem'}}>{fmt}</span>
                             </button>
                           ))}
                        </div>

                        <div className="mt-5 d-flex flex-column flex-md-row justify-content-center gap-3 align-items-center border-top pt-4">
                            <button className="btn btn-outline-secondary rounded-pill px-4 shadow-sm" onClick={() => handleConvert(fileData.availableFormats)}>
                                Convert to All Formats
                            </button>
                            <button 
                              className="btn btn-primary rounded-pill px-4 d-flex align-items-center gap-2 shadow-sm" 
                              onClick={handleSummarize}
                              disabled={summarizing}
                              style={{background: 'var(--primary)', border: 'none'}}
                            >
                              <BsStars /> {summarizing ? 'Analyzing...' : 'AI Summarize'}
                            </button>
                        </div>
                        
                        <div className="mt-4">
                           <SummarizerBox summary={summary} loading={summarizing} />
                        </div>
                      </>
                    ) : (
                      <div className="alert alert-warning">No conversion formats supported for this file type.</div>
                    )}
                    
                    <button className="btn btn-link text-muted mt-3" onClick={handleReset}>Start over</button>
                 </div>
              )}

              {converting && (
                <div className="text-center py-5">
                  <Spinner animation="border" variant="primary" style={{ width: '3rem', height: '3rem' }} />
                  <h4 className="mt-4 text-primary">Converting your file...</h4>
                  <p className="text-muted">Applying smart transformations. Please wait.</p>
                </div>
              )}

              {results && !converting && (
                <ResultsDashboard results={results} onReset={handleReset} />
              )}
            </div>
          </Col>
        </Row>
      </Container>
    </div>
  );
}

export default App;
