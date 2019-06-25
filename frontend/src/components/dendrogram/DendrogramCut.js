import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { URL } from '../../constants/constants';
import { Row, Col, FormControl, Button, ButtonGroup, Form, Card, Breadcrumb } from 'react-bootstrap';
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
        this.handleDeleteGraph = this.handleDeleteGraph.bind(this);
        this.handleCutSubmit = this.handleCutSubmit.bind(this);
    }

    componentDidMount() {
        this.loadGraphs();
    }

    loadGraphs() {
        const service = new RepositoryService();
        service.getGraphs(this.state.codebaseName, this.state.dendrogramName).then(response => {
            this.setState({
                graphs: response.data,
                graph: response.data[0]
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
            cutType = "H";
            cutValue = Number(this.state.height);
        } else {
            cutType = "N";
            cutValue = Number(this.state.numberClusters).toFixed(0);
        }

        const service = new RepositoryService();
        service.cutDendrogram(this.state.codebaseName, this.state.dendrogramName, cutValue, cutType).then(response => {
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

    handleDeleteGraph() {
        const service = new RepositoryService();
        service.deleteGraph(this.state.codebaseName, this.state.dendrogramName, this.state.graph.name).then(response => {
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
                        <Breadcrumb.Item href="/codebases">Codebases</Breadcrumb.Item>
                        <Breadcrumb.Item href={`/codebase/${this.state.codebaseName}`}>{this.state.codebaseName}</Breadcrumb.Item>
                        <Breadcrumb.Item href={`/codebase/${this.state.codebaseName}/dendrograms`}>Dendrograms</Breadcrumb.Item>
                        <Breadcrumb.Item active>{this.state.dendrogramName}</Breadcrumb.Item>
                    </Breadcrumb>
                </div>
            );
        };

        const graphs = this.state.graphs.map(graph =>
            <Button active={this.state.graph.name === graph.name} onClick={() => this.changeGraph(graph.name)}>{graph.name}</Button>
        );

        const metricRows = this.state.graphs.map(graph => {
            return {
                graph: graph.name,
                clusters: graph.clusters.length,
                singleton: graph.clusters.filter(c => c.entities.length === 1).length,
                max_cluster_size: Math.max(...graph.clusters.map(c => c.entities.length)),
                ss: Number(graph.silhouetteScore.toFixed(2))
            } 
        });

        const metricColumns = [{
            dataField: 'graph',
            text: 'Graph',
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
                    <Form.Group as={Row} controlId="height">
                        <Form.Label column sm={2}>
                            Height
                        </Form.Label>
                        <Col sm={5}>
                            <FormControl 
                                type="number"
                                placeholder="Cut Height"
                                value={this.state.height}
                                onChange={this.handleHeightChange}/>
                        </Col>
                    </Form.Group>

                    <Form.Group as={Row} controlId="or">
                        <Form.Label column sm={2}>
                            OR
                        </Form.Label>
                    </Form.Group>

                    <Form.Group as={Row} controlId="numberOfClusters">
                        <Form.Label column sm={2}>
                            Number of Clusters
                        </Form.Label>
                        <Col sm={5}>
                            <FormControl 
                                type="number"
                                placeholder="Number of Clusters in Cut"
                                value={this.state.numberClusters}
                                onChange={this.handleNumberClustersChange}/>
                        </Col>
                    </Form.Group>

                    <Form.Group as={Row}>
                        <Col sm={{ span: 5, offset: 2 }}>
                            <Button type="submit"
                                    disabled={(this.state.height !== "" && this.state.numberClusters !== "") || 
                                              (this.state.height === "" && this.state.numberClusters === "")}>
                                Cut
                            </Button>
                            <Form.Text>
                                {this.state.cutSuccess}
                            </Form.Text>
                        </Col>
                    </Form.Group>
                </Form>

                <img className="mb-5" src={URL + "codebase/" + this.state.codebaseName + "/dendrogram/" + this.state.dendrogramName + "/image?" + new Date().getTime()} alt="Dendrogram" />

                {graphs.length !== 0 &&
                    <div>
                        <div style={{overflow: "auto"}} className="mb-2">
                            <ButtonGroup>
                                {graphs}
                            </ButtonGroup>
                        </div>

                        <Card className="mb-4" key={this.state.graph.name} style={{ width: '20rem' }}>
                            <Card.Body>
                                <Card.Title>Graph: {this.state.graph.name}</Card.Title>
                                <Button href={`/codebase/${this.state.codebaseName}/dendrogram/${this.state.dendrogramName}/graph/${this.state.graph.name}`} className="mb-2">Go to Graph</Button><br/>
                                <Button onClick={this.handleDeleteGraph} variant="danger">Delete</Button>
                            </Card.Body>
                        </Card>
                    </div>
                }

                {this.state.graphs.length > 0 &&
                    <div>
                        <h5>Metrics</h5>
                        <BootstrapTable bootstrap4 keyField='graph' data={ metricRows } columns={ metricColumns } />
                    </div>
                }
            </div>
        );
    };
}