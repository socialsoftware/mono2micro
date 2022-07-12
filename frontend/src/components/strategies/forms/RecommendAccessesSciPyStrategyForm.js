import filterFactory, {numberFilter, textFilter} from 'react-bootstrap-table2-filter';
import paginationFactory from "react-bootstrap-table2-paginator";
import React, {useEffect, useState} from "react";
import Form from "react-bootstrap/Form";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import DropdownButton from "react-bootstrap/DropdownButton";
import Dropdown from "react-bootstrap/Dropdown";
import Button from "react-bootstrap/Button";
import {Modal, ModalBody, ModalFooter, ModalTitle} from "react-bootstrap";
import BootstrapTable from "react-bootstrap-table-next";
import {RepositoryService} from "../../../services/RepositoryService";
import FormControl from "react-bootstrap/FormControl";
import {SourceType} from "../../../models/sources/Source";


const nFilter = numberFilter({placeholder: "filter"});
const tFilter = textFilter({placeholder: "filter"});
const sort = true;

const decompositionInfo = [
    {dataField: 'traceType',                text: 'Trace Type',         filter: tFilter, sort},
    {dataField: 'linkageType',              text: 'Linkage Type',       filter: tFilter, sort},
    {dataField: 'accessMetricWeight',       text: 'Access Metric',      filter: nFilter, sort},
    {dataField: 'writeMetricWeight',        text: 'Write Metric',       filter: nFilter, sort},
    {dataField: 'readMetricWeight',         text: 'Read Metric',        filter: nFilter, sort},
    {dataField: 'sequenceMetricWeight',     text: 'Sequence Metric',    filter: nFilter, sort},
    {dataField: 'numberOfClusters',         text: 'Number Of Clusters', filter: nFilter, sort},
    {dataField: 'maxClusterSize',           text: 'Max Cluster Size',   filter: nFilter, sort},
    {dataField: 'Complexity',               text: 'Complexity',         filter: nFilter, sort},
    {dataField: 'Cohesion',                 text: 'Cohesion',           filter: nFilter, sort},
    {dataField: 'Coupling',                 text: 'Coupling',           filter: nFilter, sort},
    {dataField: 'Performance',              text: 'Performance',        filter: nFilter, sort},
];

