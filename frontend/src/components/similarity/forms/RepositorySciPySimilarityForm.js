import React, {useState} from "react";
import Form from "react-bootstrap/Form";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import FormControl from "react-bootstrap/FormControl";
import {RepositoryService} from "../../../services/RepositoryService";
import Button from "react-bootstrap/Button";
import HttpStatus from "http-status-codes";
import filterFactory, {numberFilter} from "react-bootstrap-table2-filter";
import paginationFactory from "react-bootstrap-table2-paginator";
import {Modal, ModalBody, ModalFooter, ModalTitle} from "react-bootstrap";
import BootstrapTable from "react-bootstrap-table-next";
import ButtonGroup from "react-bootstrap/ButtonGroup";
import {StrategyType} from "../../../models/strategy/Strategy";
import {RecommendationFactory} from "../../../models/recommendation/RecommendationFactory";


const nFilter = numberFilter({placeholder: "filter"});
const sort = true;

const decompositionInfo = [
    {dataField: 'authorMetricWeight',           text: 'Author Metric',              filter: nFilter, sort},
    {dataField: 'commitMetricWeight',           text: 'Commit Metric',              filter: nFilter, sort},
    {dataField: 'numberOfClusters',             text: 'Number Of Clusters',         filter: nFilter, sort},
    {dataField: 'maxClusterSize',               text: 'Max Cluster Size',           filter: nFilter, sort},
    {dataField: 'Team Size Reduction Ratio',    text: 'Team Size Reduction Ratio',  filter: nFilter, sort},
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

export const RepositorySciPySimilarityForm = ({strategyName, setUpdateStrategies}) => {

    const service = new RepositoryService();
    const [isUploaded, setIsUploaded] = useState("");
    const [showPopup, setShowPopup] = useState(false);
    const [recommendedDecompositions, setRecommendedDecompositions] = useState([]);
    const [selectedDecompositions, setSelectedDecompositions] = useState(undefined);
    const [loading, setLoading] = useState(false);
    const [isSelected, setIsSelected] = useState("");
    const [authorMetricWeight, setAuthorMetricWeight] = useState(50);
    const [commitMetricWeight, setCommitMetricWeight] = useState(50);
    const [recommendation, setRecommendation] = useState(undefined);

    function handleSubmit(event) {
        event.preventDefault();
        setIsUploaded("Uploading...");

        service.createSimilarity({
            strategyName,
            type: StrategyType.REPOSITORY_SCIPY,
            authorMetricWeight,
            commitMetricWeight,
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

    function handleChangeAuthorMetricWeight(event) {
        event.target.value === ''? setAuthorMetricWeight(-1): setAuthorMetricWeight(Number(event.target.value));
    }

    function handleChangeCommitMetricWeight(event) {
        event.target.value === ''? setCommitMetricWeight(-1): setCommitMetricWeight(Number(event.target.value));
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


    function handleCreateRecommendation() {
        setIsUploaded("Gathering recommendations...");
        setShowPopup(true);
        setLoading(true);

        service.recommendation(RecommendationFactory.getRecommendation({
            strategyName,
            type: StrategyType.REPOSITORY_SCIPY,
        }))
            .then(recommendation => {
                setRecommendation(recommendation);

                service.getRecommendationResult(recommendation.name).then(list => {
                    setRecommendedDecompositions(list);
                    setLoading(false);
                });
            });
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
                <Button>Similarity Distances Generation</Button>
                <Button
                    disabled={isUploaded === "Gathering recommendations..." || loading}
                    onClick={handleCreateRecommendation}
                >
                    Decomposition Recommendation
                </Button>
            </ButtonGroup>

            <Form onSubmit={handleSubmit} className="mt-2 mb-3">
                <>
                    <Form.Group as={Row} controlId="author" className="align-items-center mb-3">
                        <Form.Label column sm={2}>
                            Author Metric Weight (%)
                        </Form.Label>
                        <Col sm={2}>
                            <FormControl
                                type="number"
                                placeholder="0-100"
                                value={authorMetricWeight === -1? '': authorMetricWeight}
                                onChange={handleChangeAuthorMetricWeight} />
                        </Col>
                    </Form.Group>

                    <Form.Group as={Row} controlId="commit" className="align-items-center mb-3">
                        <Form.Label column sm={2}>
                            Commit Metric Weight (%)
                        </Form.Label>
                        <Col sm={2}>
                            <FormControl
                                type="number"
                                placeholder="0-100"
                                value={commitMetricWeight === -1? '': commitMetricWeight}
                                onChange={handleChangeCommitMetricWeight} />
                        </Col>
                    </Form.Group>

                    <Form.Group as={Row} className="align-items-center">
                        <Col sm={{offset: 2}}>
                            <Button
                                type="submit"
                                disabled={isUploaded === "Uploading..." ||
                                    !( authorMetricWeight >= 0 && commitMetricWeight >= 0 &&
                                        authorMetricWeight + commitMetricWeight === 100)
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
            </Form>
        </>
    );
}
