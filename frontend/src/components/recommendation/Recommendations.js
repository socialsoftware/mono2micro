import React, {useEffect, useState} from 'react';
import Row from 'react-bootstrap/Row';
import Breadcrumb from 'react-bootstrap/Breadcrumb';

import {useParams} from "react-router-dom";
import {APIService} from "../../services/APIService";
import {toast, ToastContainer} from "react-toastify";
import {RecommendMatrixSciPy} from "./forms/RecommendMatrixSciPy";
import { StrategyType } from '../../models/strategy/StrategyTypes';
import { RecommendationType } from '../../models/recommendation/RecommendationTypes';

export const Recommendations = () => {
    let { codebaseName, strategyName } = useParams();

    const [strategy, setStrategy] = useState(undefined);
    const [decompositions, setDecompositions] = useState([]);
    const [updateStrategies, setUpdateStrategies] = useState({});

    useEffect(() => {
        loadStrategy();
        loadDecompositions();
    }, [updateStrategies]);

    function loadStrategy() {
        const toastId = toast.loading("Fetching Strategy...");
        const service = new APIService();
        service.getStrategy(strategyName).then(response => {
            setStrategy(response);

            toast.update(toastId, {type: toast.TYPE.SUCCESS, render: "Loaded Strategy.", isLoading: false});
            setTimeout(() => {toast.dismiss(toastId)}, 1000);
        }).catch(() => toast.update(toastId, {type: toast.TYPE.ERROR, render: "Error Loading Strategy.", isLoading: false}));
    }

    function loadDecompositions() {
        const service = new APIService();
        const toastId = toast.loading("Fetching Decompositions...");
        service.getStrategyDecompositions(
            strategyName
        ).then(response => {
            setDecompositions(response);
            toast.update(toastId, {type: toast.TYPE.SUCCESS, render: "Decompositions Loaded.", isLoading: false});
            setTimeout(() => {toast.dismiss(toastId)}, 1000);
        }).catch(() => {
            toast.update(toastId, {type: toast.TYPE.ERROR, render: "Error Loading Decompositions.", isLoading: false});
        });
    }

    function handleDeleteDecomposition(decompositionName) {
        const toastId = toast.loading("Deleting " + decompositionName + "...");
        const service = new APIService();
        service.deleteDecomposition(decompositionName).then(() => {
            loadDecompositions();
            toast.update(toastId, {type: toast.TYPE.SUCCESS, render: "Decomposition deleted.", isLoading: false});
            setTimeout(() => {toast.dismiss(toastId)}, 1000);
        }).catch(() => {
            toast.update(toastId, {type: toast.TYPE.ERROR, render: "Error deleting " + decompositionName + ".", isLoading: false});
        });
    }

    function handleExportDecomposition(decompositionName) {
        const toastId = toast.loading("Exporting " + decompositionName + "...");
        const service = new APIService();
        service.exportDecomposition(decompositionName).then(response => {
            downloadDecompositionData(response);
            toast.update(toastId, {type: toast.TYPE.SUCCESS, render: "Decomposition exported.", isLoading: false});
            setTimeout(() => {toast.dismiss(toastId)}, 1000);
        }).catch(() => {
            toast.update(toastId, {type: toast.TYPE.ERROR, render: "Error exporting " + decompositionName + ".", isLoading: false});
        });
    }

    function downloadDecompositionData(response) {
        const url = window.URL.createObjectURL(
            new Blob([response.data], {type: "application/zip"})
        );

        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', 'm2m_decomposition_data.zip');
        document.body.appendChild(link);
        link.click();

        link.parentNode.removeChild(link);
        URL.revokeObjectURL(url);
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
                    {strategy.algorithmType === "SciPy Clustering" &&
                        strategy.strategyTypes.includes(StrategyType.ENTITY_VECTORIZATION_STRATEGY) ?
                        <RecommendMatrixSciPy
                            codebaseName={codebaseName}
                            strategy={strategy}
                            setUpdateStrategies={setUpdateStrategies}
                            recommendationType={RecommendationType.RECOMMEND_MATRIX_ENTITY_VECTORIZATION}
                        /> :
                    strategy.algorithmType === "SciPy Clustering" &&
                        strategy.strategyTypes.includes(StrategyType.CLASS_VECTORIZATION_STRATEGY) ?
                        <RecommendMatrixSciPy
                            codebaseName={codebaseName}
                            strategy={strategy}
                            setUpdateStrategies={setUpdateStrategies}
                            recommendationType={RecommendationType.RECOMMEND_MATRIX_CLASS_VECTORIZATION}
                        /> :
                    strategy.algorithmType === "SciPy Clustering" &&
                        strategy.strategyTypes.includes(StrategyType.FUNCTIONALITY_VECTORIZATION_CALLGRAPH_STRATEGY) ?
                        <RecommendMatrixSciPy
                            codebaseName={codebaseName}
                            strategy={strategy}
                            setUpdateStrategies={setUpdateStrategies}
                            recommendationType={RecommendationType.RECOMMEND_MATRIX_FUNCTIONALITY_VECTORIZATION_CALLGRAPH}
                        /> :
                    strategy.algorithmType === "SciPy Clustering" &&
                        strategy.strategyTypes.includes(StrategyType.FUNCTIONALITY_VECTORIZATION_ACCESSES_STRATEGY) ?
                        <RecommendMatrixSciPy
                            codebaseName={codebaseName}
                            strategy={strategy}
                            setUpdateStrategies={setUpdateStrategies}
                            recommendationType={RecommendationType.RECOMMEND_MATRIX_FUNCTIONALITY_VECTORIZATION_SEQUENCE_ACCESSES}
                        /> :
                    strategy.algorithmType === "SciPy Clustering" &&
                        <RecommendMatrixSciPy
                            codebaseName={codebaseName}
                            strategy={strategy}
                            setUpdateStrategies={setUpdateStrategies}
                            recommendationType={RecommendationType.RECOMMEND_MATRIX_SCIPY}
                        />
                    }

                    {decompositions.length !== 0 &&
                        <h4 style={{color: "#666666", marginTop: "16px"}}>
                            Decompositions
                        </h4>
                    }

                    <Row className={"d-flex flex-wrap mw-100"} style={{gap: '1rem 1rem'}}>
                        {decompositions.map(decomposition => decomposition.printCard(loadDecompositions, handleDeleteDecomposition, handleExportDecomposition))}
                    </Row>
                </>
            }
        </div>
    )
}
