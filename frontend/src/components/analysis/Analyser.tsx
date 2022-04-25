import React, {useEffect, useState} from 'react';
import {RepositoryService} from '../../services/RepositoryService';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Form from 'react-bootstrap/Form';
import FormControl from 'react-bootstrap/FormControl';
import DropdownButton from 'react-bootstrap/DropdownButton';
import Dropdown from 'react-bootstrap/Dropdown';
import Button from 'react-bootstrap/Button';
import Breadcrumb from 'react-bootstrap/Breadcrumb';
import BootstrapTable from 'react-bootstrap-table-next';
import filterFactory, {numberFilter} from 'react-bootstrap-table2-filter';
import {Codebase, Decomposition, TraceType} from "../../type-declarations/types.d";

const HttpStatus = require('http-status-codes');

const filter = numberFilter({placeholder: "filter"});
const sort = true;

const metricColumns = [
    {
        dataField: 'access',
        text: 'Access',
        sort,
        filter,
    }, 
    {
        dataField: 'write',
        text: 'Write',
        sort,
        filter,
    }, 
    {
        dataField: 'read',
        text: 'Read',
        sort,
        filter,
    }, 
    {
        dataField: 'sequence',
        text: 'Sequence',
        sort,
        filter,
    },
    {
        dataField: 'numberClusters',
        text: 'Number Clusters',
        sort,
        filter,
    }, 
    {
        dataField: 'maxClusterSize',
        text: 'Max Cluster Size',
        sort,
        filter,
    }, 
    {
        dataField: 'cohesion',
        text: 'Cohesion',
        sort,
        filter,
    }, 
    {
        dataField: 'coupling',
        text: 'Coupling',
        sort,
        filter,
    }, 
    {
        dataField: 'complexity',
        text: 'Complexity',
        sort,
        filter,
    }, 
    {
        dataField: 'performance',
        text: 'Performance',
        sort,
        filter,
    }, 
    {
        dataField: 'fmeasure',
        text: 'F-Score',
        sort,
        filter,
    }, 
    {
        dataField: 'accuracy',
        text: 'Accuracy',
        sort,
        filter,
    }, 
    {
        dataField: 'precision',
        text: 'Precision',
        sort,
        filter,
    }, 
    {
        dataField: 'recall',
        text: 'Recall',
        sort,
        filter,
    }, 
    {
        dataField: 'specificity',
        text: 'Specificity',
        sort,
        filter,
    },
    {
        dataField: 'mojoCommon',
        text: 'MoJo Common',
        sort,
        filter,
    },
    {
        dataField: 'mojoBiggest',
        text: 'MoJo Biggest',
        sort,
        filter,
    },
    {
        dataField: 'mojoNew',
        text: 'MoJo New',
        sort,
        filter,
    },
    {
        dataField: 'mojoSingletons',
        text: 'MoJo Singletons',
        sort,
        filter,
    }
];

const featuresMethodCallsMetricColumns = [
    {
        dataField: 'maxDepth',
        text: 'Max Depth',
        sort,
        filter,
    }, 
    {
        dataField: 'controllersWeight',
        text: 'Controllers Weight',
        sort,
        filter,
    }, 
    {
        dataField: 'servicesWeight',
        text: 'Services Weight',
        sort,
        filter,
    }, 
    {
        dataField: 'intermediateMethodsWeight',
        text: 'Intermediate Methods Weight',
        sort,
        filter,
    },
    {
        dataField: 'entitiesWeight',
        text: 'Entities Weight',
        sort,
        filter,
    },
    {
        dataField: 'numberClusters',
        text: 'Number Clusters',
        sort,
        filter,
    },
    {
        dataField: 'cohesion',
        text: 'Cohesion',
        sort,
        filter,
    }, 
    {
        dataField: 'coupling',
        text: 'Coupling',
        sort,
        filter,
    }, 
    {
        dataField: 'complexity',
        text: 'Complexity',
        sort,
        filter,
    }, 
    {
        dataField: 'performance',
        text: 'Performance',
        sort,
        filter,
    }, 
    {
        dataField: 'fmeasure',
        text: 'F-Score',
        sort,
        filter,
    }, 
    {
        dataField: 'accuracy',
        text: 'Accuracy',
        sort,
        filter,
    }, 
    {
        dataField: 'precision',
        text: 'Precision',
        sort,
        filter,
    }, 
    {
        dataField: 'recall',
        text: 'Recall',
        sort,
        filter,
    }, 
    {
        dataField: 'specificity',
        text: 'Specificity',
        sort,
        filter,
    },
    {
        dataField: 'mojoCommon',
        text: 'MoJo Common',
        sort,
        filter,
    },
    {
        dataField: 'mojoBiggest',
        text: 'MoJo Biggest',
        sort,
        filter,
    },
    {
        dataField: 'mojoNew',
        text: 'MoJo New',
        sort,
        filter,
    },
    {
        dataField: 'mojoSingletons',
        text: 'MoJo Singletons',
        sort,
        filter,
    }
];

