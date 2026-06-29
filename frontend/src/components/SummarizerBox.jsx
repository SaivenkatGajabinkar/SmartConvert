import React from 'react';
import { Button, Spinner, Badge } from 'react-bootstrap';
import { BsStars, BsFileTextFill, BsShieldCheck } from 'react-icons/bs';

const SummarizerBox = ({ onSummarize, loading, result }) => {
  const isLocal = result?.type && result.type.includes("Local");

  return (
    <div className="summary-card-container p-4 mb-4">
      <div className="d-flex align-items-center justify-content-between mb-3">
        <div className="d-flex align-items-center gap-2">
          <div className="bg-primary bg-opacity-10 p-2 rounded-3">
            <BsStars className="text-primary fs-4" />
          </div>
          <div>
            <h5 className="mb-0 fw-bold d-flex align-items-center gap-2">
              Document Summary
              {result && (
                <Badge bg={isLocal ? "secondary" : "info"} className="fw-normal" style={{ fontSize: '0.7rem' }}>
                  {result.type} Mode
                </Badge>
              )}
            </h5>
          </div>
        </div>
        {!result && (
          <Button 
            variant="primary" 
            className="rounded-pill px-4 shadow-sm btn-gradient"
            onClick={onSummarize}
            disabled={loading}
          >
            {loading ? (
              <>
                <Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true" className="me-2" />
                Analyzing...
              </>
            ) : (
                'Generate Summary'
            )}
          </Button>
        )}
      </div>

      {result ? (
        <div className="summary-content animate-fade-in">
          <div className="bg-light-op p-4 rounded-4 shadow-sm">
             <div className="d-flex align-items-center justify-content-between mb-3 text-muted">
                <div className="d-flex align-items-center gap-2">
                    <BsFileTextFill className="text-primary" />
                    <span className="small fw-bold text-uppercase tracking-wider">Analysis Result</span>
                </div>
                {isLocal && (
                    <div className="d-flex align-items-center gap-1 text-success small">
                        <BsShieldCheck /> Offline Privacy Mode
                    </div>
                )}
             </div>
             <p className="mb-0 lead fs-6 text-white lh-base" style={{ whiteSpace: 'pre-wrap' }}>{result.summary}</p>
          </div>
          <div className="mt-3 text-center">
             <Button variant="link" className="text-decoration-none text-muted small" onClick={onSummarize}>
                Regenerate Summary
             </Button>
          </div>
        </div>
      ) : (
        <div className="text-center py-3">
          <p className="text-muted mb-0">Need a quick overview of this file? Generate a summary locally or using AI.</p>
        </div>
      )}
    </div>
  );
};

export default SummarizerBox;
