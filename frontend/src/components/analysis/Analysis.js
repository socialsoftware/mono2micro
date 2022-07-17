import React, {useContext, useEffect, useState} from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Form from 'react-bootstrap/Form';
import DropdownButton from 'react-bootstrap/DropdownButton';
import Dropdown from 'react-bootstrap/Dropdown';
import Button from 'react-bootstrap/Button';
import Breadcrumb from 'react-bootstrap/Breadcrumb';
import BootstrapTable from 'react-bootstrap-table-next';
import ToolkitProvider, { Search } from 'react-bootstrap-table2-toolkit';
import AppContext from "../AppContext";
import {SourceType} from "../../models/sources/Source";

const HttpStatus = require('http-status-codes');

const metricsColumns = [
    {
        dataField: 'tp',
        text: 'TP',
    },
    {
        dataField: 'tn',
        text: 'TN',
    },
    {
        dataField: 'fp',
        text: 'FP',
    },
    {
        dataField: 'fn',
        text: 'FN',
    },
    {
        dataField: 'fscore',
        text: 'F-Score',
    },
    {
        dataField: 'accuracy',
        text: 'Accuracy',
    },
    {
        dataField: 'precision',
        text: 'Precision',
    },
    {
        dataField: 'recall',
        text: 'Recall',
    },
    {
        dataField: 'specificity',
        text: 'Specificity',
    },
    {
        dataField: 'mojoCommon',
        text: 'MoJo Common Entities',
    },
    {
        dataField: 'mojoBiggest',
        text: 'MoJo Biggest Cluster',
    },
    {
        dataField: 'mojoNew',
        text: 'MoJo New Cluster',
    },
    {
        dataField: 'mojoSingletons',
        text: 'MoJo Singletons',
    },
];