const featuresEntitiesTracesMetricColumns = [
    {
        dataField: 'writeMetricWeight',
        text: 'Write Weight',
        sort,
        filter,
    }, 
    {
        dataField: 'readMetricWeight',
        text: 'Read Weight',
        sort,
        filter,
    },
    {
        dataField: 'numberClusters',
        text: 'Number Clusters',
        sort,
        filter,
    },
    {
        dataField: 'cohesion',
        text: 'Cohesion',
        sort,
        filter,
    }, 
    {
        dataField: 'coupling',
        text: 'Coupling',
        sort,
        filter,
    }, 
    {
        dataField: 'complexity',
        text: 'Complexity',
        sort,
        filter,
    }, 
    {
        dataField: 'performance',
        text: 'Performance',
        sort,
        filter,
    }, 
    {
        dataField: 'fmeasure',
        text: 'F-Score',
        sort,
        filter,
    }, 
    {
        dataField: 'accuracy',
        text: 'Accuracy',
        sort,
        filter,
    }, 
    {
        dataField: 'precision',
        text: 'Precision',
        sort,
        filter,
    }, 
    {
        dataField: 'recall',
        text: 'Recall',
        sort,
        filter,
    }, 
    {
        dataField: 'specificity',
        text: 'Specificity',
        sort,
        filter,
    },
    {
        dataField: 'mojoCommon',
        text: 'MoJo Common',
        sort,
        filter,
    },
    {
        dataField: 'mojoBiggest',
        text: 'MoJo Biggest',
        sort,
        filter,
    },
    {
        dataField: 'mojoNew',
        text: 'MoJo New',
        sort,
        filter,
    },
    {
        dataField: 'mojoSingletons',
        text: 'MoJo Singletons',
        sort,
        filter,
    }
];

const otherMetricColumns = [
    {
        dataField: 'numberClusters',
        text: 'Number Clusters',
        sort,
        filter,
    },
    {
        dataField: 'cohesion',
        text: 'Cohesion',
        sort,
        filter,
    }, 
    {
        dataField: 'coupling',
        text: 'Coupling',
        sort,
        filter,
    }, 
    {
        dataField: 'complexity',
        text: 'Complexity',
        sort,
        filter,
    }, 
    {
        dataField: 'performance',
        text: 'Performance',
        sort,
        filter,
    }, 
    {
        dataField: 'fmeasure',
        text: 'F-Score',
        sort,
        filter,
    }, 
    {
        dataField: 'accuracy',
        text: 'Accuracy',
        sort,
        filter,
    }, 
    {
        dataField: 'precision',
        text: 'Precision',
        sort,
        filter,
    }, 
    {
        dataField: 'recall',
        text: 'Recall',
        sort,
        filter,
    }, 
    {
        dataField: 'specificity',
        text: 'Specificity',
        sort,
        filter,
    },
    {
        dataField: 'mojoCommon',
        text: 'MoJo Common',
        sort,
        filter,
    },
    {
        dataField: 'mojoBiggest',
        text: 'MoJo Biggest',
        sort,
        filter,
    },
    {
        dataField: 'mojoNew',
        text: 'MoJo New',
        sort,
        filter,
    },
    {
        dataField: 'mojoSingletons',
        text: 'MoJo Singletons',
        sort,
        filter,
    }
];

