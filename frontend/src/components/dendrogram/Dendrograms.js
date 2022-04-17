import React, {useEffect, useState} from 'react';
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
import {useParams} from "react-router-dom";

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
    const [dendrogramBase, setDendrogramBase] = useState("COMMIT");
    const [allDecompositions, setAllDecompositions] = useState([]);
    const [selectedProfile, setSelectedProfile] = useState("");
    const [isUploaded, setIsUploaded] = useState("");
    const [newDendrogramName, setNewDendrogramName] = useState("");
    const [linkageType, setLinkageType] = useState("average");
    const [accessMetricWeight, setAccessMetricWeight] = useState("25");
    const [writeMetricWeight, setWriteMetricWeight] = useState("25");
    const [readMetricWeight, setReadMetricWeight] = useState("25");
    const [sequenceMetricWeight, setSequenceMetricWeight] = useState("25");
    const [commitMetricWeight, setCommitMetricWeight] = useState("50");
    const [authorMetricWeight, setAuthorMetricWeight] = useState("50");
    const [amountOfTraces, setAmountOfTraces] = useState("0");
    const [typeOfTraces, setTypeOfTraces] = useState("ALL");
    const [codebase, setCodebase] = useState({ profiles: [], });

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
            ["profiles"]
        ).then(response => {
            if (response.data !== null) {
                setCodebase(response.data);
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
                "base",
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
            dendrogramBase,
            linkageType,
            Number(accessMetricWeight),
            Number(writeMetricWeight),
            Number(readMetricWeight),
            Number(sequenceMetricWeight),
            Number(commitMetricWeight),
            Number(authorMetricWeight),
            selectedProfile,
            Number(amountOfTraces),
            typeOfTraces,
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

    function handleChangeDendrogramBase(event) {
        setDendrogramBase(event.target.value);
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

    function handleChangeCommitMetricWeight(event) {
        setCommitMetricWeight(event.target.value);
    }

    function handleChangeAuthorMetricWeight(event) {
        setAuthorMetricWeight(event.target.value);
    }

    function handleChangeAmountOfTraces(event) {
        setAmountOfTraces(event.target.value);
    }

    function handleChangeTypeOfTraces(event) {
        setTypeOfTraces(event.target.value);
    }

    function selectProfile(profile) {
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

    function renderCreateDendrogramForm() {
        const profiles = codebase["profiles"];
        return (
            <Form onSubmit={handleSubmit}>
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

                <Form.Group as={Row} controlId="dendrogramBase" className="align-items-center mb-3">
                    <Form.Label column sm={2}>
                        Data source for dendrogram
                    </Form.Label>
                    <Col sm={3} style={{ paddingLeft: 0 }}>
                        <Col sm="auto">
                            <Form.Check
                                onClick={handleChangeDendrogramBase}
                                name="dendrogram-base"
                                label="Commit"
                                type="radio"
                                id="commit"
                                value="COMMIT"
                                defaultChecked
                            />
                        </Col>
                        <Col sm="auto">
                            <Form.Check
                                onClick={handleChangeDendrogramBase}
                                name="dendrogram-base"
                                label="Static"
                                type="radio"
                                id="static"
                                value="STATIC"
                            />
                        </Col>
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
                            disabled={dendrogramBase == "COMMIT"}

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
                                onClick={handleChangeTypeOfTraces}
                                name="typeOfTraces"
                                label="All"
                                type="radio"
                                id="allTraces"
                                value="ALL"
                                defaultChecked
                                disabled={dendrogramBase == "COMMIT"}
                            />
                        </Col>
                        <Col sm="auto">
                            <Form.Check
                                onClick={handleChangeTypeOfTraces}
                                name="typeOfTraces"
                                label="Longest"
                                type="radio"
                                id="longest"
                                value="LONGEST"
                                disabled={dendrogramBase == "COMMIT"}
                            />
                        </Col>
                        <Col sm="auto">
                            <Form.Check
                                onClick={handleChangeTypeOfTraces}
                                name="typeOfTraces"
                                label="With more different accesses"
                                type="radio"
                                id="withMoreDifferentTraces"
                                value="WITH_MORE_DIFFERENT_ACCESSES"
                                disabled={dendrogramBase == "COMMIT"}
                            />

                        </Col>
                        <Col sm="auto">
                            <Form.Check
                                onClick={handleChangeTypeOfTraces}
                                name="typeOfTraces"
                                label="Representative (set of accesses)"
                                type="radio"
                                id="representativeSetOfAccesses"
                                value="REPRESENTATIVE"
                                disabled={dendrogramBase == "COMMIT"}
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
                <div style={dendrogramBase=="COMMIT" ? {display: 'none'} : {}}>
                    <Form.Group as={Row} controlId="access" className="align-items-center mb-3">
                        <Form.Label column sm={2}>
                            Access Metric Weight (%)
                        </Form.Label>
                        <Col sm={2}>
                            <FormControl
                                type="number"
                                placeholder="0-100"
                                value={accessMetricWeight}
                                onChange={handleChangeAccessMetricWeight}
                                disabled={dendrogramBase == "COMMIT"} />
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
                                onChange={handleChangeWriteMetricWeight}
                                disabled={dendrogramBase == "COMMIT"} />
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
                                onChange={handleChangeReadMetricWeight}
                                disabled={dendrogramBase == "COMMIT"} />
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
                                onChange={handleChangeSequenceMetricWeight}
                                disabled={dendrogramBase == "COMMIT"} />
                        </Col>
                    </Form.Group>
                </div>

                <div style={dendrogramBase=="STATIC" ? {display: 'none'} : {}}>
                    <Form.Group as={Row} controlId="commit" className="align-items-center mb-3">
                        <Form.Label column sm={2}>
                            Commit Metric Weight (%)
                        </Form.Label>
                        <Col sm={2}>
                            <FormControl
                                type="number"
                                placeholder="0-100"
                                value={commitMetricWeight}
                                onChange={handleChangeCommitMetricWeight}/>
                        </Col>
                    </Form.Group>

                    <Form.Group as={Row} controlId="author" className="align-items-center mb-3">
                        <Form.Label column sm={2}>
                            Author Metric Weight (%)
                        </Form.Label>
                        <Col sm={2}>
                            <FormControl
                                type="number"
                                placeholder="0-100"
                                value={authorMetricWeight}
                                onChange={handleChangeAuthorMetricWeight}/>
                        </Col>
                    </Form.Group>
                </div>

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
                        <Form.Text className="ms-2">
                            {isUploaded}
                        </Form.Text>
                    </Col>
                </Form.Group>
            </Form>

        );
    }

    function renderDendrograms() {
        return (
            <Row>
                {
                    dendrograms.map(dendrogram =>
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
                                        onClick={() => handleDeleteDendrogram(dendrogram.name)}
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

            <h4 style={{ color: "#666666" }}>
                Create Dendrogram
            </h4>

            {renderCreateDendrogramForm()}

            <h4 style={{ color: "#666666" }}>
                Dendrograms
            </h4>

            {renderDendrograms()}

            <h4 style={{ color: "#666666" }}>
                Metrics
            </h4>

            {
                allDecompositions.length > 0 &&
                <BootstrapTable bootstrap4 keyField='id' data={metricRows} columns={metricColumns} />
            }
        </div>
    )
}