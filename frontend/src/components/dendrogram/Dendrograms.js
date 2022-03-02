import React, {Fragment, useEffect, useState} from 'react';
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

import {CLUSTERING_ALGORITHMS, POSSIBLE_DECOMPOSITIONS, SIMILARITY_GENERATORS, URL} from '../../constants/constants';
import BootstrapTable from 'react-bootstrap-table-next';
import {useParams} from "react-router-dom";
import {TraceType} from "../../type-declarations/types.d";

const HttpStatus = require('http-status-codes');

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

export const Dendrograms = () => {
    const [dendrograms, setDendrograms] = useState([]);
    const [allDecompositions, setAllDecompositions] = useState([]);
    const [selectedProfile, setSelectedProfile] = useState("");
    const [isUploaded, setIsUploaded] = useState("");
    const [newDendrogramName, setNewDendrogramName] = useState("");
    const [linkageType, setLinkageType] = useState("average");
    const [accessMetricWeight, setAccessMetricWeight] = useState("25");
    const [writeMetricWeight, setWriteMetricWeight] = useState("25");
    const [readMetricWeight, setReadMetricWeight] = useState("25");
    const [sequenceMetricWeight, setSequenceMetricWeight] = useState("25");
    const [amountOfTraces, setAmountOfTraces] = useState("0");
    const [traceType, setTraceType] = useState(TraceType.ALL);
    const [similarityGenerator, setSimilarityGenerator] = useState(undefined);
    const [clusteringAlgorithm, setClusteringAlgorithm] = useState(undefined);
    const [profiles, setProfiles] = useState([]);

    let { codebaseName } = useParams();

    //Executed on mount
    useEffect(() => {
        loadDendrograms();
        loadDecompositions();
        loadCodebase();
    }, []);

    function loadCodebase() {
        const service = new RepositoryService();
        service.getCodebase(
            codebaseName,
            ["profiles", "similarityGeneratorType"]
        ).then(response => {
            if (response.data !== null) {
                setProfiles(response.data.profiles);
                setSimilarityGenerator(SIMILARITY_GENERATORS[response.data.similarityGeneratorType]);
            }
        });
    }

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

    function handleSubmit(event) {
        event.preventDefault()
        setIsUploaded("Uploading...");

        const service = new RepositoryService();
        service.createDendrogram(
            codebaseName,
            newDendrogramName,
            linkageType,
            Number(accessMetricWeight),
            Number(writeMetricWeight),
            Number(readMetricWeight),
            Number(sequenceMetricWeight),
            selectedProfile,
            Number(amountOfTraces),
            traceType,
            similarityGenerator.value,
            clusteringAlgorithm.value
        )
            .then(response => {
                if (response.status === HttpStatus.CREATED) {
                    loadDendrograms();
                    loadDecompositions();
                    setIsUploaded("Upload completed successfully.");
                } else {
                    setIsUploaded("Upload failed.");
                }
            })
            .catch(error => {
                if (error.response !== undefined && error.response.status === HttpStatus.UNAUTHORIZED) {
                    setIsUploaded("Upload failed. Dendrogram name already exists.");
                } else {
                    setIsUploaded("Upload failed.");
                }
            });
    }

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

    function selectProfile(profile) {
        if (selectedProfile !== profile) {
            setSelectedProfile(profile);
        } else {
            setSelectedProfile("");
        }
    }

    function selectSimilarityGenerator(algorithm) {
        setSimilarityGenerator(algorithm);
        setClusteringAlgorithm(undefined);
    }

    function selectClusteringAlgorithm(algorithm) {
        setClusteringAlgorithm(algorithm);
    }

    function handleDeleteDendrogram(dendrogramName) {
        const service = new RepositoryService();
        
        service.deleteDendrogram(
            codebaseName,
            dendrogramName
        )
        .then(response => {
            loadDendrograms();
            loadDecompositions();
        });
    }

    function renderBreadCrumbs() {
        return (
            <Breadcrumb>
                <Breadcrumb.Item href="/">Home</Breadcrumb.Item>
                <Breadcrumb.Item href="/codebases">Codebases</Breadcrumb.Item>
                <Breadcrumb.Item href={`/codebases/${codebaseName}`}>{codebaseName}</Breadcrumb.Item>
                <Breadcrumb.Item active>Dendrograms</Breadcrumb.Item>
            </Breadcrumb>
        );
    }

    function renderClusteringAlgorithm() {
        return (
            <Fragment>
                <h4 className="mb-3 mt-3" style={{ color: "#666666" }}>
                    Clustering Algorithm
                </h4>
                <Form.Group as={Row} controlId="selectClusteringAlgorithm" className="align-items-center mb-3">
                    <Col sm={2}>
                        <DropdownButton title={clusteringAlgorithm === undefined? "Select Clustering Algorithm" : clusteringAlgorithm.name}>
                            {Object.keys(POSSIBLE_DECOMPOSITIONS[similarityGenerator.value])
                                .map(algorithmValue => CLUSTERING_ALGORITHMS[algorithmValue])
                                .map(algorithm =>
                                <Dropdown.Item
                                    key={algorithm.value}
                                    onClick={() => selectClusteringAlgorithm(algorithm)}
                                >
                                    {algorithm.name}
                                </Dropdown.Item>
                            )}
                        </DropdownButton>
                    </Col>
                </Form.Group>
            </Fragment>
        );
    }

    function renderSimilarityGenerator() {
        return (
            <Form.Group as={Row} controlId="selectSimilarityGenerator" className="align-items-center mb-3">
                <h4 className="mb-3 mt-3" style={{ color: "#666666" }}>
                    Similarity Generator
                </h4>
                <Col sm={2}>
                    <DropdownButton title={similarityGenerator === undefined? "Select Similarity Generator" : similarityGenerator.name}>
                        {Object.values(SIMILARITY_GENERATORS)
                            .map(generator =>
                                <Dropdown.Item
                                    key={generator.value}
                                    onClick={() => selectSimilarityGenerator(generator)}
                                >
                                    {generator.name}
                                </Dropdown.Item>
                            )
                        }
                    </DropdownButton>
                </Col>
            </Form.Group>
        );
    }

    function renderCreateDendrogramForm() {
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

                {/*Add render of each similarity generator like the next line to request the required elements for the dendrogram's creation*/}
                { similarityGenerator.value === "ACCESSES_LOG" && renderAccessLogParameters() }

                {/*Add the rules to disable each button in disableDendrogramSubmit*/}
                { renderCreateDendrogramButton() }

                { dendrograms.length !== 0 && renderDendrograms() }
            </Fragment>
        );
    }

    function renderAccessLogParameters() {
        return (
            <Fragment>
                <Form.Group as={Row} controlId="selectControllerProfiles" className="align-items-center mb-3">
                    <Form.Label column sm={2}>
                        Select Codebase Profiles
                    </Form.Label>
                    <Col sm={2}>
                        <DropdownButton title={'Controller Profiles'}>
                            {Object.keys(profiles).map(profile =>
                                <Dropdown.Item
                                    key={profile}
                                    onClick={() => selectProfile(profile)}
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
            </Fragment>
        );
    }

    function renderCreateDendrogramButton() {
        return(
            <Form.Group as={Row} className="align-items-center">
                <Col sm={{ offset: 2 }}>
                    <Button
                        type="submit"
                        disabled={ disableDendrogramSubmit() }
                    >
                        Create Dendrogram
                    </Button>
                    <Form.Text className="ms-2">
                        {isUploaded}
                    </Form.Text>
                </Col>
            </Form.Group>
        );
    }

    function disableDendrogramSubmit() {
        switch (similarityGenerator.value) {
            case "ACCESSES_LOG":
                return isUploaded === "Uploading..." ||
                    newDendrogramName === "" ||
                    linkageType === "" ||
                    accessMetricWeight === "" ||
                    writeMetricWeight === "" ||
                    readMetricWeight === "" ||
                    sequenceMetricWeight === "" ||
                    Number(accessMetricWeight) + Number(writeMetricWeight) + Number(readMetricWeight) + Number(sequenceMetricWeight) !== 100 ||
                    selectedProfile === "" ||
                    (traceType === "" || amountOfTraces === "");
            default:
                return false;

        }
    }

    function renderDendrograms() {
        return (
            <Row>
                <h4 style={{ color: "#666666" }}>
                    Dendrograms
                </h4>
                {dendrograms.map(dendrogram =>
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
                                    Type of traces: {dendrogram.traceType} <br />
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
                                    onClick={() => handleDeleteDendrogram(dendrogram.name)}
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

                { allDecompositions.length > 0 &&
                    <BootstrapTable bootstrap4 keyField='id' data={metricRows} columns={metricColumns} />
                }
            </Row>
        );
    }

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

    return (
        <div>
            {renderBreadCrumbs()}

            <Form onSubmit={handleSubmit} className="mb-3">
                { renderSimilarityGenerator() }

                { similarityGenerator !== undefined && renderClusteringAlgorithm() }

                { clusteringAlgorithm !== undefined && clusteringAlgorithm.hasDendrograms && renderCreateDendrogramForm() }
            </Form>
        </div>
    )
}