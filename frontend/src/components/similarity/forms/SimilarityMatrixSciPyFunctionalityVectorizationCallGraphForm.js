import React, {useEffect, useState} from "react";
import Form from "react-bootstrap/Form";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import FormControl from "react-bootstrap/FormControl";
import {APIService} from "../../../services/APIService";
import Button from "react-bootstrap/Button";
import HttpStatus from "http-status-codes";
import {SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_CALLGRAPH} from "../../../models/similarity/SimilarityScipyFunctionalityVectorizationByCallGraph";
import {RepresentationInfoParameters} from "../../../type-declarations/types";
import {WeightsFactory} from "../../../models/weights/WeightsFactory";


export const SimilarityMatrixSciPyFunctionalityVectorizationCallGraphForm = ({codebaseName, strategy, setUpdateStrategies}) => {
    const service = new APIService();
    const [isUploaded, setIsUploaded] = useState("");
    const [linkageType, setLinkageType] = useState("average");
    const [depth, setDepth] = useState(-1);
    const [weightsList, setWeightsList] = useState([]);
    const [weightSum, setWeightSum] = useState(0);

    // Executes when it is informed that there is information to be updated
    useEffect(() => {
        setWeightsList(WeightsFactory.getWeightListByStrategyType(strategy.strategyTypes));
    }, [])

    function handleSubmit(event) {
        event.preventDefault();
        setIsUploaded("Uploading...");

        service.createSimilarity({
            strategyName: strategy.name,
            type: SIMILARITY_SCIPY_FUNCTIONALITY_VECTORIZATION_CALLGRAPH,
            linkageType,
            depth,
            weightsList
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

    function changeMetricWeight(event, weightName) {
        let sum = 0;
        setWeightsList(prev => {
            return prev.map(weight => {
                if (weight[weightName] || weight[weightName] === 0)
                    weight[weightName] = event.target.value === 0 ? -1 : Number(event.target.value);
                return weight;
            });
        });
        setWeightSum(sum);
    }

    useEffect(() => {
        let sum = 0;
        weightsList.forEach(weight => Object.keys(weight.weightsLabel).forEach(key => sum += weight[key]));
        setWeightSum(sum);
    }, [weightsList]);

    function handleChangeLinkageType(event) {
        setLinkageType(event.target.id);
    }

    function handleChangeDepth(event) {
        setDepth(Number(event.target.value));
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
                <Form.Group as={Row} className="align-items-center mb-3">
                    <Form.Label as="legend" column sm={2}>
                        Max Depth
                    </Form.Label>
                    <Col sm={2}>
                        <FormControl
                            type="number"
                            placeholder="1-N"
                            value={depth === -1 ? '' : depth}
                            onChange={(event) => handleChangeDepth(event)} />
                    </Col>
                </Form.Group>
                {weightsList.flatMap(weight => Object.entries(weight.weightsLabel).map(([key, value]) =>
                    <Form.Group as={Row} key={key} controlId={key} className="align-items-center mb-3">
                        <Form.Label column sm={2}>
                            {value} (%)
                        </Form.Label>
                        <Col sm={2}>
                            <FormControl
                                type="number"
                                placeholder="0-100"
                                value={weight[key] === -1? '': weight[key]}
                                onChange={(event) => changeMetricWeight(event, key)} />
                        </Col>
                    </Form.Group>)
                )}
                <Form.Group as={Row} className="align-items-center">
                    <Col sm={{offset: 2}}>
                        <Button
                            type="submit"
                            disabled={isUploaded === "Uploading..." ||
                            !(weightSum === 100 && depth > 0 )
                            }
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