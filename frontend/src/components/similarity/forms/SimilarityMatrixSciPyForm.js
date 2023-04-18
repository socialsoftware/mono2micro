import React, {useEffect, useState} from "react";
import Form from "react-bootstrap/Form";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import FormControl from "react-bootstrap/FormControl";
import {APIService} from "../../../services/APIService";
import Button from "react-bootstrap/Button";
import HttpStatus from "http-status-codes";
import DropdownButton from "react-bootstrap/DropdownButton";
import Dropdown from "react-bootstrap/Dropdown";
import {RepresentationFile} from "../../../models/representation/Representation";
import {TraceType, RepresentationInfoParameters} from "../../../type-declarations/types";
import {SIMILARITY_SCIPY_ACCESSES_REPOSITORY} from "../../../models/similarity/SimilarityScipyAccessesAndRepository";
import {WeightsFactory} from "../../../models/weights/WeightsFactory";


export const SimilarityMatrixSciPyForm = ({codebaseName, strategy, setUpdateStrategies}) => {
    const service = new APIService();
    const [isUploaded, setIsUploaded] = useState("");
    const [profiles, setProfiles] = useState([]);
    const [profile, setProfile] = useState("Generic")
    const [linkageType, setLinkageType] = useState("average");
    const [tracesMaxLimit, setTracesMaxLimit] = useState(0);
    const [traceType, setTraceType] = useState(TraceType.ALL);
    const [weightsList, setWeightsList] = useState([]);
    const [weightSum, setWeightSum] = useState(0);

    // Executes when it is informed that there is information to be updated
    useEffect(() => {
        loadProfiles();
        setWeightsList(WeightsFactory.getWeightListByStrategyType(strategy.strategyTypes));
    }, [])

    function handleSubmit(event) {
        event.preventDefault();
        setIsUploaded("Uploading...");

        service.createSimilarity({
            strategyName: strategy.name,
            type: SIMILARITY_SCIPY_ACCESSES_REPOSITORY,
            weightsList,
            profile,
            linkageType,
            tracesMaxLimit,
            traceType
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

    function loadProfiles() {
        service.getCodebaseRepresentation(codebaseName, RepresentationFile.ACCESSES)
            .then((response) => setProfiles(response.profiles));
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

    function handleChangeProfile(profile) {
        setProfile(profile);
    }

    function handleChangeTracesMaxLimit(event) {
        setTracesMaxLimit(Number(event.target.value));
    }

    function handleChangeTraceType(event) {
        setTraceType(event.target.value);
    }

    return (
        <>
            <Form onSubmit={handleSubmit} className="mt-2 mb-3">
                <Form.Group as={Row} controlId="selectFunctionalityProfiles" className="align-items-center mb-3">
                    <Form.Label column sm={2}>
                        Select Codebase Profile
                    </Form.Label>
                    <Col sm={2}>
                        <DropdownButton title={profile === ""? 'Functionality Profiles': profile}>
                            {profiles !== [] && Object.keys(profiles).map(profile =>
                                <Dropdown.Item
                                    key={profile}
                                    onClick={() => handleChangeProfile(profile)}
                                    active={profile === profile}
                                >
                                    {profile}
                                </Dropdown.Item>
                            )}
                        </DropdownButton>
                    </Col>
                </Form.Group>
                <Form.Group as={Row} controlId="amountOfTraces" className="mb-3">
                    <Form.Label column sm={2}>
                        Amount of Traces per Functionality
                    </Form.Label>
                    <Col sm={2}>
                        <FormControl
                            type="number"
                            placeholder="0 by default"
                            value={tracesMaxLimit === 0? '' : tracesMaxLimit}
                            onChange={handleChangeTracesMaxLimit}

                        />
                        <Form.Text className="text-muted">
                            If no number is inserted, 0 is assumed to be the default value meaning the maximum number of traces
                        </Form.Text>
                    </Col>
                </Form.Group>
                <Form.Group as={Row} className="align-items-center mb-3">
                    <Form.Label as="legend" column sm={2}>
                        Type of traces
                    </Form.Label>
                    <Col sm={3} style={{ paddingLeft: 0 }}>
                        <Col sm="auto">
                            <Form.Check onClick={handleChangeTraceType} name="traceType" label="All" type="radio" id="allTraces" value="ALL" defaultChecked/>
                        </Col>
                        <Col sm="auto">
                            <Form.Check onClick={handleChangeTraceType} name="traceType" label="Longest" type="radio" id="longest" value="LONGEST"/>
                        </Col>
                        <Col sm="auto">
                            <Form.Check onClick={handleChangeTraceType} name="traceType" label="With more different accesses" type="radio" id="withMoreDifferentTraces" value="WITH_MORE_DIFFERENT_ACCESSES"/>
                        </Col>
                    </Col>
                </Form.Group>
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
                            !(weightSum === 100 && profile !== "" && traceType !== "")
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