export const Analyser = () => {
    const [analysisType, setAnalysisType] = useState("static");
    const [featureVectorizationStrategy, setFeatureVectorizationStrategy] = useState("methodCalls");
    const [linkageType, setLinkageType] = useState("average");
    const [codebases, setCodebases] = useState<Codebase[]>([]);
    const [codebase, setCodebase] = useState<Codebase>({ profiles: {} });
    const [selectedProfile, setSelectedProfile] = useState("");
    const [experts, setExperts] = useState<Decomposition[]>([]);
    const [expert, setExpert] = useState<Decomposition>({});
    const [resultData, setResultData] = useState([]);
    const [requestLimit, setRequestLimit] = useState("0");
    const [importFile, setImportFile] = useState(null);
    const [amountOfTraces, setAmountOfTraces] = useState("0");
    const [typeOfTraces, setTypeOfTraces] = useState<TraceType>(TraceType.ALL);
    const [isUploaded, setIsUploaded] = useState("");

    useEffect(() => loadCodebases(), []);

    function loadCodebases() {
        const service = new RepositoryService();
        service.getCodebases(
            [
                "name",
                "profiles",
            ]
        ).then(response => {
            return setCodebases(response.data);
        });
    }

    function loadCodebaseDecompositions(codebaseName: string) {
        const service = new RepositoryService();
        
        service.getCodebaseDecompositions(
            codebaseName,
            [
                "name",
                "expert",
                "codebaseName",
                "clusters",
                "nextClusterID"
            ]
        ).then((response) => {
            if (response.data !== null) {
                setExperts(response.data.filter((decomposition: Decomposition) => decomposition.expert));
            }
        });
    }

    function handleAnalysisType(event: any) {
        setAnalysisType(event.target.id);
    }

    function handleChangeFeatureVectorizationStrategy(event: any) {
        setFeatureVectorizationStrategy(event.target.id);
    }

    function changeCodebase(codebase: Pick<Codebase, "name" | "profiles">) {
        setCodebase(codebase);

        loadCodebaseDecompositions(codebase.name!);
    }

    function handleLinkageType(event: any) {
        setLinkageType(event.target.id);
    }

    function selectProfile(profile: string) {
        if (selectedProfile !== profile) {
            setSelectedProfile(profile);
        } else {
            setSelectedProfile("");
        }
    }

    function changeExpert(expert: any) {
        setExpert(expert);
    }

    function handleSubmit(event: any) {
        event.preventDefault()

        setIsUploaded("Uploading...");

        if (codebase.name == undefined) {
            setIsUploaded("Upload failed.");
            return;
        }

        const service = new RepositoryService();
        service.analyser(
            codebase.name,
            expert,
            selectedProfile,
            Number(requestLimit),
            Number(amountOfTraces),
            typeOfTraces,
        )
        .then(response => {
            if (response.status === HttpStatus.OK) {
                setIsUploaded("Upload completed successfully.");
            } else {
                setIsUploaded("Upload failed.");
            }
        })
        .catch(error => {
            setIsUploaded("Upload failed.");
        });
    }

    function handleEntitiesSubmit(event: any) {
        event.preventDefault()

        setIsUploaded("Uploading...");

        if (codebase.name == undefined) {
            setIsUploaded("Upload failed.");
            return;
        }

        const service = new RepositoryService();

        service.entitiesAnalyser(
            codebase.name,
            expert,
            selectedProfile,
            linkageType
        )
        .then(response => {
            if (response.status === HttpStatus.OK) {
                setIsUploaded("Upload completed successfully.");
            } else {
                setIsUploaded("Upload failed.");
            }
        })
        .catch(error => {
            setIsUploaded("Upload failed.");
        });
    }

    function handleClassesSubmit(event: any) {
        event.preventDefault()

        setIsUploaded("Uploading...");

        if (codebase.name == undefined) {
            setIsUploaded("Upload failed.");
            return;
        }

        const service = new RepositoryService();

        service.classesAnalyser(
            codebase.name,
            expert,
            selectedProfile,
            linkageType
        )
        .then(response => {
            if (response.status === HttpStatus.OK) {
                setIsUploaded("Upload completed successfully.");
            } else {
                setIsUploaded("Upload failed.");
            }
        })
        .catch(error => {
            setIsUploaded("Upload failed.");
        });
    }

    function handleFeatureSubmit(event: any) {
        event.preventDefault()

        setIsUploaded("Uploading...");

        if (codebase.name == undefined) {
            setIsUploaded("Upload failed.");
            return;
        }

        const service = new RepositoryService();

        if (featureVectorizationStrategy == "methodCalls") {
            service.methodCallsFeaturesAnalyser(
                codebase.name,
                expert,
                selectedProfile,
                linkageType
            )
            .then(response => {
                if (response.status === HttpStatus.OK) {
                    setIsUploaded("Upload completed successfully.");
                } else {
                    setIsUploaded("Upload failed.");
                }
            })
            .catch(error => {
                setIsUploaded("Upload failed.");
            });
        } else if (featureVectorizationStrategy == "entitiesTraces") {
            service.entitiesTracesFeaturesAnalyser(
                codebase.name,
                expert,
                selectedProfile,
                linkageType
            )
            .then(response => {
                if (response.status === HttpStatus.OK) {
                    setIsUploaded("Upload completed successfully.");
                } else {
                    setIsUploaded("Upload failed.");
                }
            })
            .catch(error => {
                setIsUploaded("Upload failed.");
            });
        }
        
    }

    function handleRequestLimitChange(event: any) {
        setRequestLimit(event.target.value);
    }

    function handleImportSubmit(event: any) {
        event.preventDefault();
        if (importFile == null) {
            setIsUploaded("Upload failed: No import file found.");
            return;
        }
        setResultData(Object.values(JSON.parse(importFile)));
    }

    function handleSelectImportFile(event: any) {
        let reader = new FileReader();
        reader.onload = function(e: any) {
            setImportFile(e.target.result);
        };
        
        if (event.target.files.length > 0)
            reader.readAsText(event.target.files[0]);
    }

    function handleChangeAmountOfTraces(event: any) {
       setAmountOfTraces(event.target.value);
    }

    function handleChangeTypeOfTraces(event: any) {
        setTypeOfTraces(event.target.value);
    }

    const renderBreadCrumbs = () => {
        return (
            <Breadcrumb>
                <Breadcrumb.Item href="/">
                    Home
                </Breadcrumb.Item>
                <Breadcrumb.Item active>
                    Analyser
                </Breadcrumb.Item>
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
                        label="Entities Aggregation"
                        type="radio"
                        id="entities"
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

    function renderStaticAnalyser() {
        return (
            <Form onSubmit={handleSubmit}>

            <Form.Group as={Row} controlId="codebase">
                <Form.Label column sm={3}>
                    Codebase
                </Form.Label>
                <Col sm={3}>
                    <DropdownButton title={codebase?.name || "Select Codebase"}>
                        {
                            codebases.map((codebase: any) =>
                                <Dropdown.Item
                                    key={codebase.name}
                                    onClick={() => changeCodebase(codebase)}
                                >
                                    {codebase.name}
                                </Dropdown.Item>
                            )
                        }
                    </DropdownButton>
                </Col>
            </Form.Group>

            <br/>

            <Form.Group as={Row} controlId="selectControllerProfiles">
                <Form.Label column sm={3}>
                    Select Controller Profiles
                </Form.Label>
                <Col sm={3}>
                    <DropdownButton title={'Controller Profiles'}>
                        {codebase.profiles && Object.keys(codebase.profiles).map((profile: any) =>
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
                <Form.Label column sm={3}>
                    Amount of Traces per Controller
                </Form.Label>
                <Col sm={3}>
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
                <Form.Label as="legend" column sm={3}>
                    Type of traces
                </Form.Label>
                <Col sm={3} style={{ paddingLeft: 0 }}>
                    <Col sm="auto">
                        <Form.Check
                            defaultChecked
                            id="allTraces"
                            value="ALL"
                            type="radio"
                            label="All"
                            name="typeOfTraces"
                            onClick={handleChangeTypeOfTraces}
                        />
                    </Col>
                    <Col sm="auto">
                        <Form.Check
                            type="radio"
                            id="longest"
                            value="LONGEST"
                            label="Longest"
                            name="typeOfTraces"
                            onClick={handleChangeTypeOfTraces}
                        />
                    </Col>
                    <Col sm="auto">
                        <Form.Check
                            id="withMoreDifferentTraces"
                            value="WITH_MORE_DIFFERENT_ACCESSES"
                            type="radio"
                            label="With more different accesses"
                            name="typeOfTraces"
                            onClick={handleChangeTypeOfTraces}
                        />

                    </Col>
                    <Col sm="auto">
                        <Form.Check
                            id="representativeSetOfAccesses"
                            value="REPRESENTATIVE"
                            type="radio"
                            label="Representative (set of accesses)"
                            name="typeOfTraces"
                            onClick={handleChangeTypeOfTraces}
                        />
                    </Col>
                    {/* WIP */}
                    <Col sm="auto">
                        <Form.Check
                            disabled
                            id="complete"
                            value="?"
                            type="radio"
                            label="Representative (subsequence of accesses)"
                            name="typeOfTraces"
                            onClick={undefined}
                        />
                    </Col>
                </Col>
            </Form.Group>

            <br/>

            <Form.Group as={Row} controlId="expert">
                <Form.Label column sm={3}>
                    Expert
                </Form.Label>
                <Col sm={3}>
                    <DropdownButton title={expert?.name || "Select Expert Cut"}>
                        {experts.map((expert: any) =>
                            <Dropdown.Item
                                key={expert.name}
                                onClick={() => changeExpert(expert)}
                            >
                                {expert.name}
                            </Dropdown.Item>
                        )}
                    </DropdownButton>
                </Col>
            </Form.Group>

            <br/>

            <Form.Group as={Row} controlId="requestLimit">
                <Form.Label column sm={3}>
                    Request limit
                </Form.Label>
                <Col sm={3}>
                    <FormControl
                        type="number"
                        placeholder="Request Limit"
                        value={requestLimit}
                        onChange={handleRequestLimitChange}
                    />
                </Col>
            </Form.Group>

            <br/>

            <Form.Group as={Row}>
                <Col sm={{ span: 5, offset: 3 }}>
                    <Button
                        type="submit"
                        disabled={
                            isUploaded === "Uploading..." ||
                            selectedProfile === "" ||
                            requestLimit === "" ||
                            (typeOfTraces === undefined || amountOfTraces === "")
                        }
                    >
                        Submit
                    </Button>
                    <Form.Text>
                        {isUploaded}
                    </Form.Text>
                </Col>
            </Form.Group>
            </Form>
        );
    }

    function renderFeatureAnalyser() {
        return (
            <Form onSubmit={handleFeatureSubmit}>

            <Form.Group as={Row} controlId="codebase">
                <Form.Label column sm={3}>
                    Codebase
                </Form.Label>
                <Col sm={3}>
                    <DropdownButton title={codebase?.name || "Select Codebase"}>
                        {
                            codebases.map((codebase: any) =>
                                <Dropdown.Item
                                    key={codebase.name}
                                    onClick={() => changeCodebase(codebase)}
                                >
                                    {codebase.name}
                                </Dropdown.Item>
                            )
                        }
                    </DropdownButton>
                </Col>
            </Form.Group>

            <br/>

            <Form.Group as={Row} controlId="selectControllerProfiles">
                <Form.Label column sm={3}>
                    Select Controller Profiles
                </Form.Label>
                <Col sm={3}>
                    <DropdownButton title={'Controller Profiles'}>
                        {codebase.profiles && Object.keys(codebase.profiles).map((profile: any) =>
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
                            id="entitiesTraces"
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

                <Form.Group as={Row}>
                <Col sm={{ span: 5, offset: 3 }}>
                    <Button
                        type="submit"
                        disabled={
                            isUploaded === "Uploading..." ||
                            selectedProfile === "" ||
                            requestLimit === "" ||
                            (typeOfTraces === undefined || amountOfTraces === "")
                        }
                    >
                        Submit
                    </Button>
                    <Form.Text>
                        {isUploaded}
                    </Form.Text>
                </Col>
                </Form.Group>

            </Form>
        );
    }

    function renderEntitiesAnalyser() {
        return (
            <Form onSubmit={handleEntitiesSubmit}>

            <Form.Group as={Row} controlId="codebase">
                <Form.Label column sm={3}>
                    Codebase
                </Form.Label>
                <Col sm={3}>
                    <DropdownButton title={codebase?.name || "Select Codebase"}>
                        {
                            codebases.map((codebase: any) =>
                                <Dropdown.Item
                                    key={codebase.name}
                                    onClick={() => changeCodebase(codebase)}
                                >
                                    {codebase.name}
                                </Dropdown.Item>
                            )
                        }
                    </DropdownButton>
                </Col>
            </Form.Group>

            <br/>

            <Form.Group as={Row} controlId="selectControllerProfiles">
                <Form.Label column sm={3}>
                    Select Controller Profiles
                </Form.Label>
                <Col sm={3}>
                    <DropdownButton title={'Controller Profiles'}>
                        {codebase.profiles && Object.keys(codebase.profiles).map((profile: any) =>
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

                <Form.Group as={Row}>
                <Col sm={{ span: 5, offset: 3 }}>
                    <Button
                        type="submit"
                        disabled={
                            isUploaded === "Uploading..." ||
                            selectedProfile === "" ||
                            requestLimit === "" ||
                            (typeOfTraces === undefined || amountOfTraces === "")
                        }
                    >
                        Submit
                    </Button>
                    <Form.Text>
                        {isUploaded}
                    </Form.Text>
                </Col>
                </Form.Group>
            
            </Form>
        );
    }

    function renderClassAnalyser() {
        return (
            <Form onSubmit={handleClassesSubmit}>

            <Form.Group as={Row} controlId="codebase">
                <Form.Label column sm={3}>
                    Codebase
                </Form.Label>
                <Col sm={3}>
                    <DropdownButton title={codebase?.name || "Select Codebase"}>
                        {
                            codebases.map((codebase: any) =>
                                <Dropdown.Item
                                    key={codebase.name}
                                    onClick={() => changeCodebase(codebase)}
                                >
                                    {codebase.name}
                                </Dropdown.Item>
                            )
                        }
                    </DropdownButton>
                </Col>
            </Form.Group>

            <br/>

            <Form.Group as={Row} controlId="selectControllerProfiles">
                <Form.Label column sm={3}>
                    Select Controller Profiles
                </Form.Label>
                <Col sm={3}>
                    <DropdownButton title={'Controller Profiles'}>
                        {codebase.profiles && Object.keys(codebase.profiles).map((profile: any) =>
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

                <Form.Group as={Row}>
                <Col sm={{ span: 5, offset: 3 }}>
                    <Button
                        type="submit"
                        disabled={
                            isUploaded === "Uploading..." ||
                            selectedProfile === "" ||
                            requestLimit === "" ||
                            (typeOfTraces === undefined || amountOfTraces === "")
                        }
                    >
                        Submit
                    </Button>
                    <Form.Text>
                        {isUploaded}
                    </Form.Text>
                </Col>
                </Form.Group>
            
            </Form>
        );
    }

    const metricRows = resultData.map((data: any, index: number) => {
        return {
            id: index,
            access: data.accessWeight,
            write: data.writeWeight,
            read: data.readWeight,
            sequence: data.sequenceWeight,
            numberClusters: data.numberClusters,
            maxClusterSize: data.maxClusterSize,
            cohesion: data.cohesion,
            coupling: data.coupling,
            complexity: data.complexity,
            performance: data.performance,
            fmeasure: data.fmeasure,
            accuracy: data.accuracy,
            precision: data.precision,
            recall: data.recall,
            specificity: data.specificity,
            mojoCommon: data.mojoCommon,
            mojoBiggest: data.mojoBiggest,
            mojoNew: data.mojoNew,
            mojoSingletons: data.mojoSingletons
        }
    });

    const featuresMethodCallsMetricRows = resultData.map((data: any, index: number) => {
        return {
            id: index,
            maxDepth: data.maxDepth,
            controllersWeight: data.controllersWeight,
            servicesWeight: data.servicesWeight,
            intermediateMethodsWeight: data.intermediateMethodsWeight,
            entitiesWeight: data.entitiesWeight,
            numberClusters: data.numberClusters,
            cohesion: data.cohesion.toFixed(2),
            coupling: data.coupling.toFixed(2),
            complexity: data.complexity.toFixed(2),
            performance: data.performance.toFixed(2),
            fmeasure: data.fmeasure,
            accuracy: data.accuracy,
            precision: data.precision,
            recall: data.recall,
            specificity: data.specificity,
            mojoCommon: data.mojoCommon,
            mojoBiggest: data.mojoBiggest,
            mojoNew: data.mojoNew,
            mojoSingletons: data.mojoSingletons
        }
    });

    const featuresEntitiesTracesMetricRows = resultData.map((data: any, index: number) => {
        return {
            id: index,
            writeMetricWeight: data.writeMetricWeight,
            readMetricWeight: data.readMetricWeight,
            numberClusters: data.numberClusters,
            cohesion: data.cohesion.toFixed(2),
            coupling: data.coupling.toFixed(2),
            complexity: data.complexity.toFixed(2),
            performance: data.performance.toFixed(2),
            fmeasure: data.fmeasure,
            accuracy: data.accuracy,
            precision: data.precision,
            recall: data.recall,
            specificity: data.specificity,
            mojoCommon: data.mojoCommon,
            mojoBiggest: data.mojoBiggest,
            mojoNew: data.mojoNew,
            mojoSingletons: data.mojoSingletons
        }
    });

    const otherMetricRows = resultData.map((data: any, index: number) => {
        return {
            id: index,
            numberClusters: data.numberClusters,
            cohesion: data.cohesion.toFixed(2),
            coupling: data.coupling.toFixed(2),
            complexity: data.complexity.toFixed(2),
            performance: data.performance.toFixed(2),
            fmeasure: data.fmeasure,
            accuracy: data.accuracy,
            precision: data.precision,
            recall: data.recall,
            specificity: data.specificity,
            mojoCommon: data.mojoCommon,
            mojoBiggest: data.mojoBiggest,
            mojoNew: data.mojoNew,
            mojoSingletons: data.mojoSingletons
        }
    });

    return (
        <>
            {renderBreadCrumbs()}
            
            <h4 style={{color: "#666666"}}>
                Analyser
            </h4>

            <h4 style={{ color: "#666666" }}>
                Choose the Analysis Type
            </h4>

            {renderAnalysisType()}

            <br/>

            {analysisType == "static" ? renderStaticAnalyser() : <div></div>}
            {analysisType == "feature" ? renderFeatureAnalyser() : <div></div>}
            {analysisType == "entities" ? renderEntitiesAnalyser() : <div></div>}
            {analysisType == "class" ? renderClassAnalyser() : <div></div>}

            <br/>

            <Form onSubmit={handleImportSubmit}>
                <Form.Group as={Row} controlId="importFile">
                    <Form.Label column sm={3}>
                        Import Analyser Results from File
                    </Form.Label>
                    <Col sm={3}>
                        <FormControl
                            type="file"
                            onChange={handleSelectImportFile}
                        />
                    </Col>
                </Form.Group>

                <br/>

                <Form.Group as={Row}>
                    <Col sm={{ span: 5, offset: 3 }}>
                        <Button
                            type="submit"
                            disabled={ importFile === null }
                        >
                            Import Analyser Results
                        </Button>
                    </Col>
                </Form.Group>
            </Form>

            <br/>

            {analysisType == "static" ? 
            <BootstrapTable
                keyField='id'
                data={ metricRows }
                columns={ metricColumns }
                filter={ filterFactory() }
            />
            : <div></div>}

            {analysisType == "feature" && featureVectorizationStrategy == "methodCalls" ?
            <BootstrapTable
                keyField='id'
                data={ featuresMethodCallsMetricRows }
                columns={ featuresMethodCallsMetricColumns }
                filter={ filterFactory() }
            />
            : <div></div>}

            {analysisType == "feature" && featureVectorizationStrategy == "entitiesTraces" ?
            <BootstrapTable
                keyField='id'
                data={ featuresEntitiesTracesMetricRows }
                columns={ featuresEntitiesTracesMetricColumns }
                filter={ filterFactory() }
            />
            : <div></div>}

            {analysisType == "class" || analysisType == "entities" ?
            <BootstrapTable
                keyField='id'
                data={ otherMetricRows }
                columns={ otherMetricColumns }
                filter={ filterFactory() }
            />
            : <div></div>}
        </>
    )
}