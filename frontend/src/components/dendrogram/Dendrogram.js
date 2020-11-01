import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { URL } from '../../constants/constants';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Form from 'react-bootstrap/Form';
import FormControl from 'react-bootstrap/FormControl';
import Card from 'react-bootstrap/Card';
import Button from 'react-bootstrap/Button';
import Breadcrumb from 'react-bootstrap/Breadcrumb';
import BootstrapTable from 'react-bootstrap-table-next';

var HttpStatus = require('http-status-codes');

export class Dendrogram extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            codebaseName: this.props.match.params.codebaseName,
            dendrogramName: this.props.match.params.dendrogramName,
            height: "",
            numberClusters: "",
            newExpert: "",
            isUploaded: "",
            cutSuccess: "",
            decompositions: [],
            expertFile: null, 
        };

        this.handleHeightChange = this.handleHeightChange.bind(this);
        this.handleNumberClustersChange = this.handleNumberClustersChange.bind(this);
        this.handleChangeNewExpert = this.handleChangeNewExpert.bind(this);
        this.handleDeleteDecomposition = this.handleDeleteDecomposition.bind(this);
        this.handleCutSubmit = this.handleCutSubmit.bind(this);
        this.handleExpertSubmit = this.handleExpertSubmit.bind(this);
        this.handleSelectNewExpertFile = this.handleSelectNewExpertFile.bind(this);
    }

    componentDidMount() {
        this.loadDecompositions();
    }

    loadDecompositions() {
        const service = new RepositoryService();
        service.getDecompositions(
            this.state.codebaseName,
            this.state.dendrogramName,
            [
                "name",
                "clusters",
                "singleton",
                "silhouetteScore",
                "complexity",
                "cohesion",
                "coupling",
                "performance"
            ]
        ).then(response => {
            this.setState({
                decompositions: response.data
            });
        });
    }

    handleHeightChange(event) {
        this.setState({
            height: event.target.value
        });
    }

    handleNumberClustersChange(event) {
        this.setState({
            numberClusters: event.target.value
        });
    }

    handleChangeNewExpert(event) {
        this.setState({ 
            newExpert: event.target.value 
        });
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
            cutValue = Number(this.state.numberClusters);
        }

        const service = new RepositoryService();
        
        service.cutDendrogram(
            this.state.codebaseName,
            this.state.dendrogramName,
            cutValue,
            cutType
        ).then(response => {
            if (response.status === HttpStatus.OK) {
                this.loadDecompositions();
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

    handleExpertSubmit(event){
        event.preventDefault();

        this.setState({
            isUploaded: "Uploading..."
        });

        const service = new RepositoryService();
        service.expertCut(this.state.codebaseName, this.state.dendrogramName, this.state.newExpert, this.state.expertFile).then(response => {
            if (response.status === HttpStatus.OK) {
                this.loadDecompositions();
                this.setState({
                    isUploaded: "Upload completed successfully."
                });
            } else {
                this.setState({
                    isUploaded: "Upload failed."
                });
            }
        })
        .catch(error => {
            if (error.response !== undefined && error.response.status === HttpStatus.UNAUTHORIZED) {
                this.setState({
                    isUploaded: "Upload failed. Expert name already exists."
                });
            } else {
                this.setState({
                    isUploaded: "Upload failed."
                });
            }
        });
    }

    handleDeleteDecomposition(decompositionName) {
        const service = new RepositoryService();
        service.deleteDecomposition(this.state.codebaseName, this.state.dendrogramName, decompositionName).then(response => {
            this.loadDecompositions();
        });
    }

    handleSelectNewExpertFile(event) {
        this.setState({
            expertFile: event.target.files[0]
        });
    }

    renderBreadCrumbs = () => {
        return (
            <Breadcrumb>
                <Breadcrumb.Item href="/">Home</Breadcrumb.Item>
                <Breadcrumb.Item href="/codebases">Codebases</Breadcrumb.Item>
                <Breadcrumb.Item href={`/codebases/${this.state.codebaseName}`}>{this.state.codebaseName}</Breadcrumb.Item>
                <Breadcrumb.Item href={`/codebases/${this.state.codebaseName}/dendrograms`}>Dendrograms</Breadcrumb.Item>
                <Breadcrumb.Item active>{this.state.dendrogramName}</Breadcrumb.Item>
            </Breadcrumb>
        );
    }

    renderCutForm = () => {
        return (
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
                                            (this.state.height === "" && this.state.numberClusters === "") ||
                                            this.state.cutSuccess === "Processing..."}>
                            Cut
                        </Button>
                        <Form.Text>
                            {this.state.cutSuccess}
                        </Form.Text>
                    </Col>
                </Form.Group>
            </Form>
        );
    }

    renderExpertForm = () => {
        return (
            <Form onSubmit={this.handleExpertSubmit}>
                <Form.Group as={Row} controlId="newExpertName">
                    <Form.Label column sm={2}>
                        Expert Name
                    </Form.Label>
                    <Col sm={5}>
                        <FormControl 
                            type="text"
                            maxLength="30"
                            placeholder="Expert Name"
                            value={this.state.newExpert}
                            onChange={this.handleChangeNewExpert}/>
                    </Col>
                </Form.Group>

                <Form.Group as={Row} controlId="newExpertFile">
                    <Form.Label column sm={2}>
                        Expert File (Optional)
                    </Form.Label>
                    <Col sm={5}>
                        <FormControl 
                            type="file"
                            onChange={this.handleSelectNewExpertFile}/>
                    </Col>
                </Form.Group>

                <Form.Group as={Row}>
                    <Col sm={{ span: 5, offset: 2 }}>
                        <Button type="submit"
                                disabled={this.state.isUploaded === "Uploading..." ||
                                        this.state.newExpert === ""}>
                            Create Expert
                        </Button>
                        <Form.Text>
                            {this.state.isUploaded}
                        </Form.Text>
                    </Col>
                </Form.Group>
            </Form>
        );
    }

    renderCuts = () => {
        return (
            <Row>
                {
                    this.state.decompositions.map(decomposition =>
                        <Col key={decomposition.name} md="auto">
                            <Card style={{ width: '15rem', marginBottom: "16px" }}>
                                <Card.Body>
                                    <Card.Title>
                                        {decomposition.name}
                                    </Card.Title>
                                    <Button
                                        href={`/codebases/${this.state.codebaseName}/dendrograms/${this.state.dendrogramName}/decompositions/${decomposition.name}`}
                                        className="mb-2"
                                    >
                                        Go to Decomposition
                                    </Button>
                                    <br/>
                                    <Button
                                        onClick={() => this.handleDeleteDecomposition(decomposition.name)}
                                        variant="danger"
                                    >
                                        Delete
                                    </Button>
                                </Card.Body>
                            </Card>
                        </Col>
                    )
                }
            </Row>
        );
    }

    render() {

        const metricRows = this.state.decompositions.map(decomposition => {
            return {
                decomposition: decomposition.name,
                clusters: decomposition.clusters.length,
                singleton: Object.values(decomposition.clusters).filter(c => Object.keys(c.entities).length === 1).length,
                max_cluster_size: Math.max(...Object.values(decomposition.clusters).map(c => Object.keys(c.entities).length)),
                ss: decomposition.silhouetteScore,
                cohesion: decomposition.cohesion,
                coupling: decomposition.coupling,
                complexity: decomposition.complexity,
                performance: decomposition.performance
            } 
        });

        const metricColumns = [
            {
                dataField: 'decomposition',
                text: 'Decomposition',
                sort: true
            },
            {
                dataField: 'clusters',
                text: 'Number of Retrieved Clusters',
                sort: true
            },
            {
                dataField: 'singleton',
                text: 'Number of Singleton Clusters',
                sort: true
            },
            {
                dataField: 'max_cluster_size',
                text: 'Maximum Cluster Size',
                sort: true
            },
            {
                dataField: 'ss',
                text: 'Silhouette Score',
                sort: true
            },
            {
                dataField: 'cohesion',
                text: 'Cohesion',
                sort: true
            },
            {
                dataField: 'coupling',
                text: 'Coupling',
                sort: true
            },
            {
                dataField: 'complexity',
                text: 'Complexity',
                sort: true
            },
            {
                dataField: 'performance',
                text: 'Performance',
                sort: true
            }
        ];

        return (
            <div>
                {this.renderBreadCrumbs()}
                
                <h4 style={{color: "#666666"}}>Cut Dendrogram</h4>
                {this.renderCutForm()}

                <h4 style={{color: "#666666"}}>Create Expert Cut</h4>
                {this.renderExpertForm()}

                <img width="100%" src={URL + "codebase/" + this.state.codebaseName + "/dendrogram/" + this.state.dendrogramName + "/image?" + new Date().getTime()} alt="Dendrogram" />

                <h4 style={{color: "#666666", marginTop: "16px" }}>Cuts</h4>
                {this.renderCuts()}

                {this.state.decompositions.length > 0 &&
                    <div>
                        <h4 style={{color: "#666666"}}>Metrics</h4>
                        <BootstrapTable bootstrap4 keyField='decomposition' data={ metricRows } columns={ metricColumns } />
                    </div>
                }
            </div>
        );
    };
}