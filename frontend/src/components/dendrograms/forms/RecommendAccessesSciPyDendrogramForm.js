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

export const RecommendAccessesSciPyDendrogramForm = ({strategy, setStrategy, setUpdateStrategies}) => {

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
        </Form>
    );
}