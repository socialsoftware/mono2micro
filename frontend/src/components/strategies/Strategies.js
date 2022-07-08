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
import {StrategyFactory} from "../../models/strategies/StrategyFactory";
import {StrategyDescription, StrategySources, StrategyType} from "../../models/strategies/Strategy";
import {toast, ToastContainer} from "react-toastify";
import {RecommendAccessesSciPyStrategyForm} from "./forms/RecommendAccessesSciPyStrategyForm";

export const Strategies = () => {

    let { codebaseName } = useParams();

    const [sources, setSources] = useState([]);
    const [strategy, setStrategy] = useState(undefined);
    const [strategies, setStrategies] = useState([]);
    const [updateStrategies, setUpdateStrategies] = useState({});

    // Executes on mount
    useEffect(() => {
        const service = new RepositoryService();
        service.getSourceTypes(codebaseName).then(response => setSources(response.data));
    }, []);

    useEffect(() => {
        loadStrategies();
    }, [updateStrategies]);

    function loadStrategies() {
        const toastId = toast.loading("Fetching Strategies...");
        const service = new RepositoryService();
        service.getCodebaseStrategies(codebaseName).then(response => {
            setStrategies(response);
            toast.update(toastId, {type: toast.TYPE.SUCCESS, render: "Strategies Loaded.", isLoading: false});
            setTimeout(() => {toast.dismiss(toastId)}, 1000);
        }).catch(() => {
            toast.update(toastId, {type: toast.TYPE.ERROR, render: "Error Loading Strategies.", isLoading: false});
        });
    }

    function handleDeleteStrategy(strategy) {
        const service = new RepositoryService();

        service.deleteStrategy(strategy.name).then(() => loadStrategies());
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
                        {
                            Object.entries(StrategySources).filter(([type, strategySources]) => strategySources.reduce((prev, current) => prev && sources.includes(current), true))
                                .map(([type, sources]) =>
                                    <Dropdown.Item
                                        key={type}
                                        onClick={() => handleSelectStrategy(type)}
                                    >
                                        {StrategyDescription[type]}
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
            {strategy !== undefined && strategy.type === StrategyType.ACCESSES_SCIPY &&
                <AccessesSciPyStrategyForm
                    strategy={strategy}
                    setStrategy={setStrategy}
                    setUpdateStrategies={setUpdateStrategies}
                />
            }
            {strategy !== undefined && strategy.type === StrategyType.RECOMMENDATION_ACCESSES_SCIPY &&
                <RecommendAccessesSciPyStrategyForm
                    strategy={strategy}
                    setStrategy={setStrategy}
                    setUpdateStrategies={setUpdateStrategies}
                />
            }

            {strategies.length !== 0 && renderStrategies()}
        </div>
    )
}