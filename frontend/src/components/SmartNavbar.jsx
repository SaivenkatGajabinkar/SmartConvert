import React from 'react';
import { Navbar, Container, Nav } from 'react-bootstrap';
import { BsLightningFill, BsHouseFill, BsClockHistory } from 'react-icons/bs';

/**
 * SmartNavbar - Header Navigation Bar
 * 
 * Features:
 * - Brand logo click returns to Home and resets file upload state.
 * - Navigates between Home ("#/") and History ("#/history").
 * - Clean glassmorphic styling matching the dark theme.
 */
const SmartNavbar = ({ activeTab, onTabChange, onGoHome }) => {
  // Returns to home and resets active file state
  const handleHomeClick = (e) => {
    if (e && e.preventDefault) e.preventDefault();
    if (onGoHome) {
      onGoHome();
    } else {
      onTabChange("converter");
    }
  };

  // Navigates to history dashboard
  const handleHistoryClick = (e) => {
    if (e && e.preventDefault) e.preventDefault();
    onTabChange("history");
  };


  return (
    <Navbar expand="lg" className="smart-navbar sticky-top" variant="dark">
      <Container>
        <Navbar.Brand 
          onClick={handleHomeClick} 
          style={{ cursor: 'pointer' }}
          className="d-flex align-items-center gap-2 logo-container text-decoration-none"
          role="button"
          tabIndex={0}
        >
          <div className="logo-icon-bg shadow-sm animated-logo">
            <BsLightningFill className="logo-icon" />
          </div>
          <span className="brand-title text-white">Smart<span className="gradient-text-logo">Convert</span></span>
        </Navbar.Brand>
        
        <Navbar.Toggle aria-controls="basic-navbar-nav" className="custom-toggler" />
        <Navbar.Collapse id="basic-navbar-nav">
          <Nav className="ms-auto align-items-center gap-lg-3 nav-links-container">
            <Nav.Link 
              as="button"
              type="button"
              onClick={handleHomeClick} 
              className={`nav-link-custom d-flex align-items-center gap-2 border-0 bg-transparent ${activeTab === 'converter' ? 'active' : ''}`}
            >
              <BsHouseFill className="nav-icon" /> Home
            </Nav.Link>
            
            <Nav.Link 
              as="button"
              type="button"
              onClick={handleHistoryClick} 
              className={`nav-link-custom d-flex align-items-center gap-2 border-0 bg-transparent ${activeTab === 'history' ? 'active' : ''}`}
            >
              <BsClockHistory className="nav-icon" /> History
            </Nav.Link>
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
};

export default SmartNavbar;