const pagination = paginationFactory({
    page: 2,
    sizePerPage: 5,
    sizePerPageList: [
        {text: '5', value: 5},
        {text: '10', value: 10},
        {text: '20', value: 20},
        {text: '50', value: 50},
        {text: '100', value: 100},
        {text: '200', value: 200},
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

export const RecommendAccessesSciPyStrategyForm = ({strategy, setStrategy, setUpdateStrategies}) => {

    const service = new RepositoryService();
    const [showPopup, setShowPopup] = useState(false);
    const [recommendedDecompositions, setRecommendedDecompositions] = useState([]);
    const [selectedDecompositions, setSelectedDecompositions] = useState(undefined);
    const [isSelected, setIsSelected] = useState("");
    const [isUploaded, setIsUploaded] = useState("");
    const [profiles, setProfiles] = useState([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        loadProfiles();
    }, [])

    function loadProfiles() {
        service.getCodebaseSource(strategy.codebaseName, SourceType.ACCESSES)
            .then((response) => setProfiles(response.profiles));
    }

    function handleCreateDecompositions() {
        if (selectedDecompositions.selectionContext.selected.length === 0) {
            setIsSelected("No decomposition was selected");
            return;
        }
        else setIsSelected("Creating decompositions...");

        service.createRecommendationDecompositions(strategy.name, selectedDecompositions.selectionContext.selected)
            .then(() => {
                setUpdateStrategies({});
                setIsSelected("Decompositions created.");
            }).catch(() => setIsSelected("Error during decomposition's creation."));
    }

    function handleCreateRecommendation() {
        setIsUploaded("Gathering recommendations...");
        setShowPopup(true);
        setLoading(true);

        service.recommendation(strategy)
            .then(recommendationStrategy => {
                recommendationStrategy.traceTypes = strategy.traceTypes;
                recommendationStrategy.linkageTypes = strategy.linkageTypes;
                setStrategy(recommendationStrategy);

                service.getRecommendationResult(recommendationStrategy.name)
                    .then(list => {
                        const filteredList = list.filter(item =>
                            recommendationStrategy.traceTypes.includes(item.traceType) &&
                            recommendationStrategy.linkageTypes.includes(item.linkageType));

                        setRecommendedDecompositions(filteredList);
                        setLoading(false);
                    });
                }
            );
    }

    function handleListRecommendations() {
        service.getRecommendationResult(strategy.name).then(list => {
            const filteredList = list.filter(item =>
                strategy.traceTypes.includes(item.traceType) &&
                strategy.linkageTypes.includes(item.linkageType));

            setRecommendedDecompositions(filteredList);
        });
    }

    function handleChangeLinkageTypeCheckbox(event) {
        let newStrategy = strategy.copy();
        if (event.target.checked && !newStrategy.linkageTypes.includes(event.target.id))
            newStrategy.linkageTypes.push(event.target.id);
        else newStrategy.linkageTypes = newStrategy.linkageTypes.filter(l => l !== event.target.id);

        setStrategy(newStrategy);
    }

    function handleChangeTraceTypeCheckbox(event) {
        let newStrategy = strategy.copy();
        if (event.target.checked && !newStrategy.traceTypes.includes(event.target.value))
            newStrategy.traceTypes.push(event.target.value);
        else newStrategy.traceTypes = newStrategy.traceTypes.filter(l => l !== event.target.value);

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
                </ModalBody>
                <ModalFooter>
                    <div className="flex-column">
                        <div className="flex-row">
                            <Button className="me-2" variant="success" onClick={() => handleCreateDecompositions()}>
                                Submit
                            </Button>
                            <Button className="me-2" variant="primary" disabled={strategy.isCompleted || loading} onClick={() => handleCreateRecommendation()}>
                                Refresh
                            </Button>
                            <Button className="me-2" variant="secondary" onClick={() => closePopup()}>
                                Close
                            </Button>
                        </div>
                        <div className="me-2" style={{textAlign: "right"}}>
                            <Form.Text>
                                {isSelected}
                            </Form.Text>
                        </div>
                    </div>
                </ModalFooter>
            </Modal>
        );
    }

    return (
        <Form className="mb-3">
            { renderRecommendationList() }

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
                            onClick={handleChangeTraceTypeCheckbox}
                            name="traceType"
                            label="All"
                            type="checkbox"
                            id="allTraces"
                            value="ALL"
                            defaultChecked
                        />
                    </Col>
                    <Col sm="auto">
                        <Form.Check
                            onClick={handleChangeTraceTypeCheckbox}
                            name="traceType"
                            label="Longest"
                            type="checkbox"
                            id="longest"
                            value="LONGEST"
                        />
                    </Col>
                    <Col sm="auto">
                        <Form.Check
                            onClick={handleChangeTraceTypeCheckbox}
                            name="traceType"
                            label="With more different accesses"
                            type="checkbox"
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
                        onClick={handleChangeLinkageTypeCheckbox}
                        name="linkageType"
                        label="Average"
                        type="checkbox"
                        id="average"
                        defaultChecked
                    />
                </Col>
                <Col sm="auto">
                    <Form.Check
                        onClick={handleChangeLinkageTypeCheckbox}
                        name="linkageType"
                        label="Single"
                        type="checkbox"
                        id="single"
                    />

                </Col>
                <Col sm="auto">
                    <Form.Check
                        onClick={handleChangeLinkageTypeCheckbox}
                        name="linkageType"
                        label="Complete"
                        type="checkbox"
                        id="complete"
                    />
                </Col>
            </Form.Group>
            <Form.Group as={Row} className="align-items-center">
                <Col sm={{offset: 2}}>
                    <Button
                        disabled={isUploaded === "Gathering recommendations..." || !strategy.readyToSubmit() || loading}
                        onClick={handleCreateRecommendation}
                    >
                        Show list of decompositions
                    </Button>
                    <Form.Text className="ms-2">
                        {isUploaded}
                    </Form.Text>
                </Col>
            </Form.Group>
        </Form>
    );
}