import React, {useEffect, useState} from 'react';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Form from 'react-bootstrap/Form';
import Dropdown from 'react-bootstrap/Dropdown';
import DropdownButton from 'react-bootstrap/DropdownButton';
import Breadcrumb from 'react-bootstrap/Breadcrumb';

import {useParams} from "react-router-dom";
import {AccessesSciPyStrategyForm} from "./forms/AccessesSciPyStrategyForm";
import {RepositoryService} from "../../services/RepositoryService";
import {CollectorFactory} from "../../models/collectors/CollectorFactory";
import {StrategyFactory} from "../../models/strategies/StrategyFactory";
import {StrategyDescription, StrategyType} from "../../models/strategies/Strategy";
import {toast, ToastContainer} from "react-toastify";

export const Strategies = () => {

    let { codebaseName } = useParams();

    const [collectors, setCollectors] = useState([]);
    const [strategy, setStrategy] = useState(undefined);
    const [strategies, setStrategies] = useState([]);
    const [updateStrategies, setUpdateStrategies] = useState({});

    // Executes on mount
    useEffect(() => {
        loadCollectors();
    }, []);

    useEffect(() => {
        loadStrategies();
    }, [updateStrategies]);

    function loadCollectors() {
        const service = new RepositoryService();
        service.getCodebase(codebaseName, ['collectors'])
            .then(response => {
                if (response.data !== undefined)
                    setCollectors(response.data.collectors.map(collector =>
                        CollectorFactory.getCollector({type: collector, codebaseName})));
            });
    }

    function loadStrategies() {
        const toastId = toast.loading("Fetching Strategies...");
        const allPossibleStrategies = collectors.flatMap(collector => collector.possibleStrategies)
            .filter((x, i, a) =>  a.indexOf(x) === i);

        const service = new RepositoryService();
        service.getStrategies(
            codebaseName,
            allPossibleStrategies
        ).then(response => {
            setStrategies(response);
            toast.update(toastId, {type: toast.TYPE.SUCCESS, render: "Strategies Loaded.", isLoading: false});
            setTimeout(() => {toast.dismiss(toastId)}, 1000);
        }).catch(() => {
            toast.update(toastId, {type: toast.TYPE.ERROR, render: "Error Loading Strategies.", isLoading: false});
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
            <Form.Group as={Row} controlId="selectStrategy" className="align-items-center mw-100 mb-3">
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
            <div className={"d-flex flex-wrap mw-100"} style={{gap: '1rem 1rem'}}>
                {strategies.map(s => s.printCard(handleDeleteStrategy))}
            </div>
        </Row>
    }

    return (
        <div style={{ paddingLeft: "2rem" }}>
            <ToastContainer
                position="top-center"
                theme="colored"
            />

            {renderBreadCrumbs()}

            { renderCreateStrategies() }

            {/*Add render of each strategy like the next line to request the required elements for its creation*/}
            {
                strategy !== undefined &&
                (strategy.type === StrategyType.ACCESSES_SCIPY || strategy.type === StrategyType.RECOMMENDATION_ACCESSES_SCIPY) &&
                <AccessesSciPyStrategyForm
                    strategy={strategy}
                    setStrategy={setStrategy}
                    setUpdateStrategies={setUpdateStrategies}
                />
            }

            {strategies.length !== 0 && renderStrategies()}
        </div>
    )
}