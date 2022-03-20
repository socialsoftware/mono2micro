import {useEffect, useState} from 'react';
import Breadcrumb from 'react-bootstrap/Breadcrumb';
import Button from 'react-bootstrap/Button';
import {useParams} from "react-router-dom";
import Form from "react-bootstrap/Form";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import DropdownButton from "react-bootstrap/DropdownButton";
import {
    CLUSTERING_ALGORITHMS,
    POSSIBLE_DECOMPOSITIONS,
    SIMILARITY_GENERATORS,
    SOURCES
} from "../../constants/constants";
import Dropdown from "react-bootstrap/Dropdown";
import OverlayTrigger from "react-bootstrap/OverlayTrigger";
import Popover from "react-bootstrap/Popover";
import {RepositoryService} from "../../services/RepositoryService";
import HttpStatus from "http-status-codes";
import ButtonToolbar from "react-bootstrap/ButtonToolbar";
import {AccessesSciPyStrategies} from "../strategy/AccessesSciPyStrategies";

function renderBreadCrumbs(codebaseName) {
    return (
        <Breadcrumb>
            <Breadcrumb.Item href="/">
                Home
            </Breadcrumb.Item>
            <Breadcrumb.Item href="/codebases">
                Codebases
            </Breadcrumb.Item>
            <Breadcrumb.Item active>
                {codebaseName}
            </Breadcrumb.Item>
        </Breadcrumb>
    );
}

export function Codebase() {
    let { codebaseName } = useParams();
    const [similarityGenerator, setSimilarityGenerator] = useState(undefined);
    const [clusteringAlgorithm, setClusteringAlgorithm] = useState(undefined);
    const [selectedSource, setSelectedSource] = useState(undefined);
    const [selectedFile, setSelectedFile] = useState(null);
    const [sources, setSources] = useState([]);
    const [strategies, setStrategies] = useState([]);
    const [isUploaded, setIsUploaded] = useState("");

    //Executed on mount
    useEffect(() => {
        loadStrategies();
        loadSources();
    }, []);

    function loadSources() {
        const service = new RepositoryService();
        service.getSources(codebaseName)
            .then(response => {
                setSources(response);
            });
    }

    function loadStrategies() {
        const service = new RepositoryService();
        service.getStrategies(codebaseName)
            .then(response => {
                setStrategies(response.data);
                console.log(response.data);
            });
    }

    function handleSourceSubmit(event) {
        event.preventDefault()

        setIsUploaded("Uploading...");

        if (selectedFile !== null) {
            const service = new RepositoryService();
            service.addSource(codebaseName, selectedSource.value, selectedFile)
                .then(response => {
                    if (response.status === HttpStatus.CREATED) {
                        loadSources();
                        setIsUploaded("Upload completed successfully.");
                    } else {
                        setIsUploaded("Upload failed.");
                    }
                })
                .catch(error => {
                    if (error.response !== undefined && error.response.status === HttpStatus.UNAUTHORIZED) {
                        setIsUploaded("Upload failed. Source already submitted.");
                    }
                    else if (error.response !== undefined && error.response.status === HttpStatus.NOT_FOUND) {
                        setIsUploaded("Upload failed. Invalid datafile path.");
                    }
                    else {
                        setIsUploaded("Upload failed.");
                    }
                });
            setSelectedFile(null);
        }
    }

    const getHelpText = (
        <Popover id="helpPopover">
            <div> Select the Similarity Generator and<br/>the Clustering Algorithm to create<br/>a new Strategy</div>
        </Popover>
    );

    function handleSimilarityGenerator(generator) {
        setSimilarityGenerator(generator);
        setClusteringAlgorithm(undefined);
    }

    function handleClusteringAlgorithm(algorithm) {
        setClusteringAlgorithm(algorithm);
    }

    function handleSelectedSource(source) {
        setSelectedSource(source);
    }

    function handleSelectedFile(event) {
        setSelectedFile(event.target.files[0]);
        setIsUploaded("");
    }

    function renderSources() {
        return (
            <Form onSubmit={handleSourceSubmit}>
                <Form.Group as={Row} controlId="selectSource" className="align-items-center mb-3">
                    <h4 className="mb-3 mt-3" style={{ color: "#666666" }}> Source Files </h4>
                    <Col sm={2}>
                        <DropdownButton title={selectedSource === undefined? "Select Source" : selectedSource.name}>
                            {Object.values(SOURCES)
                                .map(source =>
                                    <Dropdown.Item
                                        key={source.value}
                                        onClick={() => handleSelectedSource(source)}
                                    >
                                        {source.name}
                                    </Dropdown.Item>
                                )
                            }
                        </DropdownButton>
                    </Col>
                    {selectedSource !== undefined &&
                        <Col sm={4}>
                            <Form.Control
                                type="file"
                                onChange={handleSelectedFile}/>
                        </Col>
                    }
                    {selectedFile !== null &&
                        <Col sm={1}><Button type="submit" className="">Submit</Button></Col>
                    }
                    {isUploaded}
                </Form.Group>

                <div className={"d-flex flex-wrap"} style={{gap: '1rem 1rem'}}>
                    {sources.map(source => source.printCard())}
                </div>
            </Form>
        );
    }

    function renderSimilarityGenerator() {
        return (
            <ButtonToolbar  style={{gap: '.5rem .5rem'}}>
                <DropdownButton title={similarityGenerator === undefined? "Select Similarity Generator" : similarityGenerator.name}>
                    {Object.values(SIMILARITY_GENERATORS)
                        .map(generator =>
                            <Dropdown.Item
                                key={generator.value}
                                onClick={() => handleSimilarityGenerator(generator)}
                            >
                                {generator.name}
                            </Dropdown.Item>
                        )
                    }
                </DropdownButton>
                {similarityGenerator !== undefined &&
                    <DropdownButton title={clusteringAlgorithm === undefined? "Select Clustering Algorithm" : clusteringAlgorithm.name}>
                        {POSSIBLE_DECOMPOSITIONS[similarityGenerator.value]
                            .map(algorithmValue => CLUSTERING_ALGORITHMS[algorithmValue])
                            .map(algorithm =>
                                <Dropdown.Item
                                    key={algorithm.value}
                                    onClick={() => handleClusteringAlgorithm(algorithm)}
                                >
                                    {algorithm.name}
                                </Dropdown.Item>
                            )}
                    </DropdownButton>
                }
            </ButtonToolbar>
        );
    }

    return (
        <div>
            {renderBreadCrumbs(codebaseName)}
            <Row className="mt-4">
                <Col>
                    <h3 style={{color: "#666666"}}>Codebase</h3>
                </Col>
                <Col className="me-5">
                    <OverlayTrigger trigger="click" placement="left" overlay={getHelpText}>
                        <Button className="float-end" variant="success">Help</Button>
                    </OverlayTrigger>
                </Col>
            </Row>

            { renderSources() }

            <h4 className="mb-3 mt-3" style={{ color: "#666666" }}> Strategy </h4>

            { renderSimilarityGenerator() }

            {/*Add render of each similarity generator like the next line to request the required elements for the dendrogram's creation*/}
            {
                clusteringAlgorithm !== undefined && clusteringAlgorithm.hasDendrograms &&
                similarityGenerator.value === "ACCESSES_LOG" && clusteringAlgorithm.value === "SCIPY" &&
                <AccessesSciPyStrategies
                    codebaseName={codebaseName}
                    profiles={profiles}
                    isUploaded={isUploaded}
                    update={update}
                    setRequest={setRequest}
                />
            }
        </div>
    );
}