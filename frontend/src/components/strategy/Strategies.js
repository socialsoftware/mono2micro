import React, {Fragment, useEffect, useState} from 'react';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Form from 'react-bootstrap/Form';
import Dropdown from 'react-bootstrap/Dropdown';
import DropdownButton from 'react-bootstrap/DropdownButton';
import Breadcrumb from 'react-bootstrap/Breadcrumb';

import {POSSIBLE_STRATEGIES} from '../../constants/constants';
import {useParams} from "react-router-dom";
import {AccessesSciPyStrategies} from "./AccessesSciPyStrategies";
import {RepositoryService} from "../../services/RepositoryService";
import HttpStatus from "http-status-codes";
import {CollectorFactory} from "../../models/collectors/CollectorFactory";

export const Strategies = () => {

    let { codebaseName } = useParams();

    const [profiles, setProfiles] = useState([]);
    const [isUploaded, setIsUploaded] = useState("");
    const [update, requestUpdate] = useState(0);
    const [collectors, setCollectors] = useState([]);
    const [strategy, setStrategy] = useState(undefined);
    const [request, setRequest] = useState(undefined);

    // Executes on mount
    useEffect(() => {
        loadCodebase();
        loadCollectors();
    }, []);

    function loadCodebase() {
        const service = new RepositoryService();
        service.getCodebase(
            codebaseName,
            ["profiles"]
        ).then(response => {
            if (response.data !== undefined) {
                //setProfiles(response.data.profiles);
                setProfiles(["Generic"]);
            }
        });
    }

    function loadCollectors() {
        const service = new RepositoryService();
        service.getCodebase(codebaseName, ['collectors'])
            .then(response => {
                if (response.data !== undefined)
                    setCollectors(response.data.collectors.map(collector =>
                        CollectorFactory.getCollector(codebaseName, collector))
                    );
            });
    }

    function handleSubmit(event) {
        event.preventDefault();
        setIsUploaded("Uploading...");

        const service = new RepositoryService();
        service.createStrategy(request)
            .then(response => {
                if (response.status === HttpStatus.CREATED) {
                    requestUpdate(update + 1); // Informs the Form that the information needs to be updated
                    setIsUploaded("Upload completed successfully.");
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

    function handleSelectStrategy(strategy) {
        setStrategy(strategy);
        setRequest(undefined);
    }

    function renderBreadCrumbs() {
        return (
            <Breadcrumb>
                <Breadcrumb.Item href="/">Home</Breadcrumb.Item>
                <Breadcrumb.Item href="/codebases">Codebases</Breadcrumb.Item>
                <Breadcrumb.Item href={`/codebases/${codebaseName}`}>{codebaseName}</Breadcrumb.Item>
                <Breadcrumb.Item active>Strategies</Breadcrumb.Item>
            </Breadcrumb>
        );
    }

    function renderStrategies() {
        return (
            <Form.Group as={Row} controlId="selectStrategy" className="align-items-center mb-3">
                <h4 className="mb-3 mt-3" style={{ color: "#666666" }}>
                    Similarity Generator and Clustering Algorithm
                </h4>
                <Col sm={2}>
                    <DropdownButton title={strategy === undefined? "Select Similarity Generator and Clustering Algorithm" : POSSIBLE_STRATEGIES[strategy]}>
                        {collectors.flatMap(collector => collector.possibleStrategies)
                            .filter((x, i, a) =>  a.indexOf(x) === i) // filter repeated values
                            .map(possibleStrategy =>
                                <Dropdown.Item
                                    key={possibleStrategy}
                                    onClick={() => handleSelectStrategy(possibleStrategy)}
                                >
                                    {POSSIBLE_STRATEGIES[possibleStrategy]}
                                </Dropdown.Item>
                            )
                        }
                    </DropdownButton>
                </Col>
            </Form.Group>
        );
    }

    return (
        <div>
            {renderBreadCrumbs()}

            <Form onSubmit={handleSubmit} className="mb-3">
                { renderStrategies() }

                {/*Add render of each strategy like the next line to request the required elements for its creation*/}
                {
                    strategy !== undefined && strategy === 'ACCESSES_SCIPY' &&
                    <AccessesSciPyStrategies
                        codebaseName={codebaseName}
                        profiles={profiles}
                        isUploaded={isUploaded}
                        update={update}
                        setRequest={setRequest}
                    />
                }
            </Form>
        </div>
    )
}