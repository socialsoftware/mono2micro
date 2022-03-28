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
    const [typeOfTraces, setTypeOfTraces] = useState("ALL");
    const [codebase, setCodebase] = useState({ profiles: [], });
    const [analysisType, setAnalysisType] = useState("static");
    const [featureVectorizationStrategy, setFeatureVectorizationStrategy] = useState("methodCalls");
    const [maxDepth, setMaxDepth] = useState("0");
    const [controllersWeight, setControllersWeight] = useState("100");
    const [servicesWeight, setServicesWeight] = useState("100");
    const [intermediateMethodsWeight, setIntermediateMethodsWeight] = useState("100");
    const [entitiesWeight, setEntitiesWeight] = useState("100");
    const [constructorWeight, setConstructorWeight] = useState("100");
    const [gettersWeight, setGettersWeight] = useState("100");
    const [settersWeight, setSettersWeight] = useState("100");
    const [regularMethodsWeight, setRegularMethodsWeight] = useState("100");
    const [methodsWeight, setMethodsWeight] = useState("100");

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
                "linkageType",
                "analysisType",
                "tracesMaxlimit",
                "typeOfTraces",
                "accessMetricWeight",
                "writeMetricWeight",
                "readMetricWeight",
                "sequenceMetricWeight",
                "featureVectorizationStrategy",
                "maxDepth",
                "controllersWeight",
                "servicesWeight",
                "intermediateMethodsWeight",
                "entitiesWeight",
                "constructorWeight",
                "gettersWeight",
                "settersWeight",
                "regularMethodsWeight",
                "methodsWeight"
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
            analysisType,
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

    function handleCreateDendrogramByFeaturesSubmit(event) {
        event.preventDefault()
        setIsUploaded("Uploading...");
        const service = new RepositoryService();
        service.createDendrogramByFeatures(
            codebaseName,
            newDendrogramName,
            selectedProfile,
            linkageType,
            analysisType,
            featureVectorizationStrategy,
            Number(maxDepth),
            Number(servicesWeight),
            Number(controllersWeight),
            Number(intermediateMethodsWeight),
            Number(entitiesWeight),
            Number(constructorWeight),
            Number(gettersWeight),
            Number(settersWeight),
            Number(regularMethodsWeight),
            Number(methodsWeight)
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

    function handleCreateDendrogramByClassesSubmit(event) {
        event.preventDefault()
        setIsUploaded("Uploading...");
        const service = new RepositoryService();
        service.createDendrogramByClass(
            codebaseName,
            newDendrogramName,
            selectedProfile,
            linkageType,
            analysisType
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

    function handleAnalysisType(event) {
        setAnalysisType(event.target.id);
    }

    function handleChangeFeatureVectorizationStrategy(event) {
        setFeatureVectorizationStrategy(event.target.id);
    }

    function handleChangeMaxDepth(event) {
        setMaxDepth(event.target.value);
    }

    function handleChangeControllersWeight(event) {
        setControllersWeight(event.target.value);
    }

    function handleChangeServicesWeight(event) {
        setServicesWeight(event.target.value);
    }

    function handleChangeIntermediateMethodsWeight(event) {
        setIntermediateMethodsWeight(event.target.value);
    }

    function handleChangeEntitiesWeight(event) {
        setEntitiesWeight(event.target.value);
    }

    function handleChangeConstructorWeight(event) {
        setConstructorWeight(event.target.value);
    }

    function handleChangeGettersWeight(event) {
        setGettersWeight(event.target.value);
    }

    function handleChangeSettersWeight(event) {
        setSettersWeight(event.target.value);
    }

    function handleChangeRegularMethodsWeight(event) {
        setRegularMethodsWeight(event.target.value);
    }

    function handleChangeMethodsWeight(event) {
        setMethodsWeight(event.target.value);
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

    function renderAnalysisType() {
        return (
            <Form.Group as={Row} className="align-items-center">
                <Form.Label as="legend" column sm={2}>
                    Analysis Type
                </Form.Label>
                <Col sm="auto">
                    <Form.Check
                        onClick={handleAnalysisType}
                        name="analysisType"
                        label="Static"
                        type="radio"
                        id="static"
                        defaultChecked
                    />
                </Col>
                <Col sm="auto">
                    <Form.Check
                        onClick={handleAnalysisType}
                        name="analysisType"
                        label="Feature Aggregation"
                        type="radio"
                        id="feature"
                    />
                </Col>
                <Col sm="auto">
                    <Form.Check
                        onClick={handleAnalysisType}
                        name="analysisType"
                        label="Class Aggregation"
                        type="radio"
                        id="class"
                    />
                </Col>
            </Form.Group>
        );
    }

    function renderCreateStaticDendrogramForm() {
        const profiles = codebase["profiles"];
        return (
            <Form onSubmit={handleSubmit}>
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
                            onChange={handleChangeNewDendrogramName}
                        />
                    </Col>
                </Form.Group>

                <br/>

                <Form.Group as={Row} controlId="selectControllerProfiles" className="align-items-center">
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

                <br/>

                <Form.Group as={Row} controlId="amountOfTraces">
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

                <br/>

                <Form.Group as={Row} className="align-items-center">
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

                <br/>

                <Form.Group as={Row} className="align-items-center">
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

                <br/>

                <Form.Group as={Row} controlId="access" className="align-items-center">
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

                <br/>

                <Form.Group as={Row} controlId="write" className="align-items-center">
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

                <br/>

                <Form.Group as={Row} controlId="read" className="align-items-center">
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

                <br/>

                <Form.Group as={Row} controlId="sequence" className="align-items-center">
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

                <br/>

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

    function renderCreateFeatureDendrogramForm() {
        const profiles = codebase["profiles"];
        return (
            <Form onSubmit={handleCreateDendrogramByFeaturesSubmit}>
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
                            onChange={handleChangeNewDendrogramName}
                        />
                    </Col>
                </Form.Group>

                <br/>

                <Form.Group as={Row} controlId="selectControllerProfiles" className="align-items-center">
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

                <br/>

                <Form.Group as={Row} className="align-items-center">
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

                <br/>

                <Form.Group as={Row} controlId="selectFeatureVectorizationStrategy" className="align-items-center">
                    <Form.Label column sm={2}>
                        Select Feature Vectorization Strategy
                    </Form.Label>
                    <Col sm="auto">
                        <Form.Check
                            onClick={handleChangeFeatureVectorizationStrategy}
                            name="featureVectorizationStrategy"
                            label="Method Calls"
                            type="radio"
                            id="methodCalls"
                            defaultChecked
                        />
                    </Col>
                    <Col sm="auto">
                        <Form.Check
                            onClick={handleChangeFeatureVectorizationStrategy}
                            name="featureVectorizationStrategy"
                            label="Entities"
                            type="radio"
                            id="entities"
                        />

                    </Col>
                    <Col sm="auto">
                        <Form.Check
                            onClick={handleChangeFeatureVectorizationStrategy}
                            name="featureVectorizationStrategy"
                            label="Mixed"
                            type="radio"
                            id="mixed"
                        />
                    </Col>
                </Form.Group>

                <br/>

                {featureVectorizationStrategy == "methodCalls" ? renderMethodCallsStrategyForm() : <div></div>}
                {featureVectorizationStrategy == "entities" ? renderEntitiesStrategyForm() : <div></div>}
                {featureVectorizationStrategy == "mixed" ? renderMixedStrategyForm() : <div></div>}

                <br/>

                <Form.Group as={Row} className="align-items-center">
                    <Col sm={{ offset: 2 }}>
                        <Button
                            type="submit"
                            disabled={
                                isUploaded === "Uploading..." ||
                                newDendrogramName === "" ||
                                selectedProfile === ""
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

    function renderMethodCallsStrategyForm() {
        // Max depth
        // Service Weight, Auxiliar Weights, Entity weights
        return (
            <div>
                <Form.Group as={Row} controlId="maxDepth">
                    <Form.Label column sm={2}>
                        Maximum depth
                    </Form.Label>
                    <Col sm={2}>
                        <FormControl
                            type="number"
                            value={maxDepth}
                            onChange={handleChangeMaxDepth}
                        />
                    </Col>
                </Form.Group>

                <br/>

                <Form.Group as={Row} controlId="controllersWeight" className="align-items-center">
                    <Form.Label column sm={2}>
                        Controllers Weight (%)
                    </Form.Label>
                    <Col sm={2}>
                        <FormControl
                            type="number"
                            placeholder="0-100"
                            value={controllersWeight}
                            onChange={handleChangeControllersWeight} />
                    </Col>
                </Form.Group>

                <br/>

                <Form.Group as={Row} controlId="servicesWeight" className="align-items-center">
                    <Form.Label column sm={2}>
                        Services Weight (%)
                    </Form.Label>
                    <Col sm={2}>
                        <FormControl
                            type="number"
                            placeholder="0-100"
                            value={servicesWeight}
                            onChange={handleChangeServicesWeight} />
                    </Col>
                </Form.Group>

                <br/>

                <Form.Group as={Row} controlId="intermediateMethodsWeight" className="align-items-center">
                    <Form.Label column sm={2}>
                        Intermediate Methods Weight (%)
                    </Form.Label>
                    <Col sm={2}>
                        <FormControl
                            type="number"
                            placeholder="0-100"
                            value={intermediateMethodsWeight}
                            onChange={handleChangeIntermediateMethodsWeight} />
                    </Col>
                </Form.Group>

                <br/>

                <Form.Group as={Row} controlId="entitiesWeight" className="align-items-center">
                    <Form.Label column sm={2}>
                        Entities Weight (%)
                    </Form.Label>
                    <Col sm={2}>
                        <FormControl
                            type="number"
                            placeholder="0-100"
                            value={entitiesWeight}
                            onChange={handleChangeEntitiesWeight} />
                    </Col>
                </Form.Group>

            </div>
        );
    }

    function renderEntitiesStrategyForm() {
        // Number of accesses already present, since multiple accesses count multiple times
        // Constructor Weights, Getters Weights, Setters Weights, Other Methods Weights
        // Transformers (toString), Recognizers(isSomething) , testers (equals) ???
        return (
            <div>
                <Form.Group as={Row} controlId="constructorWeight" className="align-items-center">
                    <Form.Label column sm={2}>
                        Constructor Weight (%)
                    </Form.Label>
                    <Col sm={2}>
                        <FormControl
                            type="number"
                            placeholder="0-100"
                            value={constructorWeight}
                            onChange={handleChangeConstructorWeight} />
                    </Col>
                </Form.Group>

                <br/>

                <Form.Group as={Row} controlId="gettersWeight" className="align-items-center">
                    <Form.Label column sm={2}>
                        Getters Weight (%)
                    </Form.Label>
                    <Col sm={2}>
                        <FormControl
                            type="number"
                            placeholder="0-100"
                            value={gettersWeight}
                            onChange={handleChangeGettersWeight} />
                    </Col>
                </Form.Group>

                <br/>

                <Form.Group as={Row} controlId="settersWeight" className="align-items-center">
                    <Form.Label column sm={2}>
                        Setters Weight (%)
                    </Form.Label>
                    <Col sm={2}>
                        <FormControl
                            type="number"
                            placeholder="0-100"
                            value={settersWeight}
                            onChange={handleChangeSettersWeight} />
                    </Col>
                </Form.Group>
                
                <br/>

                <Form.Group as={Row} controlId="regularMethodsWeight" className="align-items-center">
                    <Form.Label column sm={2}>
                        Regular Methods Weight (%)
                    </Form.Label>
                    <Col sm={2}>
                        <FormControl
                            type="number"
                            placeholder="0-100"
                            value={regularMethodsWeight}
                            onChange={handleChangeRegularMethodsWeight} />
                    </Col>
                </Form.Group>
            </div>
        );
    }

    function renderMixedStrategyForm() {
        // Equal Weights
        // Entities Weights, Method Calls Weights
        return (
            <div>
                <Form.Group as={Row} controlId="entitiesWeight" className="align-items-center">
                    <Form.Label column sm={2}>
                        Entities Weight (%)
                    </Form.Label>
                    <Col sm={2}>
                        <FormControl
                            type="number"
                            placeholder="0-100"
                            value={entitiesWeight}
                            onChange={handleChangeEntitiesWeight} />
                    </Col>
                </Form.Group>

                <br/>

                <Form.Group as={Row} controlId="methodsWeight" className="align-items-center">
                    <Form.Label column sm={2}>
                        Methods Weight (%)
                    </Form.Label>
                    <Col sm={2}>
                        <FormControl
                            type="number"
                            placeholder="0-100"
                            value={methodsWeight}
                            onChange={handleChangeMethodsWeight} />
                    </Col>
                </Form.Group>
            </div>
        );
    }

    function renderCreateClassDendrogramForm() {
        const profiles = codebase["profiles"];
        return (
            <Form onSubmit={handleCreateDendrogramByClassesSubmit}>
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
                            onChange={handleChangeNewDendrogramName}
                        />
                    </Col>
                </Form.Group>

                <br/>

                <Form.Group as={Row} controlId="selectControllerProfiles" className="align-items-center">
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

                <br/>

                <Form.Group as={Row} className="align-items-center">
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

                <br/>

                <Form.Group as={Row} className="align-items-center">
                    <Col sm={{ offset: 2 }}>
                        <Button
                            type="submit"
                            disabled={
                                isUploaded === "Uploading..." ||
                                newDendrogramName === "" ||
                                selectedProfile === ""
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
                                        
                                        {dendrogram.featureVectorizationStrategy == "methodCalls" ? 
                                        <Card.Text>
                                            Linkage Type: {dendrogram.linkageType}<br/>
                                            AmountOfTraces: {dendrogram.tracesMaxLimit} <br/>
                                            Type of traces: {dendrogram.typeOfTraces} <br/>
                                            Access: {dendrogram.accessMetricWeight}%, Write: {dendrogram.writeMetricWeight}%<br/>
                                            Read: {dendrogram.readMetricWeight}%, Sequence: {dendrogram.sequenceMetricWeight}%<br/>
                                            Strategy: {dendrogram.featureVectorizationStrategy}<br/>
                                            Max Depth: {dendrogram.maxDepth}<br/>
                                            Controllers Weight: {dendrogram.controllersWeight}%<br/>
                                            Services Weight: {dendrogram.servicesWeight}%<br/>
                                            Intermediate Weight: {dendrogram.intermediateMethodsWeight}%<br/>
                                            Entities Weight: {dendrogram.entitiesWeight}%
                                        </Card.Text>
                                        : dendrogram.featureVectorizationStrategy == "methodCalls" ? 
                                        <Card.Text>
                                            Linkage Type: {dendrogram.linkageType}<br/>
                                            AmountOfTraces: {dendrogram.tracesMaxLimit} <br/>
                                            Type of traces: {dendrogram.typeOfTraces} <br/>
                                            Access: {dendrogram.accessMetricWeight}%, Write: {dendrogram.writeMetricWeight}%<br/>
                                            Read: {dendrogram.readMetricWeight}%, Sequence: {dendrogram.sequenceMetricWeight}%<br/>
                                            Strategy: {dendrogram.featureVectorizationStrategy}<br/>
                                            Constructor Weight: {dendrogram.constructorWeight}%<br/>
                                            Getters Weight: {dendrogram.gettersWeight}%<br/>
                                            Setters Weight: {dendrogram.settersWeight}%<br/>
                                            Regular Weight: {dendrogram.regularMethodsWeight}%
                                        </Card.Text>
                                        :
                                        <Card.Text>
                                            Linkage Type: {dendrogram.linkageType}<br/>
                                            AmountOfTraces: {dendrogram.tracesMaxLimit} <br/>
                                            Type of traces: {dendrogram.typeOfTraces} <br/>
                                            Access: {dendrogram.accessMetricWeight}%, Write: {dendrogram.writeMetricWeight}%<br/>
                                            Read: {dendrogram.readMetricWeight}%, Sequence: {dendrogram.sequenceMetricWeight}%<br/>
                                            Strategy: {dendrogram.featureVectorizationStrategy}<br/>
                                            Entities Weight: {dendrogram.entitiesWeight}%<br/>
                                            Methods Weight: {dendrogram.methodsWeight}%
                                        </Card.Text>
                                        }
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
                Choose the Analysis Type
            </h4>

            {renderAnalysisType()}

            <br/>

            <h4 style={{ color: "#666666" }}>
                Create Dendrogram
            </h4>

            {analysisType == "static" ? renderCreateStaticDendrogramForm() : <div></div>}
            {analysisType == "feature" ? renderCreateFeatureDendrogramForm() : <div></div>}
            {analysisType == "class" ? renderCreateClassDendrogramForm() : <div></div>}

            <br/>

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