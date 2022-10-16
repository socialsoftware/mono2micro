import React, { FunctionComponent } from 'react';
import { Link } from 'react-router-dom';
import Nav from 'react-bootstrap/Nav';
import Navbar from 'react-bootstrap/Navbar';

const Header: FunctionComponent = () => {

    return (
        <Navbar bg="dark" variant="dark" >
            <Navbar.Brand id="home">
                <Link to='/'>
                    Mono2Micro
                </Link>
            </Navbar.Brand>
            <Navbar.Toggle />
            <Navbar.Collapse>
                <Nav>
                    <Nav.Link href="/codebases">
                        Codebases
                    </Nav.Link>
                    <Nav.Link href="/comparisonTool">
                        Comparison Tool
                    </Nav.Link>
                </Nav>
            </Navbar.Collapse>
        </Navbar>
    );
}

export default Header;