export const Analysis = () => {
    const context = useContext(AppContext);
    const { translateEntity } = context;

    const [codebases, setCodebases] = useState([]);
    const [codebase, setCodebase] = useState({});
    const [decompositions, setDecompositions] = useState([]);
    const [isUploaded, setIsUploaded] = useState("");
    const [decomposition1, setDecomposition1] = useState({});
    const [decomposition2, setDecomposition2] = useState({});
    const [resultData, setResultData] = useState({});
    const [falsePairs, setFalsePairs] = useState([]);

    useEffect(() => loadCodebases(), []);

    function loadCodebases() {
        const service = new RepositoryService();
        service.getCodebases().then(response => {
            setCodebases(response.data);
        });
    }

    function loadCodebaseDecompositions(codebaseName) {
        const service = new RepositoryService();
        
        service.getCodebaseDecompositions(
            codebaseName,
            undefined,
            [
                "name",
                "dendrogramName",
                "expert",
                "codebaseName",
                "clusters"
            ]
        ).then((response) => {
            if (response.data !== null) {
                setDecompositions(response.data);
            }
        });

        service.getIdToEntity(codebaseName + SourceType.IDTOENTITIY).then(response => {
            const { updateEntityTranslationFile } = context;
            updateEntityTranslationFile(response.data);
        }).catch(error => {
            console.log(error);
        });
    }

    function changeCodebase(codebase) {
        setCodebase(codebase);

        loadCodebaseDecompositions(codebase.name);
    }

    function changeDecomposition1(decomposition) {
        setDecomposition1(decomposition);
    }

    function changeDecomposition2(decomposition) {
        setDecomposition2(decomposition);
    }

    function handleSubmit(event) {
        event.preventDefault();

        setIsUploaded("Uploading...");

        let requestData = {
            "decomposition1": decomposition1,
            "decomposition2": decomposition2
        };

        const service = new RepositoryService();
        service.analysis(requestData)
            .then(response => {
                if (response.status === HttpStatus.OK) {
                    setResultData(response.data);
                    setFalsePairs(response.data.falsePairs);
                    setIsUploaded("Upload completed successfully.");
                } else {
                    setIsUploaded("Upload failed.");
                }
            })
            .catch(() => {
                setIsUploaded("Upload failed.");
            });
    }

    const renderBreadCrumbs = () => {
        return (
            <Breadcrumb>
                <Breadcrumb.Item href="/">
                    Home
                </Breadcrumb.Item>
                <Breadcrumb.Item active>
                    Microservice Analysis
                </Breadcrumb.Item>
            </Breadcrumb>
        );
    }
    
    const falsePairRows = falsePairs.map(falsePair => {
        return {
            id: falsePair[0] + falsePair[3],
            e1: translateEntity(falsePair[0]),
            e1g1: falsePair[1],
            e1g2: falsePair[2],
            e2: translateEntity(falsePair[3]),
            e2g1: falsePair[4],
            e2g2: falsePair[5]
        }
    });

    const falsePairColumns = [{
        dataField: 'e1',
        text: 'Entity 1',
        sort: true
    }, {
        dataField: 'e1g1',
        text: decomposition1.name,
        sort: true
    }, {
        dataField: 'e1g2',
        text: decomposition2.name,
        sort: true
    }, {
        dataField: 'space',
        text: ''
    }, {
        dataField: 'e2',
        text: 'Entity 2',
        sort: true
    }, {
        dataField: 'e2g1',
        text: decomposition1.name,
        sort: true
    }, {
        dataField: 'e2g2',
        text: decomposition2.name,
        sort: true
    }];

    const { SearchBar } = Search;

    const expertDecompositionsForDecomposition1 = [];
    const nonExpertDecompositionsForDecomposition1 = [];

    decompositions.forEach((decomposition) => {
        const dropdownItem = (
            <Dropdown.Item
                key={decomposition.name}
                onClick={() => changeDecomposition1(decomposition)}
            >
                {decomposition.name + " from " + decomposition.dendrogramName}
            </Dropdown.Item>
        )

        if (decomposition.expert) {
            expertDecompositionsForDecomposition1.push(dropdownItem)
            return;
        }

        nonExpertDecompositionsForDecomposition1.push(dropdownItem);
    });

    const expertDecompositionsForDecomposition2 = [];
    const nonExpertDecompositionsForDecomposition2 = [];

    decompositions.forEach((decomposition) => {
        const dropdownItem = (
            <Dropdown.Item
                key={decomposition.name}
                onClick={() => changeDecomposition2(decomposition)}
            >
                {decomposition.name + " from " + decomposition.dendrogramName}
            </Dropdown.Item>
        )

        if (decomposition.expert) {
            expertDecompositionsForDecomposition2.push(dropdownItem)
            return;
        }

        nonExpertDecompositionsForDecomposition2.push(dropdownItem);
    });

    return (
        <>
            {renderBreadCrumbs()}
            <h4 style={{ color: "#666666" }}>Microservice Analysis</h4>

            <Form onSubmit={handleSubmit}>
                <Form.Group as={Row} controlId="codebase">
                    <Form.Label column sm={2}>
                        Codebase
                    </Form.Label>
                    <Col sm={5}>
                        <DropdownButton
                            className="mb-2"
                            title={Object.keys(codebase).length === 0 ?
                                "Select Codebase" :
                                codebase.name
                            }
                        >
                            {codebases.map(codebase =>
                                <Dropdown.Item
                                    key={codebase.name}
                                    onClick={() => changeCodebase(codebase)}
                                >
                                    {codebase.name}
                                </Dropdown.Item>
                            )}
                        </DropdownButton>
                    </Col>
                </Form.Group>

                <Form.Group as={Row} controlId="sourceOfTruth">
                    <Form.Label column sm={2}>
                        Source of Truth
                    </Form.Label>
                    <Col sm={5}>
                        <DropdownButton
                            className="mb-2"
                            title={
                                Object.keys(decomposition1).length === 0 ?
                                    "Select Cut" :
                                    decomposition1.name + " from " + decomposition1.dendrogramName
                            }
                        >
                            {expertDecompositionsForDecomposition1}
                            <Dropdown.Divider />
                            {nonExpertDecompositionsForDecomposition1}
                        </DropdownButton>
                    </Col>
                </Form.Group>

                <Form.Group as={Row} controlId="compareToCut">
                    <Form.Label column sm={2}>
                        Compare to Cut
                    </Form.Label>
                    <Col sm={5}>
                        <DropdownButton
                            className="mb-2"
                            title={Object.keys(decomposition2).length === 0 ?
                                "Select Cut" :
                                decomposition2.name + " from " + decomposition2.dendrogramName
                            }
                        >
                            {nonExpertDecompositionsForDecomposition2}
                        </DropdownButton>
                    </Col>
                </Form.Group>

                <Form.Group as={Row}>
                    <Col sm={{ span: 5, offset: 2 }}>
                        <Button
                            className="me-2 mb-3"
                            type="submit"
                            disabled={Object.keys(codebase).length === 0 ||
                                Object.keys(decomposition1).length === 0 ||
                                Object.keys(decomposition2).length === 0
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

            {Object.keys(resultData).length !== 0 &&
                <>
                    <h4 style={{ color: "#666666" }}> Metrics </h4>
                    <BootstrapTable
                        keyField='id'
                        bootstrap4
                        data={[{
                            id: "metrics",
                            tp: resultData.truePositive,
                            tn: resultData.trueNegative,
                            fp: resultData.falsePositive,
                            fn: resultData.falseNegative,
                            accuracy: resultData.accuracy,
                            precision: resultData.precision,
                            recall: resultData.recall,
                            specificity: resultData.specificity === -1 ? "--" : resultData.specificity,
                            fscore: resultData.fmeasure,
                            mojoCommon: resultData.mojoCommon,
                            mojoBiggest: resultData.mojoBiggest,
                            mojoNew: resultData.mojoNew,
                            mojoSingletons: resultData.mojoSingletons,
                        }
                        ]}
                        columns={metricsColumns}
                    />
                    <hr />
                    <h4 style={{ color: "#666666" }}>False Pairs</h4>

                    <ToolkitProvider
                        bootstrap4
                        keyField="id"
                        data={falsePairRows}
                        columns={falsePairColumns}
                        search>
                        {
                            props => (
                                <>
                                    <SearchBar {...props.searchProps} />
                                    <BootstrapTable {...props.baseProps} />
                                </>
                            )
                        }
                    </ToolkitProvider>
                </>
            }
        </>
    )
}