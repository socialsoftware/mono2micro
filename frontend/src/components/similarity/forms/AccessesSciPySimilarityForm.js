import React, {useEffect, useState} from "react";
import Form from "react-bootstrap/Form";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import DropdownButton from "react-bootstrap/DropdownButton";
import Dropdown from "react-bootstrap/Dropdown";
import FormControl from "react-bootstrap/FormControl";
import {RepositoryService} from "../../../services/RepositoryService";
import {RepresentationType} from "../../../models/representation/Representation";
import Button from "react-bootstrap/Button";
import HttpStatus from "http-status-codes";
import filterFactory, {numberFilter, textFilter} from "react-bootstrap-table2-filter";
import paginationFactory from "react-bootstrap-table2-paginator";
import {Modal, ModalBody, ModalFooter, ModalTitle} from "react-bootstrap";
import BootstrapTable from "react-bootstrap-table-next";
import {TraceType} from "../../../type-declarations/types.d";
import {RecommendationFactory} from "../../../models/recommendation/RecommendationFactory";
import ButtonGroup from "react-bootstrap/ButtonGroup";
import {StrategyType} from "../../../models/strategy/Strategy";


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

export const AccessesSciPySimilarityForm = ({codebaseName, strategyName, setUpdateStrategies}) => {

    const service = new RepositoryService();
    const [isUploaded, setIsUploaded] = useState("");
    const [profiles, setProfiles] = useState([]);
    const [showPopup, setShowPopup] = useState(false);
    const [recommendedDecompositions, setRecommendedDecompositions] = useState([]);
    const [selectedDecompositions, setSelectedDecompositions] = useState(undefined);
    const [loading, setLoading] = useState(false);
    const [isSelected, setIsSelected] = useState("");
    const [accessMetricWeight, setAccessMetricWeight] = useState(25);
    const [writeMetricWeight, setWriteMetricWeight] = useState(25);
    const [readMetricWeight, setReadMetricWeight] = useState(25);
    const [sequenceMetricWeight, setSequenceMetricWeight] = useState(25);
    const [profile, setProfile] = useState("Generic")
    const [linkageType, setLinkageType] = useState("average");
    const [tracesMaxLimit, setTracesMaxLimit] = useState(0);
    const [traceType, setTraceType] = useState(TraceType.ALL);
    const [traceTypeCheckbox, setTraceTypeCheckbox] = useState(["ALL"]);
    const [linkageTypeCheckbox, setLinkageTypeCheckbox] = useState(["average"]);
    const [method, setMethod] = useState("similarity");
    const [recommendation, setRecommendation] = useState(undefined);

    // Executes when it is informed that there is information to be updated
    useEffect(() => {
        loadProfiles();
    }, [])

    function handleSubmit(event) {
        event.preventDefault();
        setIsUploaded("Uploading...");

        service.createSimilarity({
            strategyName,
            type: StrategyType.ACCESSES_SCIPY,
            accessMetricWeight,
            writeMetricWeight,
            readMetricWeight,
            sequenceMetricWeight,
            profile,
            linkageType,
            tracesMaxLimit,
            traceType,
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
        service.getCodebaseRepresentation(codebaseName, RepresentationType.ACCESSES)
            .then((response) => setProfiles(response.profiles));
    }

    function handleChangeAccessMetricWeight(event) {
        event.target.value === ''? setAccessMetricWeight(-1): setAccessMetricWeight(Number(event.target.value));
    }

    function handleChangeWriteMetricWeight(event) {
        event.target.value === ''? setWriteMetricWeight(-1): setWriteMetricWeight(Number(event.target.value));
    }

    function handleChangeReadMetricWeight(event) {
        event.target.value === ''? setReadMetricWeight(-1): setReadMetricWeight(Number(event.target.value));
    }

    function handleChangeSequenceMetricWeight(event) {
        event.target.value === ''? setSequenceMetricWeight(-1): setSequenceMetricWeight(Number(event.target.value));
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

    function handleChangeTraceTypeCheckbox(event) {
        setTraceTypeCheckbox(prev => {
            let next = [...prev];
            if (event.target.checked && !prev.includes(event.target.value))
                next.push(event.target.value);
            else next = prev.filter(l => l !== event.target.value);
            return next;
        });
    }

    function handleChangeLinkageTypeCheckbox(event) {
        setLinkageTypeCheckbox(prev => {
            let next = [...prev];
            if (event.target.checked && !next.includes(event.target.id))
                next.push(event.target.id);
            else next = next.filter(l => l !== event.target.id);
            return next;
        })
    }



    function handleCreateRecommendation() {
        setIsUploaded("Gathering recommendations...");
        setShowPopup(true);
        setLoading(true);

        service.recommendation(RecommendationFactory.getRecommendation({
            strategyName,
            type: StrategyType.ACCESSES_SCIPY,
            profile,
            linkageTypes: linkageTypeCheckbox,
            tracesMaxLimit,
            traceTypes: traceTypeCheckbox,
        }))
        .then(recommendation => {
            recommendation.traceTypes = traceTypeCheckbox;
            recommendation.linkageTypes = linkageTypeCheckbox;
            setRecommendation(recommendation);

            service.getRecommendationResult(recommendation.name)
                .then(list => {
                    const filteredList = list.filter(item =>
                        recommendation.traceTypes.includes(item.traceType) &&
                        recommendation.linkageTypes.includes(item.linkageType));

                    setRecommendedDecompositions(filteredList);
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

        <ButtonGroup variant="contained" aria-label="outlined primary button group">
            <Button onClick={() => setMethod("similarity")}>Similarity Distances Generation</Button>
            <Button onClick={() => setMethod("recommendation")}>Decomposition Recommendation</Button>
        </ButtonGroup>

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
            {method === "recommendation" &&
            <>
                <Form.Group as={Row} className="align-items-center mb-3">
                    <Form.Label as="legend" column sm={2}>
                        Type of traces
                    </Form.Label>
                    <Col sm={3} style={{ paddingLeft: 0 }}>
                        <Col sm="auto">
                            <Form.Check
                                onChange={handleChangeTraceTypeCheckbox}
                                name="traceType"
                                label="All"
                                type="checkbox"
                                id="allTraces"
                                value="ALL"
                                checked={traceTypeCheckbox.includes("ALL")}
                            />
                        </Col>
                        <Col sm="auto">
                            <Form.Check
                                onChange={handleChangeTraceTypeCheckbox}
                                name="traceType"
                                label="Longest"
                                type="checkbox"
                                id="longest"
                                value="LONGEST"
                                checked={traceTypeCheckbox.includes("LONGEST")}
                            />
                        </Col>
                        <Col sm="auto">
                            <Form.Check
                                onChange={handleChangeTraceTypeCheckbox}
                                name="traceType"
                                label="With more different accesses"
                                type="checkbox"
                                id="withMoreDifferentTraces"
                                value="WITH_MORE_DIFFERENT_ACCESSES"
                                checked={traceTypeCheckbox.includes("WITH_MORE_DIFFERENT_ACCESSES")}
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
                            onChange={handleChangeLinkageTypeCheckbox}
                            name="linkageType"
                            label="Average"
                            type="checkbox"
                            id="average"
                            checked={linkageTypeCheckbox.includes("average")}
                        />
                    </Col>
                    <Col sm="auto">
                        <Form.Check
                            onChange={handleChangeLinkageTypeCheckbox}
                            name="linkageType"
                            label="Single"
                            type="checkbox"
                            id="single"
                            checked={linkageTypeCheckbox.includes("single")}
                        />

                    </Col>
                    <Col sm="auto">
                        <Form.Check
                            onChange={handleChangeLinkageTypeCheckbox}
                            name="linkageType"
                            label="Complete"
                            type="checkbox"
                            id="complete"
                            checked={linkageTypeCheckbox.includes("complete")}
                        />
                    </Col>
                </Form.Group>
            </>
            }
            {method === "similarity" &&
            <>
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
                            value={accessMetricWeight === -1? '': accessMetricWeight}
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
                            value={writeMetricWeight === -1? '': writeMetricWeight}
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
                            value={readMetricWeight === -1? '': readMetricWeight}
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
                            value={sequenceMetricWeight === -1? '': sequenceMetricWeight}
                            onChange={handleChangeSequenceMetricWeight} />
                    </Col>
                </Form.Group>

                <Form.Group as={Row} className="align-items-center">
                    <Col sm={{offset: 2}}>
                        <Button
                            type="submit"
                            disabled={isUploaded === "Uploading..." ||
                                !(linkageType !== "" &&
                                accessMetricWeight >= 0 && writeMetricWeight >= 0 && readMetricWeight >= 0 && sequenceMetricWeight >= 0 &&
                                accessMetricWeight + writeMetricWeight + readMetricWeight + sequenceMetricWeight === 100 &&
                                profile !== "" &&
                                traceType !== "")
                            }
                        >
                            Generate Similarity Distances
                        </Button>
                        <Form.Text className="ms-2">
                            {isUploaded}
                        </Form.Text>
                    </Col>
                </Form.Group>
            </>
            }
            {method === "recommendation" &&
                <Form.Group as={Row} className="align-items-center">
                    <Col sm={{offset: 2}}>
                        <Button
                            disabled={isUploaded === "Gathering recommendations..." ||
                                !(linkageTypeCheckbox.length !== 0 && profile !== "" && traceTypeCheckbox.length !== 0) ||
                                loading}
                            onClick={handleCreateRecommendation}
                        >
                            Recommend Decompositions
                        </Button>
                        <Form.Text className="ms-2">
                            {isUploaded}
                        </Form.Text>
                    </Col>
                </Form.Group>
            }
        </Form>
        </>
    );
}