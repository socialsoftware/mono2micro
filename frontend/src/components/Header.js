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
    }

    componentDidMount() {
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
                            <LinkContainer to='/dendrogram/load'>
                                <MenuItem eventKey={1.1} >Load Dendrogram</MenuItem>
                            </LinkContainer>
                            <LinkContainer to='/dendrogram/cut'>
                                <MenuItem eventKey={1.1} >Dendrogram Cut</MenuItem>
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
