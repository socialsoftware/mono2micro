import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { URL } from '../../constants/constants';
import { InputGroup, Button, ButtonGroup, Form, Card, Breadcrumb, BreadcrumbItem, ButtonToolbar } from 'react-bootstrap';
import BootstrapTable from 'react-bootstrap-table-next';

var HttpStatus = require('http-status-codes');

export class DendrogramCut extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            codebaseName: this.props.match.params.codebaseName,
            dendrogramName: this.props.match.params.dendrogramName,
            height: "",
            numberClusters: "",
            cutSuccess: "",
            graphs: [],
            graph: {}
        };

        this.handleHeightChange = this.handleHeightChange.bind(this);
        this.handleNumberClustersChange = this.handleNumberClustersChange.bind(this);
        this.handleCutSubmit = this.handleCutSubmit.bind(this);
    }

    componentDidMount() {
        this.loadGraphs();
    }

    loadGraphs() {
        const service = new RepositoryService();
        service.getGraphs(this.state.dendrogramName).then(response => {
            this.setState({
                graphs: response.data,
                graph: response.data === [] ? {} : response.data[0]
            });
        });
    }

    handleHeightChange(event) {
        this.setState({height: event.target.value});
    }

    handleNumberClustersChange(event) {
        this.setState({numberClusters: event.target.value});
    }
    
    handleCutSubmit(event) {
        event.preventDefault();
        
        this.setState({
            cutSuccess: "Processing..."
        });

        let cutType;
        let cutValue;
        if (this.state.height !== "") {
            cutType = "h";
            cutValue = Number(this.state.height);
        } else {
            cutType = "n";
            cutValue = Number(this.state.numberClusters).toFixed(0);
        }

        const service = new RepositoryService();
        service.cutDendrogram(this.state.dendrogramName, cutValue, cutType).then(response => {
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

    changeGraph(value) {
        this.setState({
            graph: this.state.graphs.filter(graph => graph.name === value)[0]
        });
    }

    render() {
        const BreadCrumbs = () => {
            return (
              <div>
                <Breadcrumb>
                  <Breadcrumb.Item href="/">Home</Breadcrumb.Item>
                  <Breadcrumb.Item href="/dendrograms">Dendrograms</Breadcrumb.Item>
                  <BreadcrumbItem active>{this.state.dendrogramName}</BreadcrumbItem>
                </Breadcrumb>
              </div>
            );
        };

        const graphs = this.state.graphs.map(graph =>
            <Button active={this.state.graph.name === graph.name} onClick={() => this.changeGraph(graph.name)}>{graph.name}</Button>
        );

        const rows = this.state.graphs.map(graph => {
            return {
                graph: graph.name,
                cut: graph.cutValue,
                clusters: graph.clusters.length,
                singleton: graph.clusters.filter(c => c.entities.length === 1).length,
                max_cluster_size: Math.max(...graph.clusters.map(c => c.entities.length)),
                ss: Number(graph.silhouetteScore.toFixed(2))
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
                <ButtonToolbar>
                <Form onSubmit={this.handleCutSubmit}>
                    <InputGroup className="mt-3 mb-2">
                    <InputGroup.Prepend>
                        <InputGroup.Text id="basic-addon1">Height</InputGroup.Text>
                    </InputGroup.Prepend>
                        <Form.Control type="number" value={this.state.height} onChange={this.handleHeightChange} />
                    </InputGroup>

                    OR

                    <InputGroup className="mt-2 mb-3">
                    <InputGroup.Prepend>
                        <InputGroup.Text id="basic-addon1">Number of Clusters</InputGroup.Text>
                    </InputGroup.Prepend>
                        <Form.Control type="number" value={this.state.numberClusters} onChange={this.handleNumberClustersChange} />
                    </InputGroup>
                    
                    <Button variant="primary" 
                        type="submit" 
                        disabled={(this.state.height !== "" && this.state.numberClusters !== "") || 
                        (this.state.height === "" && this.state.numberClusters === "")}>
                    Submit
                </Button>
                </Form>
                </ButtonToolbar>
                <br />
                {this.state.cutSuccess}
                <br />
                <br />
                <img className="mb-5" src={URL + "codebase/" + this.state.codebaseName + "/dendrogram/" + this.state.dendrogramName + "/image?" + new Date().getTime()} alt="Dendrogram" />

                {graphs.length !== 0 &&
                    <div>
                    <div style={{overflow: "auto"}} className="mb-3">
                    <ButtonGroup>
                        {graphs}
                    </ButtonGroup>
                    </div>

                    <Card className="mb-5" key={this.state.graph.name} style={{ width: '15rem' }}>
                        <Card.Body>
                            <Card.Title>{this.state.graph.name}</Card.Title>
                            <Button href={`/dendrogram/${this.state.dendrogramName}/graph/${this.state.graph.name}`} className="mr-4" variant="primary">See Graph</Button>
                            <Button onClick={() => this.handleDeleteGraph(this.state.graph.name)} variant="danger">Delete</Button>
                        </Card.Body>
                    </Card>
                    </div>
                }

                {this.state.graphs.length > 0 &&
                    <span>
                    <h5>Metrics</h5>
                    <BootstrapTable bootstrap4 keyField='graph' data={ rows } columns={ columns } />
                    </span>
                }
            </div>
        );
    };
}