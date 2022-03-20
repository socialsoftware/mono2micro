import React, {Fragment, useEffect, useState} from 'react';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Form from 'react-bootstrap/Form';
import Dropdown from 'react-bootstrap/Dropdown';
import DropdownButton from 'react-bootstrap/DropdownButton';
import Breadcrumb from 'react-bootstrap/Breadcrumb';

import {CLUSTERING_ALGORITHMS, POSSIBLE_DECOMPOSITIONS, SIMILARITY_GENERATORS} from '../../constants/constants';
import {useParams} from "react-router-dom";
import {AccessesSciPyStrategies} from "./AccessesSciPyStrategies";
import {RepositoryService} from "../../services/RepositoryService";
import HttpStatus from "http-status-codes";

export const Strategies = () => {

    let { codebaseName } = useParams();

    const [profiles, setProfiles] = useState([]);
    const [isUploaded, setIsUploaded] = useState("");
    const [update, requestUpdate] = useState(0);
    const [similarityGenerator, setSimilarityGenerator] = useState(undefined);
    const [clusteringAlgorithm, setClusteringAlgorithm] = useState(undefined);
    const [request, setRequest] = useState(undefined);

    // Executes on mount
    useEffect(() => {
        loadCodebase();
    }, []);

    function loadCodebase() {
        const service = new RepositoryService();
        service.getCodebase(
            codebaseName,
            ["profiles"]
        ).then(response => {
            if (response.data !== undefined) {
                //setProfiles(response.data.profiles);
                setProfiles(["Generic"]);
            }
        });
    }

    function handleSubmit(event) {
        event.preventDefault();
        setIsUploaded("Uploading...");

        const service = new RepositoryService();
        service.createStrategy(request)
            .then(response => {
                if (response.status === HttpStatus.CREATED) {
                    requestUpdate(update + 1); // Informs the Form that the information needs to be updated
                    setIsUploaded("Upload completed successfully.");
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

    function selectSimilarityGenerator(generator) {
        setSimilarityGenerator(generator);
        setClusteringAlgorithm(undefined);
        setRequest(undefined);
    }

    function selectClusteringAlgorithm(algorithm) {
        setClusteringAlgorithm(algorithm);
        setRequest(undefined);
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

    function renderSimilarityGenerator() {
        /*ADD NEW CLUSTERING ALGORITHMS AND SIMILARITY GENERATORS INTO THE POSSIBLE DECOMPOSITIONS*/
        return (
            <Form.Group as={Row} controlId="selectSimilarityGenerator" className="align-items-center mb-3">
                <h4 className="mb-3 mt-3" style={{ color: "#666666" }}>
                    Similarity Generator
                </h4>
                <Col sm={2}>
                    <DropdownButton title={similarityGenerator === undefined? "Select Similarity Generator" : similarityGenerator.name}>
                        {Object.values(SIMILARITY_GENERATORS)
                            .map(generator =>
                                <Dropdown.Item
                                    key={generator.value}
                                    onClick={() => selectSimilarityGenerator(generator)}
                                >
                                    {generator.name}
                                </Dropdown.Item>
                            )
                        }
                    </DropdownButton>
                </Col>
            </Form.Group>
        );
    }

    function renderClusteringAlgorithm() {
        /*ADD NEW CLUSTERING ALGORITHMS AND SIMILARITY GENERATORS INTO THE POSSIBLE DECOMPOSITIONS*/
        return (
            <Fragment>
                <h4 className="mb-3 mt-3" style={{ color: "#666666" }}>
                    Clustering Algorithm
                </h4>
                <Form.Group as={Row} controlId="selectClusteringAlgorithm" className="align-items-center mb-3">
                    <Col sm={2}>
                        <DropdownButton title={clusteringAlgorithm === undefined? "Select Clustering Algorithm" : clusteringAlgorithm.name}>
                            {POSSIBLE_DECOMPOSITIONS[similarityGenerator.value]
                                .map(algorithmValue => CLUSTERING_ALGORITHMS[algorithmValue])
                                .map(algorithm =>
                                <Dropdown.Item
                                    key={algorithm.value}
                                    onClick={() => selectClusteringAlgorithm(algorithm)}
                                >
                                    {algorithm.name}
                                </Dropdown.Item>
                            )}
                        </DropdownButton>
                    </Col>
                </Form.Group>
            </Fragment>
        );
    }

    return (
        <div>
            {renderBreadCrumbs()}

            <Form onSubmit={handleSubmit} className="mb-3">
                { renderSimilarityGenerator() }

                { similarityGenerator !== undefined && renderClusteringAlgorithm() }

                {/*Add render of each similarity generator like the next line to request the required elements for the dendrogram's creation*/}
                {
                    clusteringAlgorithm !== undefined && clusteringAlgorithm.hasDendrograms &&
                    similarityGenerator.value === "ACCESSES_LOG" && clusteringAlgorithm.value === "SCIPY" &&
                    <AccessesSciPyStrategies
                        codebaseName={codebaseName}
                        profiles={profiles}
                        isUploaded={isUploaded}
                        update={update}
                        setRequest={setRequest}
                    />
                }
            </Form>
        </div>
    )
}