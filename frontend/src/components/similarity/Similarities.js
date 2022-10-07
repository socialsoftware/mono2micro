import React, {useEffect, useState} from 'react';
import Row from 'react-bootstrap/Row';
import Breadcrumb from 'react-bootstrap/Breadcrumb';

import {useParams} from "react-router-dom";
import {AccessesSciPySimilarityForm} from "./forms/AccessesSciPySimilarityForm";
import {RepositoryService} from "../../services/RepositoryService";
import {toast, ToastContainer} from "react-toastify";
import {StrategyType} from "../../models/strategy/Strategy";
import {RepositorySciPySimilarityForm} from "./forms/RepositorySciPySimilarityForm";
import {AccAndRepoSciPySimilarityForm} from "./forms/AccAndRepoSciPySimilarityForm";

export const Similarities = () => {

    let { codebaseName, strategyName } = useParams();

    const [strategy, setStrategy] = useState(undefined);
    const [similarities, setSimilarities] = useState([]);
    const [decompositions, setDecompositions] = useState([]);
    const [updateStrategies, setUpdateStrategies] = useState({});

    useEffect(() => {
        loadStrategy();
    }, [updateStrategies]);

    function loadStrategy() {
        const toastId = toast.loading("Fetching Strategy...");
        const service = new RepositoryService();
        service.getStrategy(strategyName).then(response => {
            setStrategy(response);

            loadDecompositions();
            if (response.hasSimilarities)
                loadSimilarities()

            toast.update(toastId, {type: toast.TYPE.SUCCESS, render: "Strategies Loaded.", isLoading: false});
            setTimeout(() => {toast.dismiss(toastId)}, 1000);
        }).catch(() => toast.update(toastId, {type: toast.TYPE.ERROR, render: "Error Loading Strategies.", isLoading: false}));
    }

    function loadDecompositions() {
        const toastId = toast.loading("Fetching Decompositions...");
        const service = new RepositoryService();
        service.getStrategyDecompositions(strategyName)
            .then(response => {
                setDecompositions(response);
                toast.update(toastId, {type: toast.TYPE.SUCCESS, render: "Decompositions Loaded.", isLoading: false});
                setTimeout(() => {toast.dismiss(toastId)}, 1000);
            })
            .catch((error) => {console.log(error);toast.update(toastId, {type: toast.TYPE.ERROR, render: "Error while loading strategy's decompositions."})});
    }

    function loadSimilarities() {
        const toastId = toast.loading("Fetching Similarities...");
        const service = new RepositoryService();
        service.getStrategySimilarities(strategyName)
            .then(response => {
                setSimilarities(response);
                toast.update(toastId, {type: toast.TYPE.SUCCESS, render: "Similarities Loaded.", isLoading: false});
                setTimeout(() => {toast.dismiss(toastId)}, 1000);
            })
            .catch(() => toast.update(toastId, {type: toast.TYPE.ERROR, render: "Error while loading strategy's similarity."}));
    }

    function handleDeleteSimilarity(similarity) {
        const toastId = toast.loading("Deleting " + similarity.name + "...");
        const service = new RepositoryService();

        service.deleteSimilarity(similarity.name).then(() => {
            loadSimilarities();
            loadDecompositions();
            toast.update(toastId, {type: toast.TYPE.SUCCESS, render: "Similarity deleted.", isLoading: false});
            setTimeout(() => {toast.dismiss(toastId)}, 1000);
        }).catch(() => {
            toast.update(toastId, {type: toast.TYPE.ERROR, render: "Error deleting " + similarity.name + ".", isLoading: false});
        });
    }

    function handleDeleteDecomposition(decompositionName) {
        const toastId = toast.loading("Deleting " + decompositionName + "...");
        const service = new RepositoryService();
        service.deleteDecomposition(decompositionName).then(() => {
            loadDecompositions();
            toast.update(toastId, {type: toast.TYPE.SUCCESS, render: "Decomposition deleted.", isLoading: false});
            setTimeout(() => {toast.dismiss(toastId)}, 1000);
        }).catch(() => {
            toast.update(toastId, {type: toast.TYPE.ERROR, render: "Error deleting " + decompositionName + ".", isLoading: false});
        });
    }

    function renderBreadCrumbs() {
        return (
            <Breadcrumb>
                <Breadcrumb.Item href="/">Home</Breadcrumb.Item>
                <Breadcrumb.Item href="/codebases">Codebases</Breadcrumb.Item>
                <Breadcrumb.Item href={`/codebases/${codebaseName}`}>{codebaseName}</Breadcrumb.Item>
                <Breadcrumb.Item active>{strategyName}</Breadcrumb.Item>
            </Breadcrumb>
        );
    }

    function renderSimilarities() {
        return <Row>
            <h4 className="mt-4" style={{ color: "#666666" }}>
                Similarity Distances
            </h4>
            <div className={"d-flex flex-wrap mw-100"} style={{gap: '1rem 1rem'}}>
                {similarities.map(similarity => similarity.printCard(handleDeleteSimilarity))}
            </div>
        </Row>
    }

    function renderDecompositions() {
        return <Row>
            <h4 className="mt-4" style={{ color: "#666666" }}>
                Decompositions
            </h4>
            <div className={"d-flex flex-wrap mw-100"} style={{gap: '1rem 1rem'}}>
                {decompositions.map(decomposition => decomposition.printCard(loadDecompositions, handleDeleteDecomposition))}
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

            {strategy !== undefined &&
                <>
                    {/*Add form of each similarity like the next block to request the required elements for its creation*/}
                    {strategy.type === StrategyType.ACCESSES_SCIPY &&
                        <>
                            <AccessesSciPySimilarityForm
                                codebaseName={codebaseName}
                                strategyName={strategyName}
                                setUpdateStrategies={setUpdateStrategies}
                            />
                            {similarities.length !== 0 && renderSimilarities()}
                        </>
                    }
                    {strategy.type === StrategyType.REPOSITORY_SCIPY &&
                        <>
                            <RepositorySciPySimilarityForm
                                strategyName={strategyName}
                                setUpdateStrategies={setUpdateStrategies}
                            />
                            {similarities.length !== 0 && renderSimilarities()}
                        </>
                    }
                    {strategy.type === StrategyType.ACC_AND_REPO_SCIPY &&
                        <>
                            <AccAndRepoSciPySimilarityForm
                                codebaseName={codebaseName}
                                strategyName={strategyName}
                                setUpdateStrategies={setUpdateStrategies}
                            />
                            {similarities.length !== 0 && renderSimilarities()}
                        </>
                    }

                    {decompositions.length !== 0 && renderDecompositions()}
                </>
            }
        </div>
    )
}