import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { DENDROGRAM_URL } from '../../constants/constants';
import { Button, Form, Card, CardDeck, Breadcrumb, BreadcrumbItem, Table } from 'react-bootstrap';
import BootstrapTable from 'react-bootstrap-table-next';

var HttpStatus = require('http-status-codes');

export class DendrogramCut extends React.Component {
    constructor(props) {
        super(props);
        this.state = { 
            cutValue: "",
            cutSuccess: "",
            dendrogramName: this.props.match.params.dendrogramName,
            graphs: []
        };

        this.handleCutValueChange = this.handleCutValueChange.bind(this);
        this.handleCutSubmit = this.handleCutSubmit.bind(this);
    }

    componentDidMount() {
        this.loadGraphs();
    }

    loadGraphs() {
        const service = new RepositoryService();
        service.getGraphs(this.state.dendrogramName).then(response => {
            this.setState({
                graphs: response.data
            });
        });
    }

    handleCutValueChange(event) {
        this.setState({cutValue: event.target.value});
    }
    
    handleCutSubmit(event) {
        event.preventDefault();
        const service = new RepositoryService();
        this.setState({
            cutSuccess: "Processing..."
        });
        service.cutDendrogram(this.state.dendrogramName, this.state.cutValue).then(response => {
            if (response.status === HttpStatus.OK) {
                this.loadGraphs();
                this.setState({
                    cutSuccess: "Dendrogram cut successful."
                });
            } else {
                this.setState({
                    cutSuccess: "Failed to cut dendrogram."
                });
            }
        })
        .catch(error => {
            this.setState({
                cutSuccess: "Failed to cut dendrogram."
            });
        });
    }

    handleDeleteGraph(graphName) {
        const service = new RepositoryService();
        service.deleteGraph(this.state.dendrogramName, graphName).then(response => {
            this.loadGraphs();
        });
    }

    render() {
        const BreadCrumbs = () => {
            return (
              <div>
                <Breadcrumb style={{ backgroundColor: '#a32a2a' }}>
                  <Breadcrumb.Item href="/">Home</Breadcrumb.Item>
                  <Breadcrumb.Item href="/dendrograms">Dendrograms</Breadcrumb.Item>
                  <BreadcrumbItem active>{this.state.dendrogramName}</BreadcrumbItem>
                </Breadcrumb>
              </div>
            );
        };

        const graphs = this.state.graphs.map( graph =>
            <Card key={graph.name} style={{ width: '18rem' }}>
                <Card.Body>
                    <Card.Title>{graph.name}</Card.Title>
                    <Button href={`/dendrogram/${this.state.dendrogramName}/graph/${graph.name}`} className="mr-4" variant="primary">See Graph</Button>
                    <Button onClick={() => this.handleDeleteGraph(graph.name)} variant="danger">Delete</Button>
                </Card.Body>
            </Card>
        );

        const rows = this.state.graphs.map(graph => {
            return {
                graph: graph.name,
                cut: graph.cutValue,
                clusters: graph.clusters.length,
                singleton: graph.clusters.filter(c => c.entities.length === 1).length,
                max_cluster_size: Math.max(...graph.clusters.map(c => c.entities.length)),
                ss: graph.silhouetteScore
            } 
        });

        const columns = [{
            dataField: 'graph',
            text: 'Graph',
            sort: true
        }, {
            dataField: 'cut',
            text: 'Cut',
            sort: true
        }, {
            dataField: 'clusters',
            text: 'Number of Retrieved Clusters',
            sort: true
        }, {
            dataField: 'singleton',
            text: 'Number of Singleton Clusters',
            sort: true
        }, {
            dataField: 'max_cluster_size',
            text: 'Maximum Cluster Size',
            sort: true
        }, {
            dataField: 'ss',
            text: 'Silhouette Score',
            sort: true
        }];

        return (
            <div>
                <BreadCrumbs />
                <h2>Cut Dendrogram</h2>
                <Form onSubmit={this.handleCutSubmit}>
                    <Form.Group controlId="formDendrogramCut">
                        <Form.Control type="number" value={this.state.cutValue} onChange={this.handleCutValueChange} />
                    </Form.Group>
                    <Button variant="primary" type="submit" disabled={this.state.cutSuccess === "Processing..."}>
                        Submit
                    </Button>
                </Form>
                <br />
                {this.state.cutSuccess}
                <br />
                <br />
                <img src={DENDROGRAM_URL + "?dendrogramName=" + this.state.dendrogramName + "&&" + new Date().getTime()} alt="Dendrogram" />
            
                <CardDeck>
                    {graphs}
                </CardDeck>

                {this.state.graphs.length > 0 &&
                    <BootstrapTable bootstrap4 keyField='graph' data={ rows } columns={ columns } />
                }
            </div>
        );
    };
}