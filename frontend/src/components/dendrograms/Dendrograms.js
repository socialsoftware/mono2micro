import React, {useEffect, useState} from 'react';
import Row from 'react-bootstrap/Row';
import Breadcrumb from 'react-bootstrap/Breadcrumb';

import {useParams} from "react-router-dom";
import {AccessesSciPyDendrogramForm} from "./forms/AccessesSciPyDendrogramForm";
import {RepositoryService} from "../../services/RepositoryService";
import {toast, ToastContainer} from "react-toastify";
import {StrategyType} from "../../models/strategy/Strategy";

export const Dendrograms = () => {

    let { codebaseName, strategyName } = useParams();

    const [strategy, setStrategy] = useState(undefined);
    const [updateStrategies, setUpdateStrategies] = useState({});

    useEffect(() => {
        loadStrategy();
    }, [updateStrategies]);

    function loadStrategy() {
        const toastId = toast.loading("Fetching Strategy...");
        const service = new RepositoryService();
        service.getStrategy(strategyName).then(response => {
            setStrategy(response);
            toast.update(toastId, {type: toast.TYPE.SUCCESS, render: "Strategies Loaded.", isLoading: false});
            setTimeout(() => {toast.dismiss(toastId)}, 1000);
        }).catch(() => {
            toast.update(toastId, {type: toast.TYPE.ERROR, render: "Error Loading Strategies.", isLoading: false});
        });
    }

    function handleDeleteDendrogram(dendrogram) {
        const toastId = toast.loading("Deleting " + dendrogram.name + "...");
        const service = new RepositoryService();

        service.deleteDendrogram(dendrogram.name).then(() => {
            loadStrategy();
            toast.update(toastId, {type: toast.TYPE.SUCCESS, render: "Dendrogram deleted.", isLoading: false});
            setTimeout(() => {toast.dismiss(toastId)}, 1000);
        }).catch(() => {
            toast.update(toastId, {type: toast.TYPE.ERROR, render: "Error deleting " + dendrogram.name + ".", isLoading: false});
        });
    }

    function renderBreadCrumbs() {
        return (
            <Breadcrumb>
                <Breadcrumb.Item href="/">Home</Breadcrumb.Item>
                <Breadcrumb.Item href="/codebases">Codebases</Breadcrumb.Item>
                <Breadcrumb.Item href={`/codebases/${codebaseName}`}>{codebaseName}</Breadcrumb.Item>
                <Breadcrumb.Item href={`/codebases/${codebaseName}/strategies`}>Strategies</Breadcrumb.Item>
                <Breadcrumb.Item href={`/codebases/${codebaseName}/strategies/${strategyName}`}>{strategyName}</Breadcrumb.Item>
                <Breadcrumb.Item active>Dendrograms</Breadcrumb.Item>
            </Breadcrumb>
        );
    }

    function renderDendrograms() {
        return <Row>
            <h4 className="mt-4" style={{ color: "#666666" }}>
                Dendrograms
            </h4>
            <div className={"d-flex flex-wrap mw-100"} style={{gap: '1rem 1rem'}}>
                {strategy.dendrograms.map(s => s.printCard(handleDeleteDendrogram))}
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

            {/*Add render of each dendrogram like the next line to request the required elements for its creation*/}
            {strategy !== undefined && strategy.type === StrategyType.ACCESSES_SCIPY &&
                <AccessesSciPyDendrogramForm
                    codebaseName={codebaseName}
                    strategyName={strategyName}
                    setUpdateStrategies={setUpdateStrategies}
                />
            }

            {strategy.dendrograms.length !== 0 && renderDendrograms()}
        </div>
    )
}