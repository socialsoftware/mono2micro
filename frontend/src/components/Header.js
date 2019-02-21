import React from 'react';
import { Link } from 'react-router-dom';
import { Navbar, Nav, NavDropdown } from 'react-bootstrap';
import { LinkContainer } from 'react-router-bootstrap';

import { RepositoryService } from '../services/RepositoryService';

export class Header extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            graphs : [],
        };

        this.handleGetGraphs = this.handleGetGraphs.bind(this);
    }

    componentDidMount() {
         this.handleGetGraphs();
    }

    handleGetGraphs() {
        const service = new RepositoryService();
        service.getGraphs().then(response => {
            this.setState({
                graphs : response.data
            });
        });  
    }

    render() {
        const graphs = this.state.graphs.map( name =>
            <LinkContainer key={name} to={`/graphs/name/${name}`}>
                <NavDropdown.Item>{name}</NavDropdown.Item>
            </LinkContainer>
        );

        return (
            <Navbar bg="dark" variant="dark">
                <Navbar.Brand>
                    <Link to='/'>Mono2Micro</Link>
                </Navbar.Brand>
                <Navbar.Toggle />
                <Navbar.Collapse>
                    <Nav>
                        <NavDropdown title="Manage Clusters" id="basic-nav-dropdown">
                            <LinkContainer to={{ pathname: '/dendrogram/create', headerFunction: { handleGetGraphsFunction: this.handleGetGraphs} }}>
                                <NavDropdown.Item >Create Dendrogram</NavDropdown.Item>
                            </LinkContainer>
                            <LinkContainer to={{ pathname: '/dendrogram/cut', headerFunction: { handleGetGraphsFunction: this.handleGetGraphs} }} >
                                <NavDropdown.Item >Dendrogram Cut</NavDropdown.Item>
                            </LinkContainer>
                            <NavDropdown.Divider />
                            {graphs}
                        </NavDropdown>
                    </Nav>
                </Navbar.Collapse>
            </Navbar>
        );
    }
}
