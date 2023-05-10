import React, {useEffect, useState} from 'react';
import { APIService } from '../../services/APIService';
import Row from 'react-bootstrap/Row';
import Breadcrumb from 'react-bootstrap/Breadcrumb';
import {useParams} from "react-router-dom";
import {SciPyDecompositionForm} from "./forms/SciPyDecompositionForm";
import {toast, ToastContainer} from "react-toastify";
import {SIMILARITY_SCIPY_ACCESSES_REPOSITORY} from "../../models/similarity/SimilarityScipyAccessesAndRepository";
import {SIMILARITY_SCIPY_ENTITY_VECTORIZATION} from "../../models/similarity/SimilarityScipyEntityVectorization";
import {SIMILARITY_SCIPY_CLASS_VECTORIZATION} from "../../models/similarity/SimilarityScipyClassVectorization";
import {SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_CALLGRAPH} from "../../models/similarity/SimilarityScipyFunctionalityVectorizationByCallGraph";
import {SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_SEQUENCE_ACCESSES} from "../../models/similarity/SimilarityScipyFunctionalityVectorizationBySequenceOfAccesses";

export const Decompositions = () => {
    let { codebaseName, strategyName, similarityName } = useParams();
    const [similarity, setSimilarity] = useState({});
    const [decompositions, setDecompositions] = useState([]);

    //Executed on mount
    useEffect(() => {
        const service = new APIService();
        service.getSimilarity(similarityName).then(response => {
            setSimilarity(response);
            loadDecompositions();
        });
    }, []);

    function loadDecompositions() {
        const service = new APIService();
        const toastId = toast.loading("Fetching Decompositions...");
        service.getDecompositions(
            similarityName
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
            new Blob([response.data], {type: "application/json"})
        );

        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', 'm2m_decomposition_data.json');
        document.body.appendChild(link);
        link.click();

        link.parentNode.removeChild(link);
        URL.revokeObjectURL(url);
    }

    function renderBreadCrumbs() {
        return (
            <Breadcrumb>
                <Breadcrumb.Item href="/">
                    Home
                </Breadcrumb.Item>
                <Breadcrumb.Item href="/codebases">
                    Codebases
                </Breadcrumb.Item>
                <Breadcrumb.Item href={`/codebases/${codebaseName}`}>
                    {codebaseName}
                </Breadcrumb.Item>
                <Breadcrumb.Item href={`/codebases/${codebaseName}/${strategyName}/similarity`}>
                    {strategyName}
                </Breadcrumb.Item>
                <Breadcrumb.Item active>
                    {similarityName}
                </Breadcrumb.Item>
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

            <h4 style={{color: "#666666"}}>
                Decomposition Creation Method
            </h4>

            {similarity.type === SIMILARITY_SCIPY_ACCESSES_REPOSITORY &&
                <SciPyDecompositionForm loadDecompositions={loadDecompositions}/>
            }

            {similarity.type === SIMILARITY_SCIPY_ENTITY_VECTORIZATION &&
                <SciPyDecompositionForm loadDecompositions={loadDecompositions}/>
            }

            {similarity.type === SIMILARITY_SCIPY_CLASS_VECTORIZATION &&
                <SciPyDecompositionForm loadDecompositions={loadDecompositions}/>
            }

            {similarity.type === SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_CALLGRAPH &&
                <SciPyDecompositionForm loadDecompositions={loadDecompositions}/>
            }

            {similarity.type === SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_SEQUENCE_ACCESSES &&
                <SciPyDecompositionForm loadDecompositions={loadDecompositions}/>
            }

            {decompositions.length !== 0 &&
                <h4 style={{color: "#666666", marginTop: "16px"}}>
                    Decompositions
                </h4>
            }

            <Row className={"d-flex flex-wrap mw-100"} style={{gap: '1rem 1rem'}}>
                {decompositions.map(decomposition => decomposition.printCard(loadDecompositions, handleDeleteDecomposition, handleExportDecomposition))}
            </Row>
        </div>
    );
}