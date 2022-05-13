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
        dataField: 'commit',
        text: 'Commit',
        sort,
        filter,
    },
    {
        dataField: 'author',
        text: 'Author',
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
];

export const CommitAnalyser = () => {
    const [codebases, setCodebases] = useState<Codebase[]>([]);
    const [codebase, setCodebase] = useState<Codebase>({ profiles: {} });
    const [resultData, setResultData] = useState([]);
    const [importFile, setImportFile] = useState(null);
    const [isUploaded, setIsUploaded] = useState("");

    useEffect(() => loadCodebases(), []);

    function loadCodebases() {
        const service = new RepositoryService();
        service.getCodebases(
            [
                "name"
            ]
        ).then(response => {
            return setCodebases(response.data);
        });
    }

    function changeCodebase(codebase: Pick<Codebase, "name" | "profiles">) {
        setCodebase(codebase);
    }


    function handleSubmit(event: any) {
        event.preventDefault()

        setIsUploaded("Uploading...");

        if (codebase.name == undefined) {
            setIsUploaded("Upload failed.");
            return;
        }

        const service = new RepositoryService();
        service.commitAnalyser(
            codebase.name,
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


    function handleImportSubmit(event: any) {
        event.preventDefault();
        if (importFile == null) {
            setIsUploaded("Upload failed: No import file found.");
            return;
        }
        console.log(importFile);
        console.log(JSON.parse(importFile));
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
            commit: data.commitWeight,
            author: data.authorsWeight,
            numberClusters: data.numberClusters,
            maxClusterSize: data.maxClusterSize,
            cohesion: data.cohesion,
            coupling: data.coupling,
            complexity: data.complexity,
            performance: data.performance,
        }
    });

    return (
        <>
            {renderBreadCrumbs()}

            <h4 style={{color: "#666666"}}>
                Commit Analyser
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

                <Form.Group as={Row}>
                    <Col sm={{ span: 5, offset: 3 }}>
                        <Button
                            type="submit"
                            disabled={
                                isUploaded === "Uploading..."
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