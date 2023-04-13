import React, {useEffect, useState} from "react";
import Form from "react-bootstrap/Form";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import Button from "react-bootstrap/Button";
import BootstrapTable from "react-bootstrap-table-next";
import filterFactory, {numberFilter} from "react-bootstrap-table2-filter";
import paginationFactory from "react-bootstrap-table2-paginator";
import {Modal, ModalBody, ModalFooter, ModalTitle} from "react-bootstrap";
import {APIService} from "../../../services/APIService";
import { RECOMMEND_MATRIX_FUNCTIONALITY_VECTORIZATION_CALLGRAPH } from "../../../models/recommendation/RecommendMatrixFunctionalityVectorizationByCallGraph";
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

export const RecommendMatrixSciPyFunctionalityVectorizationByCallGraph = ({codebaseName, strategy, setUpdateStrategies}) => {
    
    const service = new APIService();
    const [isUploaded, setIsUploaded] = useState("");
    const [linkageType, setLinkageType] = useState("average");
    const [showPopup, setShowPopup] = useState(false);
    const [recommendedDecompositions, setRecommendedDecompositions] = useState([]);
    const [selectedDecompositions, setSelectedDecompositions] = useState(undefined);
    const [loading, setLoading] = useState(false);
    const [isSelected, setIsSelected] = useState("");
    const [weightsList, setWeightsList] = useState([]);
    const [recommendation, setRecommendation] = useState(undefined);
    const [decompositionInfo, setDecompositionInfo] = useState([]);

    useEffect(() => {
        setWeightsList(WeightsFactory.getWeightListByStrategyType(strategy.strategyTypes));
    }, [])

    function handleChangeLinkageType(event) {
        setLinkageType(event.target.id);
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
            type: RECOMMEND_MATRIX_FUNCTIONALITY_VECTORIZATION_CALLGRAPH,
            strategyName: strategy.name,
            weightsList,
            linkageType
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
        ).catch(error => {
            console.error(error);
            setIsUploaded("Recommendation service failed.");
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

            <Form onSubmit={handleCreateRecommendation} className="mt-2 mb-3">
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
                            disabled={isUploaded === "Gathering recommendations..." || loading}
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
