import React, {useEffect, useState} from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import Row from 'react-bootstrap/Row';
import Breadcrumb from 'react-bootstrap/Breadcrumb';
import {useParams} from "react-router-dom";
import {SciPyDecompositionForm} from "./forms/SciPyDecompositionForm";
import {toast, ToastContainer} from "react-toastify";
import {ACCESSES_SCIPY} from "../../models/decompositions/AccessesSciPyDecomposition";
import {REPOSITORY_SCIPY} from "../../models/decompositions/RepositorySciPyDecomposition";
import {ACC_AND_REPO_SCIPY} from "../../models/decompositions/AccAndRepoSciPyDecomposition";

export const Decompositions = () => {
    let { codebaseName, strategyName, similarityName } = useParams();
    const [similarity, setSimilarity] = useState({});
    const [decompositions, setDecompositions] = useState([]);

    //Executed on mount
    useEffect(() => {
        const service = new RepositoryService();
        service.getSimilarity(similarityName).then(response => {
            setSimilarity(response);
            loadDecompositions();
        });
    }, []);

    function loadDecompositions() {
        const service = new RepositoryService();
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

            {(similarity.decompositionType === ACCESSES_SCIPY || similarity.decompositionType === REPOSITORY_SCIPY || similarity.decompositionType === ACC_AND_REPO_SCIPY) &&
                <SciPyDecompositionForm
                    type={similarity.decompositionType}
                    loadDecompositions={loadDecompositions}
                />
            }

            {decompositions.length !== 0 &&
                <h4 style={{color: "#666666", marginTop: "16px"}}>
                    Decompositions
                </h4>
            }

            <Row className={"d-flex flex-wrap mw-100"} style={{gap: '1rem 1rem'}}>
                {decompositions.map(decomposition => decomposition.printCard(loadDecompositions, handleDeleteDecomposition))}
            </Row>
        </div>
    );
}