import React, { useState, useEffect } from 'react';
import { Container, Row, Col } from 'react-bootstrap';
import Navbar from './components/SmartNavbar';
import UploadZone from './components/UploadZone';
import ResultsDashboard from './components/ResultsDashboard';
import SummarizerBox from './components/SummarizerBox';
import HistoryDashboard from './components/HistoryDashboard';
import api from './api/api';
import './styles/App.css';

/**
 * App - Main Application Component
 * 
 * State Flow:
 * 1. Initial State: UploadZone rendered. User drops or picks a file.
 * 2. Uploaded State: Server returns available target formats and fileId.
 *    User can select multiple target formats and/or generate an AI summary.
 * 3. Converting State: Executes batch conversion and displays ResultsDashboard
 *    with instant download options.
 * 4. History View: View conversion audit trail at any time via hash routing (#/history).
 */
function App() {
  const [fileData, setFileData] = useState(null);
  const [results, setResults] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [converting, setConverting] = useState(false);
  const [error, setError] = useState('');
  const [selectedFormats, setSelectedFormats] = useState([]);
  const [summarizing, setSummarizing] = useState(false);
  const [summary, setSummary] = useState(null);

  // Initialize active tab based on window.location.hash for direct URL bookmarking
  const getInitialTab = () => {
    const hash = window.location.hash;
    if (hash === '#/history') return 'history';
    return 'converter';
  };

  const [activeTab, setActiveTab] = useState(getInitialTab());

  // Listen to popstate and hashchange events for browser back/forward button navigation
  useEffect(() => {
    const handlePopState = () => {
      const hash = window.location.hash;
      if (hash === '#/history') {
        setActiveTab('history');
      } else {
        setActiveTab('converter');
      }
    };

    window.addEventListener('popstate', handlePopState);
    window.addEventListener('hashchange', handlePopState);
    return () => {
      window.removeEventListener('popstate', handlePopState);
      window.removeEventListener('hashchange', handlePopState);
    };
  }, []);

  // Update tab state and sync browser URL hash
  const handleTabChange = (tab) => {
    setActiveTab(tab);
    const path = tab === 'history' ? '#/history' : '#/';
    if (window.location.hash !== path) {
      window.location.hash = path;
    }
  };

  // Step 1: Upload source document to backend
  const handleUpload = async (files) => {
    const file = files[0];
    if (!file) return;


    setError('');
    setUploading(true);
    const formData = new FormData();
    formData.append('file', file);

    try {
      const res = await api.post('/api/upload', formData);
      setFileData(res.data);
      setResults(null);
      setSummary(null);
      setSelectedFormats([]);
    } catch (err) {
      const errorData = err.response?.data;
      const errorMsg = typeof errorData === 'object' ? JSON.stringify(errorData) : (errorData || err.message);
      setError(`Upload failed: ${errorMsg}`);
    } finally {
      setUploading(false);
    }
  };

  // Step 2: Request conversion for selected target formats
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
      const errorData = err.response?.data;
      const errorMsg = typeof errorData === 'object' ? (errorData.message || JSON.stringify(errorData)) : (errorData || err.message);
      setError(`Conversion failed: ${errorMsg || 'Please try again.'}`);
    } finally {
      setConverting(false);
    }
  };

  // Step 3: Request AI or Local summary of uploaded document
  const handleSummarize = async () => {
    if (!fileData) return;
    setSummarizing(true);
    setSummary(null);
    setError('');
    try {
      const res = await api.post(`/api/summarize/${fileData.fileId}`);
      setSummary(res.data); // Store {summary, type} object
    } catch (err) {
      const errorData = err.response?.data;
      const errorMsg = typeof errorData === 'object' ? JSON.stringify(errorData) : (errorData || err.message);
      setError(`AI Summarization failed: ${errorMsg}`);
    } finally {
      setSummarizing(false);
    }
  };

  // Reset application to initial clean state
  const handleReset = () => {
    setFileData(null);
    setResults(null);
    setConverting(false);
    setSummarizing(false);
    setSummary(null);
    setError('');
    setSelectedFormats([]);
    handleTabChange('converter');
  };

  // Toggle selection of target conversion formats
  const toggleFormat = (fmt) => {
    setSelectedFormats(prev => 
      prev.includes(fmt) ? prev.filter(f => f !== fmt) : [...prev, fmt]
    );
  };


  return (
    <div className="app-dark">
      <div className="pastel-blobs">
        <div className="blob blob-1"></div>
        <div className="blob blob-2"></div>
        <div className="blob blob-3"></div>
        <div className="blob blob-4"></div>
      </div>
      <Navbar activeTab={activeTab} onTabChange={handleTabChange} onGoHome={handleReset} />
      
      <main className="py-5">
        <Container>
          <Row className="justify-content-center">
            <Col lg={10} xl={8}>
              <div className="glass-card p-4 p-md-5">
                {activeTab === 'history' ? (
                  <HistoryDashboard />
                ) : (
                  <>
                    <div className="text-center mb-5">
                      <h1 className="display-4 fw-bold mb-3 gradient-text">SmartConvert</h1>
                      <p className="lead text-muted">A multi File converting platform</p>
                    </div>

                    {error && <div className="alert alert-danger mb-4">{error}</div>}
                    
                    {!fileData && !results && !converting && (
                      <UploadZone 
                        onUpload={handleUpload} 
                        loading={uploading} 
                        multiple={false} 
                      />
                    )}

                    {fileData && !results && !converting && (
                      <div className="animate-fade-in">
                        <div className="d-flex justify-content-between align-items-center mb-4 p-3 bg-light-op rounded-4">
                            <div className="d-flex align-items-center">
                                <div className="file-icon-sm me-3">📄</div>
                                <h5 className="mb-0">{fileData.originalName}</h5>
                            </div>
                            <button className="btn btn-sm btn-outline-danger" onClick={handleReset}>Change File</button>
                        </div>

                        <div className="mb-5">
                            <div className="d-flex justify-content-between align-items-center mb-3">
                                <h5 className="mb-0">Selected Formats:</h5>
                                {selectedFormats.length > 0 && (
                                    <button 
                                        className="btn btn-primary rounded-pill px-4 shadow"
                                        onClick={() => handleConvert(selectedFormats)}
                                    >
                                        CONVERT NOW
                                    </button>
                                )}
                            </div>
                            <div className="format-grid">
                                {fileData.availableFormats.map(fmt => (
                                    <button 
                                        key={fmt} 
                                        className={`btn-format ${selectedFormats.includes(fmt) ? 'active' : ''}`} 
                                        onClick={() => toggleFormat(fmt)}
                                    >
                                        <span className="small text-muted">{selectedFormats.includes(fmt) ? 'Selected' : 'Target'}</span>
                                        <span className="fw-bold">{fmt}</span>
                                    </button>
                                ))}
                            </div>
                        </div>

                        <SummarizerBox 
                            onSummarize={handleSummarize} 
                            loading={summarizing} 
                            result={summary} 
                        />

                        <div className="alert alert-info py-4 rounded-4 text-center">
                            <h5 className="mb-2">File Ready for Conversion</h5>
                            <p className="mb-0 text-muted">Choose your target formats above to proceed.</p>
                        </div>
                      </div>
                    )}

                    {(converting || results) && (
                      <ResultsDashboard 
                        results={results} 
                        loading={converting} 
                        onReset={handleReset}
                        originalName={fileData?.originalName}
                      />
                    )}
                  </>
                )}
              </div>
            </Col>
          </Row>
        </Container>
      </main>

      
      <footer className="py-4 text-center text-muted">
      </footer>
    </div>
  );
}

export default App;
