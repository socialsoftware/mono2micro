import React, {useEffect, useState} from 'react';
import Row from 'react-bootstrap/Row';
import Breadcrumb from 'react-bootstrap/Breadcrumb';

import {useParams} from "react-router-dom";
import {APIService} from "../../services/APIService";
import {toast, ToastContainer} from "react-toastify";
import {ACCESSES_SCIPY} from "../../models/decompositions/AccessesSciPyDecomposition";
import {REPOSITORY_SCIPY} from "../../models/decompositions/RepositorySciPyDecomposition";
import {ACC_AND_REPO_SCIPY} from "../../models/decompositions/AccAndRepoSciPyDecomposition";
import {SimilarityMatrixSciPyForm} from "./forms/SimilarityMatrixSciPyForm";

export const Similarities = () => {

    let { codebaseName, strategyName } = useParams();

    const [strategy, setStrategy] = useState(undefined);
    const [similarities, setSimilarities] = useState([]);
    const [updateStrategies, setUpdateStrategies] = useState({});

    useEffect(() => {
        loadStrategy();
    }, [updateStrategies]);

    function loadStrategy() {
        const toastId = toast.loading("Fetching Strategy...");
        const service = new APIService();
        service.getStrategy(strategyName).then(response => {
            setStrategy(response);
            loadSimilarities();

            toast.update(toastId, {type: toast.TYPE.SUCCESS, render: "Loaded Strategy.", isLoading: false});
            setTimeout(() => {toast.dismiss(toastId)}, 1000);
        }).catch(() => toast.update(toastId, {type: toast.TYPE.ERROR, render: "Error Loading Strategy.", isLoading: false}));
    }

    function loadSimilarities() {
        const toastId = toast.loading("Fetching Similarities...");
        const service = new APIService();
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
        const service = new APIService();

        service.deleteSimilarity(similarity.name).then(() => {
            loadSimilarities();
            toast.update(toastId, {type: toast.TYPE.SUCCESS, render: "Similarity deleted.", isLoading: false});
            setTimeout(() => {toast.dismiss(toastId)}, 1000);
        }).catch(() => {
            toast.update(toastId, {type: toast.TYPE.ERROR, render: "Error deleting " + similarity.name + ".", isLoading: false});
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

    return (
        <div style={{ paddingLeft: "2rem" }}>
            <ToastContainer
                position="top-center"
                theme="colored"
            />

            {renderBreadCrumbs()}

            {strategy !== undefined &&
                <>
                    {/*Add form of each similarity type like the next block to request the required elements for its creation*/}
                    {(strategy.decompositionType === ACCESSES_SCIPY ||
                        strategy.decompositionType === REPOSITORY_SCIPY ||
                        strategy.decompositionType === ACC_AND_REPO_SCIPY) &&
                        <>
                            <SimilarityMatrixSciPyForm
                                codebaseName={codebaseName}
                                strategy={strategy}
                                setUpdateStrategies={setUpdateStrategies}
                            />
                        </>
                    }
                    {similarities.length !== 0 && renderSimilarities()}
                </>
            }
        </div>
    )
}