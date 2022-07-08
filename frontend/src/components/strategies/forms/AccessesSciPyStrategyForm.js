import React, {useEffect, useState} from "react";
import Form from "react-bootstrap/Form";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import DropdownButton from "react-bootstrap/DropdownButton";
import Dropdown from "react-bootstrap/Dropdown";
import FormControl from "react-bootstrap/FormControl";
import {RepositoryService} from "../../../services/RepositoryService";
import {SourceType} from "../../../models/sources/Source";
import Button from "react-bootstrap/Button";
import HttpStatus from "http-status-codes";

export const AccessesSciPyStrategyForm = ({strategy, setStrategy, setUpdateStrategies}) => {

    const service = new RepositoryService();
    const [isUploaded, setIsUploaded] = useState("");
    const [profiles, setProfiles] = useState([]);

    // Executes when it is informed that there is information to be updated
    useEffect(() => {
        loadProfiles();
    }, [])

    function handleSubmit(event) {
        event.preventDefault();
        setIsUploaded("Uploading...");

        service.createAccessesSciPyStrategy(strategy)
            .then(response => {
                if (response.status === HttpStatus.CREATED) {
                    setIsUploaded("");
                    setStrategy(undefined);
                    setUpdateStrategies({}); // Calls function responsible for updating strategies
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
        service.getCodebaseSource(strategy.codebaseName, SourceType.ACCESSES)
            .then((response) => setProfiles(response.profiles));
    }

    function handleChangeAccessMetricWeight(event) {
        let newStrategy = strategy.copy();
        newStrategy.accessMetricWeight = event.target.value === ''? -1: Number(event.target.value);
        setStrategy(newStrategy);
    }

    function handleChangeWriteMetricWeight(event) {
        let newStrategy = strategy.copy();
        newStrategy.writeMetricWeight = event.target.value === ''? -1: Number(event.target.value);
        setStrategy(newStrategy);
    }

    function handleChangeReadMetricWeight(event) {
        let newStrategy = strategy.copy();
        newStrategy.readMetricWeight = event.target.value === ''? -1: Number(event.target.value);
        setStrategy(newStrategy);
    }

    function handleChangeSequenceMetricWeight(event) {
        let newStrategy = strategy.copy();
        newStrategy.sequenceMetricWeight = event.target.value === ''? -1: Number(event.target.value);
        setStrategy(newStrategy);
    }

    function handleChangeLinkageType(event) {
        let newStrategy = strategy.copy();
        newStrategy.linkageType = event.target.id;
        setStrategy(newStrategy);
    }

    function handleChangeProfile(profile) {
        let newStrategy = strategy.copy();
        newStrategy.profile = profile;
        setStrategy(newStrategy);
    }

    function handleChangeTracesMaxLimit(event) {
        let newStrategy = strategy.copy();
        newStrategy.tracesMaxLimit = Number(event.target.value);
        setStrategy(newStrategy);
    }

    function handleChangeTraceType(event) {
        let newStrategy = strategy.copy();
        newStrategy.traceType = event.target.value;
        setStrategy(newStrategy);
    }

    return (
        <Form onSubmit={handleSubmit} className="mb-3">
            <Form.Group as={Row} controlId="selectFunctionalityProfiles" className="align-items-center mb-3">
                <Form.Label column sm={2}>
                    Select Codebase Profile
                </Form.Label>
                <Col sm={2}>
                    <DropdownButton title={strategy.profile === ""? 'Functionality Profiles': strategy.profile}>
                        {profiles !== [] && Object.keys(profiles).map(profile =>
                            <Dropdown.Item
                                key={profile}
                                onClick={() => handleChangeProfile(profile)}
                                active={strategy.profile === profile}
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
                        value={strategy.tracesMaxLimit === 0? '' : strategy.tracesMaxLimit}
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
                        <Form.Check
                            onClick={handleChangeTraceType}
                            name="traceType"
                            label="All"
                            type="radio"
                            id="allTraces"
                            value="ALL"
                            defaultChecked
                        />
                    </Col>
                    <Col sm="auto">
                        <Form.Check
                            onClick={handleChangeTraceType}
                            name="traceType"
                            label="Longest"
                            type="radio"
                            id="longest"
                            value="LONGEST"
                        />
                    </Col>
                    <Col sm="auto">
                        <Form.Check
                            onClick={handleChangeTraceType}
                            name="traceType"
                            label="With more different accesses"
                            type="radio"
                            id="withMoreDifferentTraces"
                            value="WITH_MORE_DIFFERENT_ACCESSES"
                        />

                    </Col>
                </Col>
            </Form.Group>
            <Form.Group as={Row} className="align-items-center mb-3">
                <Form.Label as="legend" column sm={2}>
                    Linkage Type
                </Form.Label>
                <Col sm="auto">
                    <Form.Check
                        onClick={handleChangeLinkageType}
                        name="linkageType"
                        label="Average"
                        type="radio"
                        id="average"
                        defaultChecked
                    />
                </Col>
                <Col sm="auto">
                    <Form.Check
                        onClick={handleChangeLinkageType}
                        name="linkageType"
                        label="Single"
                        type="radio"
                        id="single"
                    />

                </Col>
                <Col sm="auto">
                    <Form.Check
                        onClick={handleChangeLinkageType}
                        name="linkageType"
                        label="Complete"
                        type="radio"
                        id="complete"
                    />
                </Col>
            </Form.Group>
            <Form.Group as={Row} controlId="access" className="align-items-center mb-3">
                <Form.Label column sm={2}>
                    Access Metric Weight (%)
                </Form.Label>
                <Col sm={2}>
                    <FormControl
                        type="number"
                        placeholder="0-100"
                        value={strategy.accessMetricWeight === -1? '': strategy.accessMetricWeight}
                        onChange={handleChangeAccessMetricWeight} />
                </Col>
            </Form.Group>

            <Form.Group as={Row} controlId="write" className="align-items-center mb-3">
                <Form.Label column sm={2}>
                    Write Metric Weight (%)
                </Form.Label>
                <Col sm={2}>
                    <FormControl
                        type="number"
                        placeholder="0-100"
                        value={strategy.writeMetricWeight === -1? '': strategy.writeMetricWeight}
                        onChange={handleChangeWriteMetricWeight} />
                </Col>
            </Form.Group>

            <Form.Group as={Row} controlId="read" className="align-items-center mb-3">
                <Form.Label column sm={2}>
                    Read Metric Weight (%)
                </Form.Label>
                <Col sm={2}>
                    <FormControl
                        type="number"
                        placeholder="0-100"
                        value={strategy.readMetricWeight === -1? '': strategy.readMetricWeight}
                        onChange={handleChangeReadMetricWeight} />
                </Col>
            </Form.Group>

            <Form.Group as={Row} controlId="sequence" className="align-items-center mb-3">
                <Form.Label column sm={2}>
                    Sequence Metric Weight (%)
                </Form.Label>
                <Col sm={2}>
                    <FormControl
                        type="number"
                        placeholder="0-100"
                        value={strategy.sequenceMetricWeight === -1? '': strategy.sequenceMetricWeight}
                        onChange={handleChangeSequenceMetricWeight} />
                </Col>
            </Form.Group>
            <Form.Group as={Row} className="align-items-center">
                <Col sm={{offset: 2}}>
                    <Button
                        type="submit"
                        disabled={isUploaded === "Uploading..." || !strategy.readyToSubmit()}
                    >
                        Create Strategy
                    </Button>
                    <Form.Text className="ms-2">
                        {isUploaded}
                    </Form.Text>
                </Col>
            </Form.Group>
        </Form>
    );
}