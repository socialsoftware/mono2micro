import React, {Fragment, useEffect, useState} from "react";
import Form from "react-bootstrap/Form";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import DropdownButton from "react-bootstrap/DropdownButton";
import Dropdown from "react-bootstrap/Dropdown";
import FormControl from "react-bootstrap/FormControl";
import Button from "react-bootstrap/Button";
import Card from "react-bootstrap/Card";
import {URL} from "../../constants/constants";
import BootstrapTable from "react-bootstrap-table-next";
import {RepositoryService} from "../../services/RepositoryService";
import {TraceType} from "../../type-declarations/types.d";


export const AccessesScipyDendrogram = ({codebaseName, profiles, isUploaded, update, setRequest}) => {

    const [dendrograms, setDendrograms] = useState([]);
    const [allDecompositions, setAllDecompositions] = useState([]);
    const [selectedProfile, setSelectedProfile] = useState("");
    const [newDendrogramName, setNewDendrogramName] = useState("");
    const [linkageType, setLinkageType] = useState("average");
    const [accessMetricWeight, setAccessMetricWeight] = useState("25");
    const [writeMetricWeight, setWriteMetricWeight] = useState("25");
    const [readMetricWeight, setReadMetricWeight] = useState("25");
    const [sequenceMetricWeight, setSequenceMetricWeight] = useState("25");
    const [amountOfTraces, setAmountOfTraces] = useState("0");
    const [traceType, setTraceType] = useState(TraceType.ALL);

    // Executes it is informed that there is information to be updated
    useEffect(() => {
        loadDendrograms();
        loadDecompositions();
    }, [update])

    // Updates the request for dendrogram creation
    useEffect(() => {
        setRequest({
            codebaseName,
            name: newDendrogramName,
            accessMetricWeight: Number(accessMetricWeight),
            writeMetricWeight: Number(writeMetricWeight),
            readMetricWeight: Number(readMetricWeight),
            sequenceMetricWeight: Number(sequenceMetricWeight),
            profile: selectedProfile,
            linkageType,
            tracesMaxLimit: Number(amountOfTraces),
            traceType,
            similarityGeneratorType: "ACCESSES_LOG",
            clusteringAlgorithmType: "SCIPY"
        });}, [newDendrogramName, linkageType, accessMetricWeight, writeMetricWeight, readMetricWeight, sequenceMetricWeight, selectedProfile, amountOfTraces, traceType]);

    function loadDendrograms() {
        const service = new RepositoryService();
        service.getDendrograms(
            codebaseName,
            [
                "name",
                "profiles",
                "linkageType",
                "tracesMaxlimit",
                "traceType",
                "accessMetricWeight",
                "writeMetricWeight",
                "readMetricWeight",
                "sequenceMetricWeight"
            ]
        ).then((response) => {
            if (response.data !== null) {
                setDendrograms(response.data);
            }
        });
    }

    function loadDecompositions() {
        const service = new RepositoryService();
        service.getCodebaseDecompositions(
            codebaseName,
            [
                "name",
                "nextClusterID",
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
                setAllDecompositions(response.data);
            }
        });
    }


    const metricColumns = [
        { dataField: 'dendrogram',        text: 'Dendrogram',                    sort: true },
        { dataField: 'decomposition',     text: 'Decomposition',                 sort: true },
        { dataField: 'clusters',          text: 'Number of Retrieved Clusters',  sort: true },
        { dataField: 'singleton',         text: 'Number of Singleton Clusters',  sort: true },
        { dataField: 'max_cluster_size',  text: 'Maximum Cluster Size',          sort: true },
        { dataField: 'ss',                text: 'Silhouette Score',              sort: true },
        { dataField: 'cohesion',          text: 'Cohesion',                      sort: true },
        { dataField: 'coupling',          text: 'Coupling',                      sort: true },
        { dataField: 'complexity',        text: 'Complexity',                    sort: true },
        { dataField: 'performance',       text: 'Performance',                   sort: true }
    ];

    const metricRows = allDecompositions.map(decomposition => {
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


    function handleChangeNewDendrogramName(event) {
        setNewDendrogramName(event.target.value);
    }

    function handleLinkageType(event) {
        setLinkageType(event.target.id);
    }

    function handleChangeAccessMetricWeight(event) {
        setAccessMetricWeight(event.target.value);
    }

    function handleChangeWriteMetricWeight(event) {
        setWriteMetricWeight(event.target.value);
    }

    function handleChangeReadMetricWeight(event) {
        setReadMetricWeight(event.target.value);
    }

    function handleChangeSequenceMetricWeight(event) {
        setSequenceMetricWeight(event.target.value);
    }

    function handleChangeAmountOfTraces(event) {
        setAmountOfTraces(event.target.value);
    }

    function handleChangeTraceType(event) {
        setTraceType(event.target.value);
    }

    function handleSelectProfile(profile) {
        if (selectedProfile !== profile) {
            setSelectedProfile(profile);
        } else {
            setSelectedProfile("");
        }
    }

    function handleDeleteDendrogram(dendrogramName) {
        const service = new RepositoryService();

        service.deleteDendrogram(
            codebaseName,
            dendrogramName
        )
            .then(() => {
                loadDendrograms();
                loadDecompositions();
            });
    }


    return (
        <Fragment>
            <h4 className="mb-3 mt-3" style={{ color: "#666666" }}>
                Dendrogram
            </h4>
            <Form.Group as={Row} controlId="newDendrogramName" className="align-items-center mb-3">
                <Form.Label column sm={2}>
                    Dendrogram Name
                </Form.Label>
                <Col sm={2}>
                    <FormControl
                        type="text"
                        maxLength="30"
                        placeholder="Dendrogram Name"
                        value={newDendrogramName}
                        onChange={handleChangeNewDendrogramName}
                    />
                </Col>
            </Form.Group>
            <Form.Group as={Row} controlId="selectControllerProfiles" className="align-items-center mb-3">
                <Form.Label column sm={2}>
                    Select Codebase Profiles
                </Form.Label>
                <Col sm={2}>
                    <DropdownButton title={'Controller Profiles'}>
                        {Object.keys(profiles).map(profile =>
                            <Dropdown.Item
                                key={profile}
                                onClick={() => handleSelectProfile(profile)}
                                active={selectedProfile === profile}
                            >
                                {profile}
                            </Dropdown.Item>
                        )}
                    </DropdownButton>
                </Col>
            </Form.Group>
            <Form.Group as={Row} controlId="amountOfTraces" className="mb-3">
                <Form.Label column sm={2}>
                    Amount of Traces per Controller
                </Form.Label>
                <Col sm={2}>
                    <FormControl
                        type="number"
                        value={amountOfTraces}
                        onChange={handleChangeAmountOfTraces}

                    />
                    <Form.Text className="text-muted">
                        If no number is inserted, 0 is assumed to be the default value meaning the maximum number of traces
                    </Form.Text>
                </Col>
            </Form.Group>
            <Form.Group as={Row} className="align-items-center mb-3">
                <Form.Label as="legend" column sm={2}>
                    Type of traces
                </Form.Label>
                <Col sm={3} style={{ paddingLeft: 0 }}>
                    <Col sm="auto">
                        <Form.Check
                            onClick={handleChangeTraceType}
                            name="traceType"
                            label="All"
                            type="radio"
                            id="allTraces"
                            value="ALL"
                            defaultChecked
                        />
                    </Col>
                    <Col sm="auto">
                        <Form.Check
                            onClick={handleChangeTraceType}
                            name="traceType"
                            label="Longest"
                            type="radio"
                            id="longest"
                            value="LONGEST"
                        />
                    </Col>
                    <Col sm="auto">
                        <Form.Check
                            onClick={handleChangeTraceType}
                            name="traceType"
                            label="With more different accesses"
                            type="radio"
                            id="withMoreDifferentTraces"
                            value="WITH_MORE_DIFFERENT_ACCESSES"
                        />

                    </Col>
                    <Col sm="auto">
                        <Form.Check
                            onClick={handleChangeTraceType}
                            name="traceType"
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
                            name="traceType"
                            label="Representative (subsequence of accesses)"
                            type="radio"
                            id="complete"
                            value="?"
                            disabled
                        />
                    </Col>
                </Col>
            </Form.Group>
            <Form.Group as={Row} className="align-items-center mb-3">
                <Form.Label as="legend" column sm={2}>
                    Linkage Type
                </Form.Label>
                <Col sm="auto">
                    <Form.Check
                        onClick={handleLinkageType}
                        name="linkageType"
                        label="Average"
                        type="radio"
                        id="average"
                        defaultChecked
                    />
                </Col>
                <Col sm="auto">
                    <Form.Check
                        onClick={handleLinkageType}
                        name="linkageType"
                        label="Single"
                        type="radio"
                        id="single"
                    />

                </Col>
                <Col sm="auto">
                    <Form.Check
                        onClick={handleLinkageType}
                        name="linkageType"
                        label="Complete"
                        type="radio"
                        id="complete"
                    />
                </Col>
            </Form.Group>

            <Form.Group as={Row} controlId="access" className="align-items-center mb-3">
                <Form.Label column sm={2}>
                    Access Metric Weight (%)
                </Form.Label>
                <Col sm={2}>
                    <FormControl
                        type="number"
                        placeholder="0-100"
                        value={accessMetricWeight}
                        onChange={handleChangeAccessMetricWeight} />
                </Col>
            </Form.Group>

            <Form.Group as={Row} controlId="write" className="align-items-center mb-3">
                <Form.Label column sm={2}>
                    Write Metric Weight (%)
                </Form.Label>
                <Col sm={2}>
                    <FormControl
                        type="number"
                        placeholder="0-100"
                        value={writeMetricWeight}
                        onChange={handleChangeWriteMetricWeight} />
                </Col>
            </Form.Group>

            <Form.Group as={Row} controlId="read" className="align-items-center mb-3">
                <Form.Label column sm={2}>
                    Read Metric Weight (%)
                </Form.Label>
                <Col sm={2}>
                    <FormControl
                        type="number"
                        placeholder="0-100"
                        value={readMetricWeight}
                        onChange={handleChangeReadMetricWeight} />
                </Col>
            </Form.Group>

            <Form.Group as={Row} controlId="sequence" className="align-items-center mb-3">
                <Form.Label column sm={2}>
                    Sequence Metric Weight (%)
                </Form.Label>
                <Col sm={2}>
                    <FormControl
                        type="number"
                        placeholder="0-100"
                        value={sequenceMetricWeight}
                        onChange={handleChangeSequenceMetricWeight} />
                </Col>
            </Form.Group>

            <Form.Group as={Row} className="align-items-center">
                <Col sm={{ offset: 2 }}>
                    <Button
                        type="submit"
                        disabled={ isUploaded === "Uploading..." ||
                            newDendrogramName === "" ||
                            linkageType === "" ||
                            accessMetricWeight === "" ||
                            writeMetricWeight === "" ||
                            readMetricWeight === "" ||
                            sequenceMetricWeight === "" ||
                            Number(accessMetricWeight) + Number(writeMetricWeight) + Number(readMetricWeight) + Number(sequenceMetricWeight) !== 100 ||
                            selectedProfile === "" ||
                            (traceType === "" || amountOfTraces === "")
                        }
                    >
                        Create Dendrogram
                    </Button>
                    <Form.Text className="ms-2">
                        {isUploaded}
                    </Form.Text>
                </Col>
            </Form.Group>
            <Row>
                <h4 style={{ color: "#666666" }}>
                    Dendrograms
                </h4>
                {dendrograms.map(d =>
                    <Col key={d.name} md="auto">
                        <Card className="mb-4" style={{ width: '20rem' }}>
                            <Card.Img
                                variant="top"
                                src={URL + "codebase/" + codebaseName + "/dendrogram/" + d.name + "/image?" + new Date().getTime()}
                            />
                            <Card.Body>
                                <Card.Title>{d.name}</Card.Title>
                                <Card.Text>
                                    Linkage Type: {d.linkageType}< br />
                                    AmountOfTraces: {d.tracesMaxLimit} <br />
                                    Type of traces: {d.traceType} <br />
                                    Access: {d.accessMetricWeight}%< br />
                                    Write: {d.writeMetricWeight}%< br />
                                    Read: {d.readMetricWeight}%< br />
                                    Sequence: {d.sequenceMetricWeight}%
                                </Card.Text>
                                <Button href={`/codebases/${codebaseName}/dendrograms/${d.name}`}
                                        className="mb-2">
                                    Go to Dendrogram
                                </Button>
                                <br />
                                <Button
                                    onClick={() => handleDeleteDendrogram(d.name)}
                                    variant="danger"
                                >
                                    Delete
                                </Button>
                            </Card.Body>
                        </Card>
                        <br />
                    </Col>
                )}

                <h4 style={{ color: "#666666" }}>
                    Metrics
                </h4>

                { allDecompositions.length > 0 && <BootstrapTable bootstrap4 keyField='id' data={metricRows} columns={metricColumns} /> }
            </Row>
        </Fragment>
    );
}