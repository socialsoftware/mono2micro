import React, {useEffect, useState} from 'react';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Form from 'react-bootstrap/Form';
import Dropdown from 'react-bootstrap/Dropdown';
import DropdownButton from 'react-bootstrap/DropdownButton';
import Breadcrumb from 'react-bootstrap/Breadcrumb';

import {useParams} from "react-router-dom";
import {AccessesSciPyForm} from "./forms/AccessesSciPyForm";
import {RepositoryService} from "../../services/RepositoryService";
import HttpStatus from "http-status-codes";
import {CollectorFactory} from "../../models/collectors/CollectorFactory";
import {StrategyFactory} from "../../models/strategies/StrategyFactory";
import {StrategyDescription, StrategyType} from "../../models/strategies/Strategy";
import Button from "react-bootstrap/Button";

export const Strategies = () => {

    let { codebaseName } = useParams();

    const [isUploaded, setIsUploaded] = useState("");
    const [collectors, setCollectors] = useState([]);
    const [strategy, setStrategy] = useState(undefined);
    const [strategies, setStrategies] = useState([]);

    // Executes on mount
    useEffect(() => {
        loadCollectors();
        loadStrategies();
    }, []);

    function loadCollectors() {
        const service = new RepositoryService();
        service.getCodebase(codebaseName, ['collectors'])
            .then(response => {
                if (response.data !== undefined)
                    setCollectors(response.data.collectors.map(collector =>
                        CollectorFactory.getCollector(codebaseName, collector)));
            });
    }

    function loadStrategies() {
        const allPossibleStrategies = collectors.flatMap(collector => collector.possibleStrategies)
            .filter((x, i, a) =>  a.indexOf(x) === i);

        const service = new RepositoryService();
        service.getStrategies(
            codebaseName,
            allPossibleStrategies
        ).then(response => setStrategies(response));
    }

    function handleSubmit(event) {
        event.preventDefault();
        setIsUploaded("Uploading...");

        const service = new RepositoryService();
        service.createStrategy(strategy)
            .then(response => {
                if (response.status === HttpStatus.CREATED) {
                    setStrategy(undefined);
                    setIsUploaded("");
                } else {
                    setIsUploaded("Upload failed.");
                }
            })
            .catch(error => {
                if (error.response !== undefined && error.response.status === HttpStatus.UNAUTHORIZED) {
                    setIsUploaded("Upload failed. Name already exists.");
                } else {
                    setIsUploaded("Upload failed.");
                }
            });
    }

    function handleDeleteStrategy(strategy) {
        const service = new RepositoryService();

        service.deleteStrategy(
            strategy.codebaseName,
            strategy.name
        )
            .then(() => {
                loadStrategies();
            });
    }


    function handleSelectStrategy(strategy) {
        setStrategy(StrategyFactory.getStrategy({type: strategy, codebaseName}));
    }

    function renderBreadCrumbs() {
        return (
            <Breadcrumb>
                <Breadcrumb.Item href="/">Home</Breadcrumb.Item>
                <Breadcrumb.Item href="/codebases">Codebases</Breadcrumb.Item>
                <Breadcrumb.Item href={`/codebases/${codebaseName}`}>{codebaseName}</Breadcrumb.Item>
                <Breadcrumb.Item active>Strategies</Breadcrumb.Item>
            </Breadcrumb>
        );
    }

    function renderCreateStrategies() {
        return (
            <Form.Group as={Row} controlId="selectStrategy" className="align-items-center mb-3">
                <h4 className="mb-3 mt-3" style={{ color: "#666666" }}>
                    Similarity Generator and Clustering Algorithm
                </h4>
                <Col sm={2}>
                    <DropdownButton title={strategy === undefined? "Select Similarity Generator and Clustering Algorithm" : StrategyDescription[strategy.type]}>
                        {collectors.flatMap(collector => collector.possibleStrategies)
                            .filter((x, i, a) =>  a.indexOf(x) === i) // filter repeated values
                            .map(possibleStrategy =>
                                <Dropdown.Item
                                    key={possibleStrategy}
                                    onClick={() => handleSelectStrategy(possibleStrategy)}
                                >
                                    {StrategyDescription[possibleStrategy]}
                                </Dropdown.Item>
                            )
                        }
                    </DropdownButton>
                </Col>
            </Form.Group>
        );
    }

    function renderStrategies() {
        return <Row>
            <h4 className="mt-4" style={{ color: "#666666" }}>
                Strategies
            </h4>
            <div className={"d-flex flex-wrap"} style={{gap: '1rem 1rem'}}>
                {strategies.map(s => s.printCard(handleDeleteStrategy))}
            </div>

            <h4 style={{ color: "#666666" }}>
                Decompositions
            </h4>

            {
                //TODO: Decide what to do with the decomposition table
                //allDecompositions.length > 0 && <BootstrapTable bootstrap4 keyField='id' data={decompositionRows} columns={decompositionColumns} />
            }
        </Row>
    }

    return (
        <div>
            {renderBreadCrumbs()}

            <Form onSubmit={handleSubmit} className="mb-3">
                { renderCreateStrategies() }

                {/*Add render of each strategy like the next line to request the required elements for its creation*/}
                {
                    strategy !== undefined && strategy.type === StrategyType.ACCESSES_SCIPY &&
                    <AccessesSciPyForm
                        strategy={strategy}
                        setStrategy={setStrategy}
                    />
                }

                {
                    strategy !== undefined &&
                    <Form.Group as={Row} className="align-items-center">
                        <Col sm={{offset: 2}}>
                            <Button
                                type="submit"
                                disabled={isUploaded === "Uploading..." || !strategy.readyToSubmit()}
                            >
                                Create Strategy
                            </Button>
                            <Form.Text className="ms-2">
                                {isUploaded}
                            </Form.Text>
                        </Col>
                    </Form.Group>
                }

                {strategies.length !== 0 && renderStrategies()}
            </Form>
        </div>
    )
}