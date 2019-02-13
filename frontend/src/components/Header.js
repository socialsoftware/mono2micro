import React from 'react';
import { Link } from 'react-router-dom';
import { Navbar, Nav, NavDropdown, MenuItem } from 'react-bootstrap';
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
                <MenuItem eventKey={name}>{name}</MenuItem>
            </LinkContainer>
        );

        return (
            <Navbar inverse collapseOnSelect>
                <Navbar.Header>
                    <Navbar.Brand>
                        <Link to='/'>mono2micro</Link>
                    </Navbar.Brand>
                    <Navbar.Toggle />
                </Navbar.Header>
                <Navbar.Collapse>
                    <Nav>
                        <NavDropdown eventKey={1} title="Manage Clusters" id="basic-nav-dropdown">
                            <LinkContainer to={{ pathname: '/dendrogram/create', headerFunction: { handleGetGraphsFunction: this.handleGetGraphs} }}>
                                <MenuItem eventKey={1.1} >Create Dendrogram</MenuItem>
                            </LinkContainer>
                            <LinkContainer to={{ pathname: '/dendrogram/cut', headerFunction: { handleGetGraphsFunction: this.handleGetGraphs} }} >
                                <MenuItem eventKey={1.2} >Dendrogram Cut</MenuItem>
                            </LinkContainer>
                            <MenuItem divider />
                            {graphs}
                        </NavDropdown>
                    </Nav>
              </Navbar.Collapse>
            </Navbar>
        );
    }
}
