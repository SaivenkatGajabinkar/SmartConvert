import React, { useEffect, useState } from 'react';
import { Table, Button, Spinner, Badge } from 'react-bootstrap';
import { BsDownload, BsClockHistory, BsFileEarmarkCheck, BsTrash } from 'react-icons/bs';
import api, { baseURL } from '../api/api';

const HistoryDashboard = () => {
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchHistory = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await api.get('/api/history');
      setHistory(res.data);
    } catch {
      setError('Failed to load conversion history.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHistory();
  }, []);

  const handleDownload = (id) => {
    window.location.href = `${baseURL}/api/download/${id}`;
  };

  const formatDate = (dateStr) => {
    try {
      const date = new Date(dateStr);
      return date.toLocaleString();
    } catch {
      return dateStr;
    }
  };

  const getFormatBadgeColor = (fmt) => {
    const formatColors = {
      PDF: 'danger',
      DOCX: 'primary',
      TXT: 'secondary',
      CSV: 'success',
      JSON: 'warning',
      XML: 'info',
      HTML: 'dark',
      PNG: 'success',
      JPG: 'info',
      JPEG: 'info',
    };
    return formatColors[fmt.toUpperCase()] || 'primary';
  };

  return (
    <div className="history-dashboard animate-fade-in">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h4 className="fw-bold mb-0 d-flex align-items-center gap-2 text-white">
          <BsClockHistory className="text-primary" /> Conversion History
        </h4>
        <Button variant="outline-primary" size="sm" onClick={fetchHistory} disabled={loading} className="rounded-pill px-3">
          Refresh
        </Button>
      </div>

      {error && <div className="alert alert-danger p-3 rounded-4">{error}</div>}

      {loading ? (
        <div className="text-center py-5">
          <Spinner animation="border" variant="primary" />
          <p className="mt-2 text-muted">Fetching your conversion history...</p>
        </div>
      ) : history.length === 0 ? (
        <div className="text-center py-5 border border-dashed rounded-4 bg-light-op">
          <p className="text-muted mb-0">No conversions recorded yet. Get started by uploading a file!</p>
        </div>
      ) : (
        <div className="table-responsive rounded-4 shadow-sm">
          <Table hover className="mb-0 align-middle table-custom bg-transparent">
            <thead className="text-secondary">
              <tr>
                <th>Original File</th>
                <th>Target Format</th>
                <th>Converted Filename</th>
                <th>Date & Time</th>
                <th className="text-end">Actions</th>
              </tr>
            </thead>
            <tbody>
              {history.map((item) => (
                <tr key={item.id}>
                  <td className="fw-semibold text-white">
                    <span className="me-2">📄</span>
                    {item.originalFilename}
                  </td>
                  <td>
                    <Badge bg={getFormatBadgeColor(item.outputFormat)} className="px-2.5 py-1.5 rounded-pill text-uppercase">
                      {item.outputFormat}
                    </Badge>
                  </td>
                  <td className="text-muted small text-truncate" style={{ maxWidth: '200px' }}>
                    {item.outputFilename}
                  </td>
                  <td className="text-secondary small">
                    {formatDate(item.convertedAt)}
                  </td>
                  <td className="text-end">
                    <Button 
                      variant="primary" 
                      size="sm" 
                      className="btn-gradient rounded-pill px-3 d-inline-flex align-items-center gap-1.5 shadow-sm"
                      onClick={() => handleDownload(item.id)}
                    >
                      <BsDownload size={13} /> Download
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        </div>
      )}
    </div>
  );
};

export default HistoryDashboard;
