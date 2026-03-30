import React from 'react';
import { Button, Card, Row, Col } from 'react-bootstrap';
import { BsCheckCircleFill, BsDownload, BsFileEarmarkText } from 'react-icons/bs';
import { baseURL } from '../api';

const ResultsDashboard = ({ results, onReset }) => {
  const downloadFile = (id) => {
    window.location.href = `${baseURL}/api/download/${id}`;
  };

  return (
    <div className="results-dashboard px-4 pb-4">
      <div className="text-center mb-5">
        <BsCheckCircleFill className="text-success mb-3" style={{ fontSize: '3.5rem' }} />
        <h3 className="fw-bold text-success mb-2">Smart Conversion Successful!</h3>
        <p className="text-muted">Your optimized outputs are ready for download.</p>
      </div>

      <Row className="justify-content-center px-lg-4">
        {results?.map((res) => (
          <Col md={12} key={res.id} className="mb-3">
            <div className="result-card p-4 d-flex flex-row align-items-center justify-content-between">
              <div className="d-flex align-items-center">
                 <div className="bg-light p-3 rounded-circle me-4 shadow-sm">
                   <BsFileEarmarkText className="text-primary" style={{ fontSize: '1.8rem' }} />
                 </div>
                 <div>
                    <h5 className="mb-1 fw-bold text-dark">{res.outputFormat} Format</h5>
                    <small className="text-secondary text-truncate d-block" style={{maxWidth: '220px'}}>{res.outputFilename}</small>
                 </div>
              </div>
              <div>
                <Button variant="primary" className="btn-gradient rounded-pill d-flex align-items-center gap-2 shadow-sm" onClick={() => downloadFile(res.id)}>
                   <BsDownload /> Download
                </Button>
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
