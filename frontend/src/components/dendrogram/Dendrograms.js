import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { Form, Row, Col, FormControl, Dropdown, DropdownButton, ButtonGroup, Button, Card, Breadcrumb } from 'react-bootstrap';
import { URL } from '../../constants/constants';
import BootstrapTable from 'react-bootstrap-table-next';

var HttpStatus = require('http-status-codes');

export class Dendrograms extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            codebaseName: this.props.match.params.codebaseName,
            codebase: {},
            dendrogram: {},
            dendrograms: [],
            dendrogramGraphs: [],
            isUploaded: "",
            newDendrogramName: "",
            linkageType: "",
            accessMetricWeight: "",
            writeMetricWeight: "",
            readMetricWeight: "",
            sequenceMetric1Weight: "",
            sequenceMetric2Weight: "",
            sequenceMetric3Weight: "",
            selectedProfiles: []
        };

        this.handleDeleteDendrogram = this.handleDeleteDendrogram.bind(this);
        this.handleSubmit= this.handleSubmit.bind(this);
        this.handleChangeNewDendrogramName = this.handleChangeNewDendrogramName.bind(this);
        this.handleLinkageType = this.handleLinkageType.bind(this);
        this.handleChangeAccessMetricWeight = this.handleChangeAccessMetricWeight.bind(this);
        this.handleChangeWriteMetricWeight = this.handleChangeWriteMetricWeight.bind(this);
        this.handleChangeReadMetricWeight = this.handleChangeReadMetricWeight.bind(this);
        this.handleChangeSequenceMetric1Weight = this.handleChangeSequenceMetric1Weight.bind(this);
        this.handleChangeSequenceMetric2Weight = this.handleChangeSequenceMetric2Weight.bind(this);
        this.handleChangeSequenceMetric3Weight = this.handleChangeSequenceMetric3Weight.bind(this);
    }

    componentDidMount() {
        const service = new RepositoryService();
        service.getCodebase(this.state.codebaseName).then(response => {
            this.setState({
                codebase: response.data
            });
        });

        this.loadDendrograms();
    }

    loadDendrograms() {
        const service = new RepositoryService();
        service.getDendrograms(this.state.codebaseName).then(response => {
            this.setState({
                dendrogram: response.data[0],
                dendrograms: response.data,
                dendrogramGraphs: response.data.map(dend => dend.graphs).flat()
            });
        });
    }

    handleSubmit(event){
        event.preventDefault()
        const service = new RepositoryService();

        this.setState({
            isUploaded: "Uploading..."
        });
        
        service.createDendrogram(this.state.codebaseName, this.state.newDendrogramName, this.state.linkageType, Number(this.state.accessMetricWeight), Number(this.state.writeMetricWeight), Number(this.state.readMetricWeight), Number(this.state.sequenceMetric1Weight), Number(this.state.sequenceMetric2Weight), Number(this.state.sequenceMetric3Weight), this.state.selectedProfiles).then(response => {
            if (response.status === HttpStatus.CREATED) {
                this.loadDendrograms();
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

    handleChangeSequenceMetric1Weight(event) {
        this.setState({
           sequenceMetric1Weight: event.target.value
        });
    }

    handleChangeSequenceMetric2Weight(event) {
        this.setState({
           sequenceMetric2Weight: event.target.value
        });
    }

    handleChangeSequenceMetric3Weight(event) {
        this.setState({
           sequenceMetric3Weight: event.target.value
        });
    }

    selectProfile(profile) {
        if (this.state.selectedProfiles.includes(profile)) {
            this.state.selectedProfiles.splice(this.state.selectedProfiles.indexOf(profile), 1);
        } else {
            this.state.selectedProfiles.push(profile);
        }
        this.setState({
            selectedProfiles: this.state.selectedProfiles
        });
    }

    handleDeleteDendrogram() {
        const service = new RepositoryService();
        service.deleteDendrogram(this.state.codebaseName, this.state.dendrogram.name).then(response => {
            this.loadDendrograms();
        });
    }

    changeDendrogram(value) {
        this.setState({
            dendrogram: this.state.dendrograms.filter(d => d.name === value)[0]
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
                        <Breadcrumb.Item active>Dendrograms</Breadcrumb.Item>
                    </Breadcrumb>
              </div>
            );
        };

        const dendrograms = this.state.dendrograms.map(d =>
            <Button key={d.name} active={this.state.dendrogram.name === d.name} onClick={() => this.changeDendrogram(d.name)}>{d.name}</Button>
        );

        const metricRows = this.state.dendrogramGraphs.map(graph => {
            return {
                id: graph.dendrogramName + graph.name,
                dendrogram: graph.dendrogramName,
                graph: graph.name,
                clusters: graph.clusters.length,
                singleton: graph.clusters.filter(c => c.entities.length === 1).length,
                max_cluster_size: Math.max(...graph.clusters.map(c => c.entities.length)),
                ss: Number(graph.silhouetteScore.toFixed(2))
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
        }];

        return (
            <div>
                <BreadCrumbs />
                <h2>Dendrograms</h2>

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
                                {Object.keys(this.state.codebase).length === 0 ? [] : Object.keys(this.state.codebase.profiles).map(profile =>
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

                    <Form.Group as={Row} controlId="sequence1">
                        <Form.Label column sm={3}>
                            Sequence Metric 1 Weight (%)
                        </Form.Label>
                        <Col sm={5}>
                            <FormControl 
                                type="number"
                                placeholder="0-100"
                                value={this.state.sequenceMetric1Weight}
                                onChange={this.handleChangeSequenceMetric1Weight}/>
                        </Col>
                    </Form.Group>

                    <Form.Group as={Row} controlId="sequence2">
                        <Form.Label column sm={3}>
                            Sequence Metric 2 Weight (%)
                        </Form.Label>
                        <Col sm={5}>
                            <FormControl 
                                type="number"
                                placeholder="0-100"
                                value={this.state.sequenceMetric2Weight}
                                onChange={this.handleChangeSequenceMetric2Weight}/>
                        </Col>
                    </Form.Group>

                    <Form.Group as={Row} controlId="sequence3">
                        <Form.Label column sm={3}>
                            Sequence Metric 3 Weight (%)
                        </Form.Label>
                        <Col sm={5}>
                            <FormControl 
                                type="number"
                                placeholder="0-100"
                                value={this.state.sequenceMetric3Weight}
                                onChange={this.handleChangeSequenceMetric3Weight}/>
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
                                            this.state.sequenceMetric1Weight === "" || 
                                            this.state.sequenceMetric2Weight === "" || 
                                            this.state.sequenceMetric3Weight === "" || 
                                            Number(this.state.accessMetricWeight) + Number(this.state.writeMetricWeight) + Number(this.state.readMetricWeight) + Number(this.state.sequenceMetric1Weight) + Number(this.state.sequenceMetric2Weight) + Number(this.state.sequenceMetric3Weight) !== 100 || 
                                            this.state.selectedProfiles.length === 0}>
                                Create Dendrogram
                            </Button>
                            <Form.Text>
                                {this.state.isUploaded}
                            </Form.Text>
                        </Col>
                    </Form.Group>
                </Form>             

                {dendrograms.length !== 0 &&
                    <div>
                        <div style={{overflow: "auto"}} className="mb-2">
                            <ButtonGroup>
                                {dendrograms}
                            </ButtonGroup>
                        </div>

                        <Card className="mb-4" key={this.state.dendrogram.name} style={{ width: '20rem' }}>
                            <Card.Img variant="top" src={URL + "codebase/" + this.state.codebaseName + "/dendrogram/" + this.state.dendrogram.name + "/image?" + new Date().getTime()}/>
                            <Card.Body>
                                <Card.Title>Dendrogram: {this.state.dendrogram.name}</Card.Title>
                                <Card.Text>
                                    Linkage Type: {this.state.dendrogram.linkageType}< br/>
                                    Access Metric Weight: {this.state.dendrogram.accessMetricWeight}%< br/>
                                    Write Metric Weight: {this.state.dendrogram.writeMetricWeight}%< br/>
                                    Read Metric Weight: {this.state.dendrogram.readMetricWeight}%< br/>
                                    Sequence Metric 1 Weight: {this.state.dendrogram.sequenceMetric1Weight}%< br/>
                                    Sequence Metric 2 Weight: {this.state.dendrogram.sequenceMetric2Weight}%< br/>
                                    Sequence Metric 3 Weight: {this.state.dendrogram.sequenceMetric3Weight}%< br/>
                                    # of Controllers: {this.state.dendrogram.controllers.length}< br/>
                                    # of Entities: {this.state.dendrogram.entities.length}
                                </Card.Text>
                                <Button href={`/codebase/${this.state.codebaseName}/dendrogram/${this.state.dendrogram.name}`} className="mb-2">Go to Dendrogram</Button><br/>
                                <Button onClick={this.handleDeleteDendrogram} variant="danger">Delete</Button>
                            </Card.Body>
                        </Card>
                    </div>
                }

                {this.state.dendrogramGraphs.length > 0 &&
                    <div>
                        <h5>Metrics</h5>
                        <BootstrapTable bootstrap4 keyField='id' data={ metricRows } columns={ metricColumns } />
                    </div>
                }
            </div>
        )
    }
}