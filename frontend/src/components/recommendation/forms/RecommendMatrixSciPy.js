import React, {useEffect, useState} from "react";
import Form from "react-bootstrap/Form";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import FormControl from "react-bootstrap/FormControl";
import {APIService} from "../../../services/APIService";
import Button from "react-bootstrap/Button";
import filterFactory, {numberFilter} from "react-bootstrap-table2-filter";
import paginationFactory from "react-bootstrap-table2-paginator";
import {Modal, ModalBody, ModalFooter, ModalTitle} from "react-bootstrap";
import BootstrapTable from "react-bootstrap-table-next";
import DropdownButton from "react-bootstrap/DropdownButton";
import Dropdown from "react-bootstrap/Dropdown";
import {RepresentationFile} from "../../../models/representation/Representation";
import {TraceType} from "../../../type-declarations/types";
import {WeightsFactory} from "../../../models/weights/WeightsFactory";


const nFilter = numberFilter({placeholder: "filter"});
const sort = true;

const columns = [
    {dataField: 'accessMetricWeight',           text: 'Access Weight',              filter: nFilter, sort},
    {dataField: 'writeMetricWeight',            text: 'Write Weight',               filter: nFilter, sort},
    {dataField: 'readMetricWeight',             text: 'Read Weight',                filter: nFilter, sort},
    {dataField: 'sequenceMetricWeight',         text: 'Sequence Weight',            filter: nFilter, sort},
    {dataField: 'authorMetricWeight',           text: 'Author Weight',              filter: nFilter, sort},
    {dataField: 'commitMetricWeight',           text: 'Commit Weight',              filter: nFilter, sort},
    {dataField: 'controllersWeight',            text: 'Controllers Weight',         filter: nFilter, sort},
    {dataField: 'servicesWeight',               text: 'Services Weight',            filter: nFilter, sort},
    {dataField: 'intermediateMethodsWeight',    text: 'Intermediate Methods Weight',filter: nFilter, sort},
    {dataField: 'entitiesWeight',               text: 'Entities Weight',            filter: nFilter, sort},
    {dataField: 'numberOfClusters',             text: 'Number Of Clusters',         filter: nFilter, sort},
    {dataField: 'maxClusterSize',               text: 'Max Cluster Size',           filter: nFilter, sort},
    {dataField: 'Complexity',                   text: 'Complexity',                 filter: nFilter, sort},
    {dataField: 'Cohesion',                     text: 'Cohesion',                   filter: nFilter, sort},
    {dataField: 'Coupling',                     text: 'Coupling',                   filter: nFilter, sort},
    {dataField: 'Performance',                  text: 'Performance',                filter: nFilter, sort},
    {dataField: 'TSR',                          text: 'Team Size Reduction Ratio',  filter: nFilter, sort},
];

const pagination = paginationFactory({
    page: 1,
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

export const RecommendMatrixSciPy = ({codebaseName, strategy, setUpdateStrategies, recommendationType}) => {

    const service = new APIService();
    const [isUploaded, setIsUploaded] = useState("");
    const [profiles, setProfiles] = useState([]);
    const [profile, setProfile] = useState("Generic")
    const [linkageType, setLinkageType] = useState("average");
    const [tracesMaxLimit, setTracesMaxLimit] = useState(0);
    const [traceType, setTraceType] = useState(TraceType.ALL);
    const [showPopup, setShowPopup] = useState(false);
    const [recommendedDecompositions, setRecommendedDecompositions] = useState([]);
    const [selectedDecompositions, setSelectedDecompositions] = useState(undefined);
    const [loading, setLoading] = useState(false);
    const [isSelected, setIsSelected] = useState("");
    const [weightsList, setWeightsList] = useState([]);
    const [recommendation, setRecommendation] = useState(undefined);
    const [decompositionInfo, setDecompositionInfo] = useState([]);

    // Executes when it is informed that there is information to be updated
    useEffect(() => {
        loadProfiles();
        setWeightsList(WeightsFactory.getWeightListByStrategyType(strategy.strategyTypes));
    }, [])

    function loadProfiles() {
        service.getCodebaseRepresentation(codebaseName, RepresentationFile.ACCESSES)
            .then((response) => setProfiles(response.profiles));
    }

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

    function handleCreateDecompositions() {
        if (selectedDecompositions.selectionContext.selected.length === 0) {
            setIsSelected("No decomposition was selected");
            return;
        }
        else setIsSelected("Creating decomposition...");

        service.createRecommendationDecompositions(recommendation.name, selectedDecompositions.selectionContext.selected)
            .then(() => {
                setUpdateStrategies({});
                setIsSelected("Decompositions created.");
            }).catch(() => setIsSelected("Error during decomposition's creation."));
    }


    function handleCreateRecommendation(event) {
        if (event !== undefined)
            event.preventDefault();
        setIsUploaded("Gathering recommendations...");
        setLoading(true);

        service.recommendation({
            type: recommendationType,
            strategyName: strategy.name,
            weightsList,
            profile,
            linkageType,
            tracesMaxLimit,
            traceType
        }).then(recommendation => {
                setRecommendation(recommendation);

                service.getRecommendationResult(recommendation.name).then(list => {
                    if (list.length !== 0) {
                        let line = Object.keys(list[0]);
                        setDecompositionInfo(columns.filter(column => line.includes(column.dataField)));
                    }
                    else setDecompositionInfo(columns);
                    setRecommendedDecompositions(list);

                    setShowPopup(true);
                    setLoading(false);
                });
            }
        );
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
                    Select the desired decompositions and click in the <em>Create</em> button to create de decompositions.
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
                                Create
                            </Button>
                            <Button className="me-2" variant="primary" disabled={recommendation !== undefined && recommendation.isCompleted || loading}
                                    onClick={() => handleCreateRecommendation()}>
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
        <>
            { renderRecommendationList() }

            <Form onSubmit={handleCreateRecommendation} className="mt-2 mb-3">
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
                <Form.Group as={Row} className="align-items-center">
                    <Col sm={{offset: 2}}>
                        <Button
                            type="submit"
                            disabled={isUploaded === "Gathering recommendations..." || !(profile !== "") || loading}
                        >
                            Recommend Decompositions
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
