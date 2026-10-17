import { Button, Row, Col } from 'react-bootstrap';
import { BsCheckCircleFill, BsDownload, BsFileEarmarkText } from 'react-icons/bs';
import { baseURL } from '../api/api';

const ResultsDashboard = ({ results, loading, onReset, originalName }) => {

  const downloadFile = (id, filename) => {
    const url = `${baseURL}/api/download/${id}`;
    const link = document.createElement('a');
    link.href = url;
    if (filename) link.download = filename;
    link.target = '_blank';
    document.body.appendChild(link);
    link.click();
    link.remove();
  };

  const downloadAll = () => {
    if (!results || results.length === 0) return;
    results.forEach((res, index) => {
      setTimeout(() => {
        downloadFile(res.id, res.outputFilename);
      }, index * 400);
    });
  };

  if (loading) {
    return (
      <div className="text-center py-5 animate-fade-in">
        <div className="spinner-border text-primary mb-4" style={{ width: '3.5rem', height: '3.5rem' }} role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
        <h4 className="fw-bold text-white mb-2">Converting Your File...</h4>
        <p className="text-muted">
          Applying smart transformations to <span className="text-primary">{originalName || 'your document'}</span>. Please wait a moment.
        </p>
      </div>
    );
  }

  return (
    <div className="results-dashboard px-4 pb-4">
      <div className="text-center mb-5">
        <BsCheckCircleFill className="text-success mb-3" style={{ fontSize: '3.5rem' }} />
        <h3 className="fw-bold text-success mb-2">Smart Conversion Successful!</h3>
        <p className="text-muted">Your optimized outputs are ready for download.</p>
        {results && results.length > 1 && (
          <Button 
            variant="outline-success" 
            className="rounded-pill px-4 mt-2 shadow-sm"
            onClick={downloadAll}
          >
            <BsDownload className="me-2" /> Download All ({results.length} files)
          </Button>
        )}
      </div>

      <Row className="justify-content-center px-lg-4">
        {results?.map((res) => (
          <Col md={12} key={res.id} className="mb-4">
            <div className="result-card p-4 rounded-3 shadow-sm">
              <div className="d-flex flex-row align-items-center justify-content-between mb-4">
                <div className="d-flex align-items-center">
                   <div className="bg-light-op p-3 rounded-circle me-4 shadow-sm">
                     <BsFileEarmarkText className="text-primary" style={{ fontSize: '1.8rem' }} />
                   </div>
                   <div>
                      <h5 className="mb-1 fw-bold text-white">{res.outputFormat} Format</h5>
                      <small className="text-secondary text-truncate d-block" style={{maxWidth: '320px'}} title={res.outputFilename}>
                        {res.outputFilename}
                      </small>
                   </div>
                </div>
                <div>
                  <Button 
                    variant="primary" 
                    className="btn-gradient rounded-pill d-flex align-items-center gap-2 shadow-sm" 
                    onClick={() => downloadFile(res.id, res.outputFilename)}
                  >
                     <BsDownload /> Download
                  </Button>
                </div>
              </div>

            </div>
          </Col>
        ))}
      </Row>

      <div className="text-center mt-5">
         <button className="btn btn-outline-secondary px-5 py-2 rounded-pill fw-semibold shadow-sm" onClick={onReset}>
             Convert Another File
         </button>
      </div>
    </div>
  );
};

export default ResultsDashboard;
