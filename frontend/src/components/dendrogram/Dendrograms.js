import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Form from 'react-bootstrap/Form';
import FormControl from 'react-bootstrap/FormControl';
import Dropdown from 'react-bootstrap/Dropdown';
import DropdownButton from 'react-bootstrap/DropdownButton';
import Card from 'react-bootstrap/Card';
import Button from 'react-bootstrap/Button';
import Breadcrumb from 'react-bootstrap/Breadcrumb';

import { URL } from '../../constants/constants';
import BootstrapTable from 'react-bootstrap-table-next';

var HttpStatus = require('http-status-codes');

const sort = true;

const metricColumns = [
    {
        dataField: 'dendrogram',
        text: 'Dendrogram',
        sort,
    },
    {
        dataField: 'decomposition',
        text: 'Decomposition',
        sort,
    },
    {
        dataField: 'clusters',
        text: 'Number of Retrieved Clusters',
        sort,
    },
    {
        dataField: 'singleton',
        text: 'Number of Singleton Clusters',
        sort,
    },
    {
        dataField: 'max_cluster_size',
        text: 'Maximum Cluster Size',
        sort,
    },
    {
        dataField: 'ss',
        text: 'Silhouette Score',
        sort,
    },
    {
        dataField: 'cohesion',
        text: 'Cohesion',
        sort,
    },
    {
        dataField: 'coupling',
        text: 'Coupling',
        sort,
    },
    {
        dataField: 'complexity',
        text: 'Complexity',
        sort,
    },
    {
        dataField: 'performance',
        text: 'Performance',
        sort,
    }
];

