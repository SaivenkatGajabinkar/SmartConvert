import React from 'react';
import { Navbar, Container, Nav, Button } from 'react-bootstrap';
import { BsLightningFill, BsGithub, BsHouseFill, BsClockHistory, BsCompass } from 'react-icons/bs';

const SmartNavbar = ({ activeTab, onTabChange }) => {
  return (
    <Navbar expand="lg" className="smart-navbar sticky-top" variant="dark">
      <Container>
        <Navbar.Brand 
          onClick={() => onTabChange("converter")} 
          style={{ cursor: 'pointer' }}
          className="d-flex align-items-center gap-2 logo-container"
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
              onClick={() => onTabChange("converter")} 
              className={`nav-link-custom d-flex align-items-center gap-2 ${activeTab === 'converter' ? 'active' : ''}`}
            >
              <BsHouseFill className="nav-icon" /> Home
            </Nav.Link>
            
            <Nav.Link 
              onClick={() => onTabChange("history")} 
              className={`nav-link-custom d-flex align-items-center gap-2 ${activeTab === 'history' ? 'active' : ''}`}
            >
              <BsClockHistory className="nav-icon" /> History
            </Nav.Link>

            <div className="vertical-divider d-none d-lg-block"></div>
            
            <Button 
                variant="outline-light" 
                className="rounded-pill px-4 d-flex align-items-center gap-2 btn-github shadow-sm border-white-op"
                href="https://github.com" 
                target="_blank"
            >
              <BsGithub /> GitHub
            </Button>
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
};

export default SmartNavbar;
