import React, {useEffect, useState} from "react";
import Form from "react-bootstrap/Form";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import {APIService} from "../../../services/APIService";
import Button from "react-bootstrap/Button";
import HttpStatus from "http-status-codes";
import {SIMILARITY_SCIPY_ENTITY_VECTORIZATION} from "../../../models/similarity/SimilarityScipyEntityVectorization";


export const SimilarityMatrixSciPyEntityVectorizationForm = ({codebaseName, strategy, setUpdateStrategies}) => {
    const service = new APIService();
    const [isUploaded, setIsUploaded] = useState("");
    const [linkageType, setLinkageType] = useState("average");

    // Executes when it is informed that there is information to be updated
    useEffect(() => {
    }, [])

    function handleSubmit(event) {
        event.preventDefault();
        setIsUploaded("Uploading...");

        service.createSimilarity({
            strategyName: strategy.name,
            type: SIMILARITY_SCIPY_ENTITY_VECTORIZATION,
            linkageType
        })
            .then(response => {
                if (response.status === HttpStatus.CREATED) {
                    setIsUploaded("");
                    setUpdateStrategies({}); // Calls function responsible for updating similarity
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

    function handleChangeLinkageType(event) {
        setLinkageType(event.target.id);
    }

    return (
        <>
            <Form onSubmit={handleSubmit} className="mt-2 mb-3">
                <Form.Group as={Row} className="align-items-center mb-3">
                    <Form.Label as="legend" column sm={2}>
                        Linkage Type
                    </Form.Label>
                    <Col sm="auto">
                        <Form.Check onClick={handleChangeLinkageType} name="linkageType" label="Average" type="radio" id="average" defaultChecked/>
                    </Col>
                    <Col sm="auto">
                        <Form.Check onClick={handleChangeLinkageType} name="linkageType" label="Single" type="radio" id="single"/>
                    </Col>
                    <Col sm="auto">
                        <Form.Check onClick={handleChangeLinkageType} name="linkageType" label="Complete" type="radio" id="complete"/>
                    </Col>
                </Form.Group>
                <Form.Group as={Row} className="align-items-center">
                    <Col sm={{offset: 2}}>
                        <Button
                            type="submit"
                            disabled={isUploaded === "Uploading..." || false}
                        >
                            Generate Similarity Distances
                        </Button>
                        <Form.Text className="ms-2">
                            {isUploaded}
                        </Form.Text>
                    </Col>
                </Form.Group>
            </Form>
        </>
    );
}