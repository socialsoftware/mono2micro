import React, {useEffect, useState} from 'react';
import { APIService } from '../../services/APIService';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Form from 'react-bootstrap/Form';
import DropdownButton from 'react-bootstrap/DropdownButton';
import Dropdown from 'react-bootstrap/Dropdown';
import Button from 'react-bootstrap/Button';
import Breadcrumb from 'react-bootstrap/Breadcrumb';
import {DefaultComparisonTool} from "./implementations/DefaultComparisonTool";

const HttpStatus = require('http-status-codes');

export const ComparisonTool = () => {
    const [codebases, setCodebases] = useState([]);
    const [codebase, setCodebase] = useState({});
    const [decompositions, setDecompositions] = useState([]);
    const [isUploaded, setIsUploaded] = useState("");
    const [decomposition1, setDecomposition1] = useState({});
    const [decomposition2, setDecomposition2] = useState({});
    const [resultData, setResultData] = useState({});

    useEffect(() => loadCodebases(), []);

    function loadCodebases() {
        const service = new APIService();
        service.getCodebases().then(response => {
            setCodebases(response);
        });
    }

    function loadCodebaseDecompositions(codebaseName) {
        const service = new APIService();
        
        service.getCodebaseDecompositions(
            codebaseName
        ).then((response) => {
            if (response.data !== null) {
                setDecompositions(response.data);
            }
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

        const service = new APIService();
        service.analysis(decomposition1.name, decomposition2.name)
            .then(response => {
                if (response.status === HttpStatus.OK) {
                    setResultData(response.data);
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
                    Comparison Tool
                </Breadcrumb.Item>
            </Breadcrumb>
        );
    }

    const expertDecompositionsForDecomposition1 = [];
    const nonExpertDecompositionsForDecomposition1 = [];

    decompositions.forEach((decomposition) => {
        const dropdownItem = (
            <Dropdown.Item
                key={decomposition.name}
                onClick={() => changeDecomposition1(decomposition)}
            >
                {decomposition.name + " from " + decomposition.strategyName}
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
                {decomposition.name + " from " + decomposition.strategyName}
            </Dropdown.Item>
        )

        if (decomposition.expert) {
            expertDecompositionsForDecomposition2.push(dropdownItem)
            return;
        }

        nonExpertDecompositionsForDecomposition2.push(dropdownItem);
    });

    return (
        <div className={"ms-2 me-2"}>
            {renderBreadCrumbs()}
            <h4 style={{ color: "#666666" }}>Comparison Tool</h4>

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
                                    decomposition1.name + " from " + decomposition1.strategyName
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
                                decomposition2.name + " from " + decomposition2.strategyName
                            }
                        >
                            {<>
                                {expertDecompositionsForDecomposition2}
                                <Dropdown.Divider />
                            </>}
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

            {/*ADD ADDITIONAL COMPARISONS HERE*/}

            {resultData.type === "DEFAULT_COMPARISON_TOOL_DTO" &&
                <DefaultComparisonTool
                    codebaseName={codebase.name}
                    resultData={resultData}
                />
            }
        </div>
    )
}