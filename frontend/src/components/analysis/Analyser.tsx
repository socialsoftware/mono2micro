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
import {
    ClusteringAlgorithmType,
    Codebase,
    Decomposition,
    SimilarityGeneratorType,
    TraceType
} from "../../type-declarations/types.d";

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

export const Analyser = () => {
    const [codebases, setCodebases] = useState<Codebase[]>([]);
    const [codebase, setCodebase] = useState<Codebase>({ profiles: {} });
    const [selectedProfile, setSelectedProfile] = useState("");
    const [experts, setExperts] = useState<Decomposition[]>([]);
    const [expert, setExpert] = useState<Decomposition>({});
    const [resultData, setResultData] = useState([]);
    const [requestLimit, setRequestLimit] = useState("0");
    const [importFile, setImportFile] = useState(null);
    const [amountOfTraces, setAmountOfTraces] = useState("0");
    const [traceType, setTraceType] = useState<TraceType>(TraceType.ALL);
    const [similarityGenerator, setSimilarityGenerator] = useState(SimilarityGeneratorType.DEFAULT);
    const [clusteringAlgorithm, setClusteringAlgorithm] = useState(ClusteringAlgorithmType.SCIPY);
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

    function changeCodebase(codebase: Pick<Codebase, "name" | "profiles">) {
        setCodebase(codebase);

        loadCodebaseDecompositions(codebase.name!);
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
            traceType,
            similarityGenerator,
            clusteringAlgorithm
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

    function handleChangeTraceType(event: any) {
        setTraceType(event.target.value);
    }

    function handleChangeSimilarityGenerator(event: any) {
        setSimilarityGenerator(event.target.value);
    }

    function handleChangeClusteringAlgorithm(event: any) {
        setClusteringAlgorithm(event.target.value);
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

    return (
        <>
            {renderBreadCrumbs()}
            <h4 style={{color: "#666666"}}>
                Analyser
            </h4>

            <Form onSubmit={handleSubmit}>
                <Form.Group as={Row} controlId="codebase" className="mb-3">
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

                <Form.Group as={Row} controlId="selectControllerProfiles" className="mb-3">
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
                <Form.Group as={Row} controlId="amountOfTraces" className="mb-3">
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
                <Form.Group as={Row} className="align-items-center mb-3">
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
                                name="traceType"
                                onClick={handleChangeTraceType}
                            />
                        </Col>
                        <Col sm="auto">
                            <Form.Check
                                type="radio"
                                id="longest"
                                value="LONGEST"
                                label="Longest"
                                name="traceType"
                                onClick={handleChangeTraceType}
                            />
                        </Col>
                        <Col sm="auto">
                            <Form.Check
                                id="withMoreDifferentTraces"
                                value="WITH_MORE_DIFFERENT_ACCESSES"
                                type="radio"
                                label="With more different accesses"
                                name="traceType"
                                onClick={handleChangeTraceType}
                            />

                        </Col>
                        <Col sm="auto">
                            <Form.Check
                                id="representativeSetOfAccesses"
                                value="REPRESENTATIVE"
                                type="radio"
                                label="Representative (set of accesses)"
                                name="traceType"
                                onClick={handleChangeTraceType}
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
                                name="traceType"
                                onClick={undefined}
                            />
                        </Col>
                    </Col>
                </Form.Group>

                <Form.Group as={Row} className="align-items-center mb-3">
                    <Form.Label as="legend" column sm={3}>
                        Similarity Generator
                    </Form.Label>
                    <Col sm={3} style={{ paddingLeft: 0 }}>
                        <Col sm="auto">
                            <Form.Check
                                defaultChecked
                                id="default"
                                value="DEFAULT"
                                type="radio"
                                label="Default"
                                name="similarityGenerator"
                                onClick={handleChangeSimilarityGenerator}
                            />
                        </Col>
                    </Col>
                </Form.Group>

                <Form.Group as={Row} className="align-items-center mb-3">
                    <Form.Label as="legend" column sm={3}>
                        Clustering Algorithm
                    </Form.Label>
                    <Col sm={3} style={{ paddingLeft: 0 }}>
                        <Col sm="auto">
                            <Form.Check
                                defaultChecked
                                id="scipy"
                                value="SCIPY"
                                type="radio"
                                label="SciPy"
                                name="clusteringAlgorithm"
                                onClick={handleChangeClusteringAlgorithm}
                            />
                        </Col>
                    </Col>
                </Form.Group>

                <Form.Group as={Row} controlId="expert" className="mb-3">
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

                <Form.Group as={Row} controlId="requestLimit" className="mb-3">
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

                <Form.Group as={Row}>
                    <Col sm={{ span: 5, offset: 3 }}>
                        <Button
                            type="submit"
                            disabled={
                                isUploaded === "Uploading..." ||
                                selectedProfile === "" ||
                                requestLimit === "" ||
                                (traceType === undefined || amountOfTraces === "") ||
                                similarityGenerator === undefined ||
                                clusteringAlgorithm === undefined
                            }
                        >
                            Submit
                        </Button>
                        <Form.Text className="ms-2">
                            {isUploaded}
                        </Form.Text>
                    </Col>
                </Form.Group>
            </Form>
            <br />
            <Form onSubmit={handleImportSubmit}>
                <Form.Group as={Row} controlId="importFile" className="mb-3">
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

                <Form.Group as={Row} className="mb-4">
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
            <BootstrapTable
                keyField='id'
                data={ metricRows }
                columns={ metricColumns }
                filter={ filterFactory() }
            />
        </>
    )
}