export class Dendrograms extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            dendrograms: [],
            allDecompositions: [],
            selectedProfile: "",
            isUploaded: "",
            newDendrogramName: "",
            linkageType: "average",
            accessMetricWeight: "25",
            writeMetricWeight: "25",
            readMetricWeight: "25",
            sequenceMetricWeight: "25",
            amountOfTraces: "0",
            typeOfTraces: "ALL",
            codebase: {
                profiles: [],
            },
        };

        this.handleDeleteDendrogram = this.handleDeleteDendrogram.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleChangeNewDendrogramName = this.handleChangeNewDendrogramName.bind(this);
        this.handleLinkageType = this.handleLinkageType.bind(this);
        this.handleChangeAccessMetricWeight = this.handleChangeAccessMetricWeight.bind(this);
        this.handleChangeWriteMetricWeight = this.handleChangeWriteMetricWeight.bind(this);
        this.handleChangeReadMetricWeight = this.handleChangeReadMetricWeight.bind(this);
        this.handleChangeSequenceMetricWeight = this.handleChangeSequenceMetricWeight.bind(this);
        this.handleChangeAmountOfTraces = this.handleChangeAmountOfTraces.bind(this);
        this.handleChangeTypeOfTraces = this.handleChangeTypeOfTraces.bind(this);
    }

    componentDidMount() {
        this.loadDendrograms();
        this.loadDecompositions();
        this.loadCodebase();
    }

    loadCodebase() {
        const service = new RepositoryService();
        service.getCodebase(
            this.props.match.params.codebaseName,
            ["profiles"]
        ).then(response => {
            if (response.data !== null) {
                this.setState({ codebase: response.data });
            }
        });
    }

    loadDendrograms() {
        const service = new RepositoryService();
        service.getDendrograms(
            this.props.match.params.codebaseName,
            [
                "name",
                "profiles",
                "linkageType",
                "tracesMaxlimit",
                "typeOfTraces",
                "accessMetricWeight",
                "writeMetricWeight",
                "readMetricWeight",
                "sequenceMetricWeight"
            ]
        ).then((response) => {
            if (response.data !== null) {
                this.setState({
                    dendrograms: response.data,
                });
            }
        });
    }

    loadDecompositions() {
        const service = new RepositoryService();
        service.getCodebaseDecompositions(
            this.props.match.params.codebaseName,
            [
                "name",
                "dendrogramName",
                "clusters",
                "tracesMaxlimit",
                "silhouetteScore",
                "cohesion",
                "coupling",
                "complexity",
                "performance"
            ]
        ).then((response) => {
            if (response.data !== null) {
                this.setState({
                    allDecompositions: response.data,
                });
            }
        });
    }

    handleSubmit(event) {
        event.preventDefault()

        this.setState({
            isUploaded: "Uploading..."
        });

        const {
            newDendrogramName,
            linkageType,
            accessMetricWeight,
            writeMetricWeight,
            readMetricWeight,
            sequenceMetricWeight,
            selectedProfile,
            amountOfTraces,
            typeOfTraces,
        } = this.state;

        const service = new RepositoryService();
        service.createDendrogram(
            this.props.match.params.codebaseName,
            newDendrogramName,
            linkageType,
            Number(accessMetricWeight),
            Number(writeMetricWeight),
            Number(readMetricWeight),
            Number(sequenceMetricWeight),
            selectedProfile,
            Number(amountOfTraces),
            typeOfTraces,
        )
            .then(response => {
                if (response.status === HttpStatus.CREATED) {
                    this.loadDendrograms();
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
        console.log(event.target.id)
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

    handleChangeAmountOfTraces(event) {
        this.setState({
            amountOfTraces: event.target.value
        });
    }

    handleChangeTypeOfTraces(event) {
        this.setState({
            typeOfTraces: event.target.value
        });
    }

    selectProfile(profile) {
        if (this.state.selectedProfile !== profile) {
            this.setState({
                selectedProfile: profile
            });
        } else {
            this.setState({
                selectedProfile: "",
            });
        }
    }

    handleDeleteDendrogram(dendrogramName) {
        const service = new RepositoryService();
        
        service.deleteDendrogram(
            this.props.match.params.codebaseName,
            dendrogramName
        )
        .then(response => {
            this.loadDendrograms();
            this.loadDecompositions();
        });
    }

    renderBreadCrumbs = () => {
        return (
            <Breadcrumb>
                <Breadcrumb.Item href="/">Home</Breadcrumb.Item>
                <Breadcrumb.Item href="/codebases">Codebases</Breadcrumb.Item>
                <Breadcrumb.Item href={`/codebases/${this.props.match.params.codebaseName}`}>{this.props.match.params.codebaseName}</Breadcrumb.Item>
                <Breadcrumb.Item active>Dendrograms</Breadcrumb.Item>
            </Breadcrumb>
        );
    }

    renderCreateDendrogramForm = () => {
        const {
            accessMetricWeight,
            isUploaded,
            linkageType,
            newDendrogramName,
            readMetricWeight,
            selectedProfile,
            sequenceMetricWeight,
            writeMetricWeight,
            amountOfTraces,
            typeOfTraces,
            codebase: {
                profiles,
            },
        } = this.state;

        return (
            <Form onSubmit={this.handleSubmit}>
                <Form.Group as={Row} controlId="newDendrogramName" className="align-items-center">
                    <Form.Label column sm={2}>
                        Dendrogram Name
                    </Form.Label>
                    <Col sm={2}>
                        <FormControl
                            type="text"
                            maxLength="30"
                            placeholder="Dendrogram Name"
                            value={newDendrogramName}
                            onChange={this.handleChangeNewDendrogramName}
                        />
                    </Col>
                </Form.Group>

                <Form.Group as={Row} controlId="selectControllerProfiles" className="align-items-center">
                    <Form.Label column sm={2}>
                        Select Codebase Profiles
                    </Form.Label>
                    <Col sm={2}>
                        <DropdownButton title={'Controller Profiles'}>
                            {Object.keys(profiles).map(profile =>
                                <Dropdown.Item
                                    key={profile}
                                    onSelect={() => this.selectProfile(profile)}
                                    active={selectedProfile === profile}
                                >
                                    {profile}
                                </Dropdown.Item>
                            )}
                        </DropdownButton>
                    </Col>
                </Form.Group>
                <Form.Group as={Row} controlId="amountOfTraces">
                    <Form.Label column sm={2}>
                        Amount of Traces per Controller
                    </Form.Label>
                    <Col sm={2}>
                        <FormControl
                            type="number"
                            value={amountOfTraces}
                            onChange={this.handleChangeAmountOfTraces}

                        />
                        <Form.Text className="text-muted">
                            If no number is inserted, 0 is assumed to be the default value meaning the maximum number of traces
                        </Form.Text>
                    </Col>
                </Form.Group>
                <Form.Group as={Row} className="align-items-center">
                    <Form.Label as="legend" column sm={2}>
                        Type of traces
                    </Form.Label>
                    <Col sm={3} style={{ paddingLeft: 0 }}>
                        <Col sm="auto">
                            <Form.Check
                                onClick={this.handleChangeTypeOfTraces}
                                name="typeOfTraces"
                                label="All"
                                type="radio"
                                id="allTraces"
                                value="ALL"
                                defaultChecked
                            />
                        </Col>
                        <Col sm="auto">
                            <Form.Check
                                onClick={this.handleChangeTypeOfTraces}
                                name="typeOfTraces"
                                label="Longest"
                                type="radio"
                                id="longest"
                                value="LONGEST"
                            />
                        </Col>
                        <Col sm="auto">
                            <Form.Check
                                onClick={this.handleChangeTypeOfTraces}
                                name="typeOfTraces"
                                label="With more different accesses"
                                type="radio"
                                id="withMoreDifferentTraces"
                                value="WITH_MORE_DIFFERENT_ACCESSES"
                            />

                        </Col>
                        <Col sm="auto">
                            <Form.Check
                                onClick={this.handleChangeTypeOfTraces}
                                name="typeOfTraces"
                                label="Representative (set of accesses)"
                                type="radio"
                                id="representativeSetOfAccesses"
                                value="REPRESENTATIVE"
                            />
                        </Col>
                        {/* WIP */}
                        <Col sm="auto">
                            <Form.Check
                                onClick={undefined}
                                name="typeOfTraces"
                                label="Representative (subsequence of accesses)"
                                type="radio"
                                id="complete"
                                value="?"
                                disabled
                            />
                        </Col>
                    </Col>
                </Form.Group>
                <Form.Group as={Row} className="align-items-center">
                    <Form.Label as="legend" column sm={2}>
                        Linkage Type
                    </Form.Label>
                    <Col sm="auto">
                        <Form.Check
                            onClick={this.handleLinkageType}
                            name="linkageType"
                            label="Average"
                            type="radio"
                            id="average"
                            defaultChecked
                        />
                    </Col>
                    <Col sm="auto">
                        <Form.Check
                            onClick={this.handleLinkageType}
                            name="linkageType"
                            label="Single"
                            type="radio"
                            id="single"
                        />

                    </Col>
                    <Col sm="auto">
                        <Form.Check
                            onClick={this.handleLinkageType}
                            name="linkageType"
                            label="Complete"
                            type="radio"
                            id="complete"
                        />
                    </Col>
                </Form.Group>

                <Form.Group as={Row} controlId="access" className="align-items-center">
                    <Form.Label column sm={2}>
                        Access Metric Weight (%)
                    </Form.Label>
                    <Col sm={2}>
                        <FormControl
                            type="number"
                            placeholder="0-100"
                            value={accessMetricWeight}
                            onChange={this.handleChangeAccessMetricWeight} />
                    </Col>
                </Form.Group>

                <Form.Group as={Row} controlId="write" className="align-items-center">
                    <Form.Label column sm={2}>
                        Write Metric Weight (%)
                    </Form.Label>
                    <Col sm={2}>
                        <FormControl
                            type="number"
                            placeholder="0-100"
                            value={writeMetricWeight}
                            onChange={this.handleChangeWriteMetricWeight} />
                    </Col>
                </Form.Group>

                <Form.Group as={Row} controlId="read" className="align-items-center">
                    <Form.Label column sm={2}>
                        Read Metric Weight (%)
                    </Form.Label>
                    <Col sm={2}>
                        <FormControl
                            type="number"
                            placeholder="0-100"
                            value={readMetricWeight}
                            onChange={this.handleChangeReadMetricWeight} />
                    </Col>
                </Form.Group>

                <Form.Group as={Row} controlId="sequence" className="align-items-center">
                    <Form.Label column sm={2}>
                        Sequence Metric Weight (%)
                    </Form.Label>
                    <Col sm={2}>
                        <FormControl
                            type="number"
                            placeholder="0-100"
                            value={sequenceMetricWeight}
                            onChange={this.handleChangeSequenceMetricWeight} />
                    </Col>
                </Form.Group>

                <Form.Group as={Row} className="align-items-center">
                    <Col sm={{ offset: 2 }}>
                        <Button
                            type="submit"
                            disabled={
                                isUploaded === "Uploading..." ||
                                newDendrogramName === "" ||
                                linkageType === "" ||
                                accessMetricWeight === "" ||
                                writeMetricWeight === "" ||
                                readMetricWeight === "" ||
                                sequenceMetricWeight === "" ||
                                Number(accessMetricWeight) + Number(writeMetricWeight) + Number(readMetricWeight) + Number(sequenceMetricWeight) !== 100 ||
                                selectedProfile === "" ||
                                (typeOfTraces === "" || amountOfTraces === "")
                            }
                        >
                            Create Dendrogram
                        </Button>
                        <Form.Text>
                            {isUploaded}
                        </Form.Text>
                    </Col>
                </Form.Group>
            </Form>
        );
    }

    renderDendrograms = () => {
        const { codebaseName } = this.props.match.params;
        return (
            <Row>
                {
                    this.state.dendrograms.map(dendrogram =>
                        <Col key={dendrogram.name} md="auto">
                            <Card className="mb-4" style={{ width: '20rem' }}>
                                <Card.Img
                                    variant="top"
                                    src={URL + "codebase/" + codebaseName + "/dendrogram/" + dendrogram.name + "/image?" + new Date().getTime()}
                                />
                                <Card.Body>
                                    <Card.Title>{dendrogram.name}</Card.Title>
                                    <Card.Text>
                                        Linkage Type: {dendrogram.linkageType}< br />
                                        AmountOfTraces: {dendrogram.tracesMaxLimit} <br />
                                        Type of traces: {dendrogram.typeOfTraces} <br />
                                        Access: {dendrogram.accessMetricWeight}%< br />
                                        Write: {dendrogram.writeMetricWeight}%< br />
                                        Read: {dendrogram.readMetricWeight}%< br />
                                        Sequence: {dendrogram.sequenceMetricWeight}%
                                    </Card.Text>
                                    <Button href={`/codebases/${codebaseName}/dendrograms/${dendrogram.name}`}
                                        className="mb-2">
                                        Go to Dendrogram
                                    </Button>
                                    <br />
                                    <Button 
                                        onClick={() => this.handleDeleteDendrogram(dendrogram.name)}
                                        variant="danger"
                                    >
                                        Delete
                                    </Button>
                                </Card.Body>
                            </Card>
                            <br />
                        </Col>
                    )
                }
            </Row>
        );
    }

    render() {
        const metricRows = this.state.allDecompositions.map(decomposition => {
            
            let amountOfSingletonClusters = 0;
            let maxClusterSize = 0;

            Object.values(decomposition.clusters).forEach(c => {
                const numberOfEntities = c.entities.length;

                if (numberOfEntities === 1) amountOfSingletonClusters++;

                if (numberOfEntities > maxClusterSize) maxClusterSize = numberOfEntities;
            })

            return {
                id: decomposition.dendrogramName + decomposition.name,
                dendrogram: decomposition.dendrogramName,
                decomposition: decomposition.name,
                clusters: Object.keys(decomposition.clusters).length,
                singleton: amountOfSingletonClusters,
                max_cluster_size: maxClusterSize,
                ss: decomposition.silhouetteScore,
                cohesion: decomposition.cohesion,
                coupling: decomposition.coupling,
                complexity: decomposition.complexity,
                performance: decomposition.performance
            }
        });

        return (
            <div>
                {this.renderBreadCrumbs()}

                <h4 style={{ color: "#666666" }}>
                    Create Dendrogram
                </h4>

                {this.renderCreateDendrogramForm()}

                <h4 style={{ color: "#666666" }}>
                    Dendrograms
                </h4>

                {this.renderDendrograms()}

                <h4 style={{ color: "#666666" }}>
                    Metrics
                </h4>

                {
                    this.state.allDecompositions.length > 0 &&
                    <BootstrapTable bootstrap4 keyField='id' data={metricRows} columns={metricColumns} />
                }
            </div>
        )
    }
}