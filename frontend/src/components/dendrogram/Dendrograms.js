import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { Form, Row, Col, FormControl, Dropdown, DropdownButton, Button, Card, Breadcrumb } from 'react-bootstrap';
import { URL } from '../../constants/constants';
import BootstrapTable from 'react-bootstrap-table-next';

var HttpStatus = require('http-status-codes');

export class Dendrograms extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            codebaseName: this.props.match.params.codebaseName,
            codebase: {},
            dendrograms: [],
            allGraphs: [],
            profiles: [],
            selectedProfiles: [],
            isUploaded: "",
            newDendrogramName: "",
            linkageType: "",
            accessMetricWeight: "",
            writeMetricWeight: "",
            readMetricWeight: "",
            sequenceMetricWeight: ""
        };

        this.handleDeleteDendrogram = this.handleDeleteDendrogram.bind(this);
        this.handleSubmit= this.handleSubmit.bind(this);
        this.handleChangeNewDendrogramName = this.handleChangeNewDendrogramName.bind(this);
        this.handleLinkageType = this.handleLinkageType.bind(this);
        this.handleChangeAccessMetricWeight = this.handleChangeAccessMetricWeight.bind(this);
        this.handleChangeWriteMetricWeight = this.handleChangeWriteMetricWeight.bind(this);
        this.handleChangeReadMetricWeight = this.handleChangeReadMetricWeight.bind(this);
        this.handleChangeSequenceMetricWeight = this.handleChangeSequenceMetricWeight.bind(this);
    }

    componentDidMount() {
        this.loadCodebase();
    }

    loadCodebase() {
        const service = new RepositoryService();
        service.getCodebase(this.state.codebaseName).then(response => {
            if (response.data !== null) {
                this.setState({
                    codebase: response.data,
                    profiles: Object.keys(response.data.profiles),
                    dendrograms: response.data.dendrograms,
                    allGraphs: response.data.dendrograms.map(dendrogram => dendrogram.graphs).flat()
                });
            }
        });
    }

    handleSubmit(event){
        event.preventDefault()

        this.setState({
            isUploaded: "Uploading..."
        });
        
        const service = new RepositoryService();
        service.createDendrogram(this.state.codebaseName, this.state.newDendrogramName, this.state.linkageType, Number(this.state.accessMetricWeight), Number(this.state.writeMetricWeight), Number(this.state.readMetricWeight), Number(this.state.sequenceMetricWeight), this.state.selectedProfiles).then(response => {
            if (response.status === HttpStatus.CREATED) {
                this.loadCodebase();
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
                    isUploaded: "Upload failed. Dendrogram name already exists."
                });
            } else {
                this.setState({
                    isUploaded: "Upload failed."
                });
            }
        });
    }

    handleChangeNewDendrogramName(event) {
        this.setState({ 
            newDendrogramName: event.target.value 
        });
    }

    handleLinkageType(event) {
        this.setState({
            linkageType: event.target.id
        });
    }

    handleChangeAccessMetricWeight(event) {
        this.setState({
            accessMetricWeight: event.target.value
        });
    }

    handleChangeWriteMetricWeight(event) {
        this.setState({
            writeMetricWeight: event.target.value
        });
    }

    handleChangeReadMetricWeight(event) {
        this.setState({
            readMetricWeight: event.target.value
        });
    }

    handleChangeSequenceMetricWeight(event) {
        this.setState({
           sequenceMetricWeight: event.target.value
        });
    }

    selectProfile(profile) {
        if (this.state.selectedProfiles.includes(profile)) {
            let filteredArray = this.state.selectedProfiles.filter(p => p !== profile);
            this.setState({
                selectedProfiles: filteredArray
            });
        } else {
            this.setState({
                selectedProfiles: [...this.state.selectedProfiles, profile]
            });
        }
    }

    handleDeleteDendrogram(dendrogramName) {
        const service = new RepositoryService();
        service.deleteDendrogram(this.state.codebaseName, dendrogramName).then(response => {
            this.loadCodebase();
        });
    }

    renderBreadCrumbs = () => {
        return (
            <Breadcrumb>
                <Breadcrumb.Item href="/">Home</Breadcrumb.Item>
                <Breadcrumb.Item href="/codebases">Codebases</Breadcrumb.Item>
                <Breadcrumb.Item href={`/codebases/${this.state.codebaseName}`}>{this.state.codebaseName}</Breadcrumb.Item>
                <Breadcrumb.Item active>Dendrograms</Breadcrumb.Item>
            </Breadcrumb>
        );
    }

    renderCreateDendrogramForm = () => {
        return (
            <Form onSubmit={this.handleSubmit}>
                <Form.Group as={Row} controlId="newDendrogramName">
                    <Form.Label column sm={3}>
                        Dendrogram Name
                    </Form.Label>
                    <Col sm={5}>
                        <FormControl 
                            type="text"
                            maxLength="30"
                            placeholder="Dendrogram Name"
                            value={this.state.newDendrogramName}
                            onChange={this.handleChangeNewDendrogramName}/>
                    </Col>
                </Form.Group>

                <Form.Group as={Row} controlId="selectControllerProfiles">
                    <Form.Label column sm={3}>
                        Select Controller Profiles
                    </Form.Label>
                    <Col sm={5}>
                        <DropdownButton title={'Controller Profiles'}>
                            {this.state.profiles.map(profile =>
                                <Dropdown.Item
                                    key={profile}
                                    onSelect={() => this.selectProfile(profile)}
                                    active={this.state.selectedProfiles.includes(profile)}>{profile}</Dropdown.Item>)}
                        </DropdownButton>
                    </Col>
                </Form.Group>

                <fieldset>
                    <Form.Group as={Row}>
                        <Form.Label as="legend" column sm={3}>
                            Linkage Type
                        </Form.Label>
                        <Col sm={5}>
                        <Form.Check
                            onClick={this.handleLinkageType}
                            name="linkageType"
                            label="Average"
                            type="radio"
                            id="average"/>
                        <Form.Check
                            onClick={this.handleLinkageType}
                            name="linkageType"
                            label="Single"
                            type="radio"
                            id="single"/>
                        <Form.Check
                            onClick={this.handleLinkageType}
                            name="linkageType"
                            label="Complete"
                            type="radio"
                            id="complete"/>
                        </Col>
                    </Form.Group>
                </fieldset>

                <Form.Group as={Row} controlId="access">
                    <Form.Label column sm={3}>
                        Access Metric Weight (%)
                    </Form.Label>
                    <Col sm={5}>
                        <FormControl 
                            type="number"
                            placeholder="0-100"
                            value={this.state.accessMetricWeight}
                            onChange={this.handleChangeAccessMetricWeight}/>
                    </Col>
                </Form.Group>

                <Form.Group as={Row} controlId="write">
                    <Form.Label column sm={3}>
                        Write Metric Weight (%)
                    </Form.Label>
                    <Col sm={5}>
                        <FormControl 
                            type="number"
                            placeholder="0-100"
                            value={this.state.writeMetricWeight}
                            onChange={this.handleChangeWriteMetricWeight}/>
                    </Col>
                </Form.Group>

                <Form.Group as={Row} controlId="read">
                    <Form.Label column sm={3}>
                        Read Metric Weight (%)
                    </Form.Label>
                    <Col sm={5}>
                        <FormControl 
                            type="number"
                            placeholder="0-100"
                            value={this.state.readMetricWeight}
                            onChange={this.handleChangeReadMetricWeight}/>
                    </Col>
                </Form.Group>

                <Form.Group as={Row} controlId="sequence">
                    <Form.Label column sm={3}>
                        Sequence Metric Weight (%)
                    </Form.Label>
                    <Col sm={5}>
                        <FormControl 
                            type="number"
                            placeholder="0-100"
                            value={this.state.sequenceMetricWeight}
                            onChange={this.handleChangeSequenceMetricWeight}/>
                    </Col>
                </Form.Group>

                <Form.Group as={Row}>
                    <Col sm={{ span: 5, offset: 3 }}>
                        <Button type="submit"
                                disabled={this.state.isUploaded === "Uploading..." || 
                                        this.state.newDendrogramName === "" || 
                                        this.state.linkageType === "" || 
                                        this.state.accessMetricWeight === "" || 
                                        this.state.writeMetricWeight === "" ||
                                        this.state.readMetricWeight === "" || 
                                        this.state.sequenceMetricWeight === "" ||
                                        Number(this.state.accessMetricWeight) + Number(this.state.writeMetricWeight) + Number(this.state.readMetricWeight) + Number(this.state.sequenceMetricWeight) !== 100 || 
                                        this.state.selectedProfiles.length === 0}>
                            Create Dendrogram
                        </Button>
                        <Form.Text>
                            {this.state.isUploaded}
                        </Form.Text>
                    </Col>
                </Form.Group>
            </Form>
        );
    }

    renderDendrograms = () => {
        return this.state.dendrograms.map(dendrogram =>
            <div>
                <Card className="mb-4" key={dendrogram.name} style={{ width: '20rem' }}>
                    <Card.Img variant="top" src={URL + "codebase/" + this.state.codebaseName + "/dendrogram/" + dendrogram.name + "/image?" + new Date().getTime()}/>
                    <Card.Body>
                        <Card.Title>{dendrogram.name}</Card.Title>
                        <Card.Text>
                            Linkage Type: {dendrogram.linkageType}< br/>
                            Access: {dendrogram.accessMetricWeight}%< br/>
                            Write: {dendrogram.writeMetricWeight}%< br/>
                            Read: {dendrogram.readMetricWeight}%< br/>
                            Sequence: {dendrogram.sequenceMetricWeight}%
                        </Card.Text>
                        <Button href={`/codebases/${this.state.codebaseName}/dendrograms/${dendrogram.name}`} 
                                className="mb-2">
                                    Go to Dendrogram
                        </Button>
                        <br/>
                        <Button onClick={() => this.handleDeleteDendrogram(dendrogram.name)}
                                variant="danger">
                                    Delete
                        </Button>
                    </Card.Body>
                </Card>
                <br/>
            </div>
        );
    }

    render() {
        const metricRows = this.state.allGraphs.map(graph => {
            debugger;
            return {
                id: graph.dendrogramName + graph.name,
                dendrogram: graph.dendrogramName,
                graph: graph.name,
                clusters: graph.clusters.length,
                singleton: graph.clusters.filter(c => c.entities.length === 1).length,
                max_cluster_size: Math.max(...graph.clusters.map(c => Object.keys(c.entities).length)),
                ss: graph.silhouetteScore,
                cohesion: graph.cohesion,
                coupling: graph.coupling,
                complexity: graph.complexity
            }
        });

        const metricColumns = [{
            dataField: 'dendrogram',
            text: 'Dendrogram',
            sort: true
        }, {
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
        }, {
            dataField: 'cohesion',
            text: 'Cohesion',
            sort: true
        }, {
            dataField: 'coupling',
            text: 'Coupling',
            sort: true
        }, {
            dataField: 'complexity',
            text: 'Complexity',
            sort: true
        }];

        return (
            <div>
                {this.renderBreadCrumbs()}
                
                <h4 style={{color: "#666666"}}>Create Dendrogram</h4>
                {this.renderCreateDendrogramForm()}

                <h4 style={{color: "#666666"}}>Dendrograms</h4>
                {this.renderDendrograms()}

                {this.state.allGraphs.length > 0 &&
                    <div>
                        <h4 style={{color: "#666666"}}>Metrics</h4>
                        <BootstrapTable bootstrap4 keyField='id' data={ metricRows } columns={ metricColumns } />
                    </div>
                }
            </div>
        )
    }
}