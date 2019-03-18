import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { Button, Card, CardDeck, Breadcrumb, BreadcrumbItem } from 'react-bootstrap';
import { DENDROGRAM_URL } from '../../constants/constants';

const BreadCrumbs = () => {
    return (
      <div>
        <Breadcrumb style={{ backgroundColor: '#a32a2a' }}>
          <Breadcrumb.Item href="/">Home</Breadcrumb.Item>
          <BreadcrumbItem active>Dendrograms</BreadcrumbItem>
        </Breadcrumb>
      </div>
    );
};

export class Dendrograms extends React.Component {
    constructor(props) {
        super(props);
        this.state = { 
            dendrogramNames: []
        };
    }

    componentDidMount() {
        this.loadDendrograms();
    }

    loadDendrograms() {
        const service = new RepositoryService();
        service.getDendrograms().then(response => {
            this.setState({
                dendrogramNames: response.data
            });
        });
    }

    handleDeleteDendrogram(name) {
        const service = new RepositoryService();
        service.deleteDendrogram(name).then(response => {
            this.loadDendrograms();
        });
    }

    render() {
        const dendrograms = this.state.dendrogramNames.map( name =>
            <Card key={name} style={{ width: '18rem' }}>
                <Card.Img variant="top" src={DENDROGRAM_URL + "?dendrogramName=" + name + "&&" + new Date().getTime()} />
                <Card.Body>
                    <Card.Title>{name}</Card.Title>
                    <Button href={`/dendrogram/${name}`} className="mr-4" variant="primary">See Dendrogram</Button>
                    <Button onClick={() => this.handleDeleteDendrogram(name)} variant="danger">Delete</Button>
                </Card.Body>
            </Card>
        );

        return (
            <div>
                <BreadCrumbs />
                <h2 className="mb-3">Dendrograms</h2>
                <Button className="mb-3" href="/dendrogram/create">Create New Dendrogram</Button>
                <CardDeck>
                    {dendrograms}
                </CardDeck>
            </div>
        )
    }
}