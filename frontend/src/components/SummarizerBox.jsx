import React from 'react';
import { BsStars } from 'react-icons/bs';

function SummarizerBox({ summary, loading }) {
  if (loading) {
    return (
      <div className="summary-box text-center">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
        <p className="mt-2 text-muted">AI is analyzing your document...</p>
      </div>
    );
  }

  if (!summary) return null;

  return (
    <div className="summary-box">
      <h5 className="mb-3 d-flex align-items-center gap-2">
        <BsStars className="text-primary" /> AI Summary & Insights
      </h5>
      <div className="summary-content" style={{ lineHeight: '1.6', color: '#334155' }}>
        {summary.split('\n').map((line, i) => (
          <p key={i} className={line.startsWith('*') || line.startsWith('-') ? 'ps-3' : ''}>
            {line}
          </p>
        ))}
      </div>
    </div>
  );
}

export default SummarizerBox;
