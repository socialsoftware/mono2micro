import React, {useEffect, useState} from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Form from 'react-bootstrap/Form';
import DropdownButton from 'react-bootstrap/DropdownButton';
import Dropdown from 'react-bootstrap/Dropdown';
import Button from 'react-bootstrap/Button';
import Breadcrumb from 'react-bootstrap/Breadcrumb';
import {AccessesSciPyAnalysis} from "./implementations/AccessesSciPyAnalysis";
import {StrategyType} from "../../models/strategy/Strategy";

const HttpStatus = require('http-status-codes');

export const Analysis = () => {
    const [codebases, setCodebases] = useState([]);
    const [codebase, setCodebase] = useState({});
    const [decompositions, setDecompositions] = useState([]);
    const [isUploaded, setIsUploaded] = useState("");
    const [decomposition1, setDecomposition1] = useState({});
    const [decomposition2, setDecomposition2] = useState({});
    const [resultData, setResultData] = useState({});

    useEffect(() => loadCodebases(), []);

    function loadCodebases() {
        const service = new RepositoryService();
        service.getCodebases().then(response => {
            setCodebases(response);
        });
    }

    function loadCodebaseDecompositions(codebaseName) {
        const service = new RepositoryService();
        
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

        const service = new RepositoryService();
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
                    Microservice Analysis
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

            {decomposition1.length !== 0 && decomposition1.type === StrategyType.ACCESSES_SCIPY &&
                decomposition2.length !== 0 && decomposition2.type === StrategyType.ACCESSES_SCIPY &&
                <AccessesSciPyAnalysis
                    codebaseName={codebase.name}
                    resultData={resultData}
                    decomposition1={decomposition1}
                    decomposition2={decomposition2}
                />
            }
        </>
    )
}