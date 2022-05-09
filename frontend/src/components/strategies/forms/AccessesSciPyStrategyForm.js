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
import ButtonGroup from "react-bootstrap/ButtonGroup";
import {StrategyType} from "../../../models/strategies/Strategy";
import {StrategyFactory} from "../../../models/strategies/StrategyFactory";
import {Modal, ModalBody, ModalFooter, ModalTitle} from "react-bootstrap";
import BootstrapTable from "react-bootstrap-table-next";
import paginationFactory from "react-bootstrap-table2-paginator";
import filterFactory, {numberFilter} from 'react-bootstrap-table2-filter';

const filter = numberFilter({placeholder: "filter"});
const sort = true;

const decompositionInfo = [
    {dataField: 'accessMetricWeight', text: 'Access Metric', filter, sort},
    {dataField: 'writeMetricWeight', text: 'Write Metric', filter, sort},
    {dataField: 'readMetricWeight', text: 'Read Metric', filter, sort},
    {dataField: 'sequenceMetricWeight', text: 'Sequence Metric', filter, sort},
    {dataField: 'numberOfClusters', text: 'Number Of Clusters', filter, sort},
    {dataField: 'maxClusterSize', text: 'Max Cluster Size', filter, sort},
    {dataField: 'Complexity', text: 'Complexity', filter, sort},
    {dataField: 'Cohesion', text: 'Cohesion', filter, sort},
    {dataField: 'Coupling', text: 'Coupling', filter, sort},
    {dataField: 'Performance', text: 'Performance', filter, sort},
    {dataField: 'Silhouette Score', text: 'Silhouette Score', filter, sort},
];

const pagination = paginationFactory({
    page: 2,
    sizePerPage: 5,
    sizePerPageList: [
        {text: '5', value: 5},
        {text: '10', value: 10},
        {text: '15', value: 15},
    ],
    lastPageText: '>>',
    firstPageText: '<<',
    nextPageText: '>',
    prePageText: '<',
    showTotal: true,
    alwaysShowAllBtns: true,
});

const selectRow = {
    mode: 'checkbox',
    clickToSelect: true
};

export const AccessesSciPyStrategyForm = ({strategy, setStrategy, setUpdateStrategies}) => {

    const service = new RepositoryService();
    const [showPopup, setShowPopup] = useState(false);
    const [isUploaded, setIsUploaded] = useState("");
    const [profiles, setProfiles] = useState([]);
    const [typeOfSubmission, setTypeOfSubmission] = useState(undefined);
    const [recommendedDecompositions, setRecommendedDecompositions] = useState([]);
    const [selectedDecompositions, setSelectedDecompositions] = useState(undefined);
    const [isSelected, setIsSelected] = useState("");

    // Executes when it is informed that there is information to be updated
    useEffect(() => {
        loadProfiles();
    }, [])

    function handleSubmit(event) {
        event.preventDefault();
        setIsUploaded("Uploading...");

        service.createStrategy(strategy)
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
        service.getSource(strategy.codebaseName, SourceType.ACCESSES)
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

    function handleRecommendation() {
        if (strategy.type === StrategyType.ACCESSES_SCIPY)
            setStrategy(StrategyFactory.getStrategy({type: StrategyType.RECOMMENDATION_ACCESSES_SCIPY, codebaseName: strategy.codebaseName}));

        setTypeOfSubmission("recommend");
    }

    function handleManualCreation() {
        if (strategy.type === StrategyType.RECOMMENDATION_ACCESSES_SCIPY)
            setStrategy(StrategyFactory.getStrategy({type: StrategyType.ACCESSES_SCIPY, codebaseName: strategy.codebaseName}));

        setTypeOfSubmission("manual");
    }

    function handleListRecommendations() {
        setIsUploaded("Gathering recommendations...");
        setShowPopup(true);

        service.recommendation(strategy.codebaseName, strategy)
            .then(recommendationStrategy => {
                setStrategy(recommendationStrategy);

                service.getRecommendationResult(recommendationStrategy.codebaseName, recommendationStrategy.name)
                    .then(list => setRecommendedDecompositions(list));
            }
        );
    }

    function reloadRecommendationList() {
        service.getRecommendationResult(strategy.codebaseName, strategy.name)
            .then(list => setRecommendedDecompositions(list));
    }

    function handleCreateDecompositions() {
        if (selectedDecompositions.selectionContext.selected.length === 0)
            setIsSelected("No decomposition was selected");
        else setIsSelected("Creating decompositions...");

        service.createRecommendationDecompositions(strategy.codebaseName, strategy.name, selectedDecompositions.selectionContext.selected)
            .then(() => setUpdateStrategies({})).catch(() => setIsSelected("Error during decomposition's creation."));

        setIsSelected("Decompositions created.")
    }

    function renderRecommendationList() {
        const closePopup = function () {setShowPopup(false); setIsSelected(""); setIsUploaded("")};

        return (
            <Modal
                id="modal-table"
                show={showPopup}
                onHide={() => closePopup()}
                backdrop="static"
            >
                <ModalTitle>&ensp;Recommendation List</ModalTitle>
                <ModalBody>
                    This list can be refreshed to display more decompositions.<br/>
                    Click in the <em>Refresh</em> button to update the list.<br/>
                    Click in the <em>Submit</em> button to create de decompositions.
                </ModalBody>
                <ModalFooter>
                    <BootstrapTable
                        bootstrap4
                        keyField='name'
                        data={recommendedDecompositions}
                        columns={decompositionInfo}
                        pagination={pagination}
                        filter={filterFactory()}
                        selectRow={selectRow}
                        ref={n => setSelectedDecompositions(n)}
                    />
                    <Button className="mt-0 mb-3" variant="success" onClick={() => handleCreateDecompositions()}>
                        Submit
                    </Button>
                    <Button className="mt-0 mb-3" variant="primary" onClick={() => reloadRecommendationList()}>
                        Refresh
                    </Button>
                    <Button className="mt-0 mb-3" variant="secondary" onClick={() => closePopup()}>
                        Close
                    </Button>
                    <Form.Text className="ms-2">
                        {isSelected}
                    </Form.Text>
                </ModalFooter>
            </Modal>
        );
    }

    return (
        <Form onSubmit={handleSubmit} className="mb-3">
            { renderRecommendationList() }

            <ButtonGroup className="mb-3">
                <Button
                    onClick={handleRecommendation}
                >
                    Recommend Decompositions
                </Button>
                <Button
                    onClick={handleManualCreation}
                >
                    Manually Create Strategy
                </Button>
            </ButtonGroup>

            {typeOfSubmission !== undefined &&
                <React.Fragment>
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
                </React.Fragment>
            }

            {typeOfSubmission === "recommend" &&
                <React.Fragment>
                    <Form.Group as={Row} className="align-items-center">
                        <Col sm={{offset: 2}}>
                            <Button
                                disabled={isUploaded === "Gathering recommendations..." || !strategy.readyToSubmit()}
                                onClick={handleListRecommendations}
                            >
                                Show list of decompositions
                            </Button>
                            <Form.Text className="ms-2">
                                {isUploaded}
                            </Form.Text>
                        </Col>
                    </Form.Group>
                </React.Fragment>
            }

            {typeOfSubmission === "manual" &&
                <React.Fragment>
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
                </React.Fragment>
            }
        </Form>
    );
}