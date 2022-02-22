import React, {useEffect, useState} from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { URL } from '../../constants/constants';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Form from 'react-bootstrap/Form';
import FormControl from 'react-bootstrap/FormControl';
import Card from 'react-bootstrap/Card';
import Button from 'react-bootstrap/Button';
import Breadcrumb from 'react-bootstrap/Breadcrumb';
import BootstrapTable from 'react-bootstrap-table-next';
import {useParams} from "react-router-dom";

const HttpStatus = require('http-status-codes');

const metricColumns = [
    {
        dataField: 'decomposition',
        text: 'Decomposition',
        sort: true
    },
    {
        dataField: 'clusters',
        text: 'Number of Retrieved Clusters',
        sort: true
    },
    {
        dataField: 'singleton',
        text: 'Number of Singleton Clusters',
        sort: true
    },
    {
        dataField: 'max_cluster_size',
        text: 'Maximum Cluster Size',
        sort: true
    },
    {
        dataField: 'ss',
        text: 'Silhouette Score',
        sort: true
    },
    {
        dataField: 'cohesion',
        text: 'Cohesion',
        sort: true
    },
    {
        dataField: 'coupling',
        text: 'Coupling',
        sort: true
    },
    {
        dataField: 'complexity',
        text: 'Complexity',
        sort: true
    },
    {
        dataField: 'performance',
        text: 'Performance',
        sort: true
    }
];

export const Dendrogram = () => {
    let { codebaseName, dendrogramName } = useParams();

    const [height, setHeight] = useState("");
    const [numberClusters, setNumberClusters] = useState("");
    const [newExpert, setNewExpert] = useState("");
    const [isUploaded, setIsUploaded] = useState("");
    const [cutSuccess, setCutSuccess] = useState("");
    const [decompositions, setDecompositions] = useState([]);
    const [expertFile, setExpertFile] = useState(null);

    //Executed on mount
    useEffect(() => loadDecompositions(), []);

    function loadDecompositions() {
        const service = new RepositoryService();
        service.getDecompositions(
            codebaseName,
            dendrogramName,
            [
                "name",
                "clusters",
                "singleton",
                "silhouetteScore",
                "complexity",
                "cohesion",
                "coupling",
                "performance"
            ]
        ).then(response => {
            setDecompositions(response.data);
        });
    }

    function handleHeightChange(event) {
        setHeight(event.target.value);
    }

    function handleNumberClustersChange(event) {
        setNumberClusters(event.target.value);
    }

    function handleChangeNewExpert(event) {
        setNewExpert(event.target.value);
    }
    
    function handleCutSubmit(event) {
        event.preventDefault();
        
        setCutSuccess("Processing...");

        let cutType;
        let cutValue;
        if (height !== "") {
            cutType = "H";
            cutValue = Number(height);
        } else {
            cutType = "N";
            cutValue = Number(numberClusters);
        }

        const service = new RepositoryService();
        
        service.cutDendrogram(
            codebaseName,
            dendrogramName,
            cutValue,
            cutType
        ).then(response => {
            if (response.status === HttpStatus.OK) {
                loadDecompositions();
                setCutSuccess("Dendrogram cut successful.");
            } else {
                setCutSuccess("Failed to cut dendrogram.");
            }
        })
        .catch(error => {
            setCutSuccess("Failed to cut dendrogram.");
        });
    }

    function handleExpertSubmit(event){
        event.preventDefault();

        setIsUploaded("Uploading...");

        const service = new RepositoryService();
        service.expertCut(
            codebaseName,
            dendrogramName,
            newExpert,
            expertFile
        ).then(response => {
            if (response.status === HttpStatus.OK) {
                loadDecompositions();
                setIsUploaded("Upload completed successfully.");
            } else {
                setIsUploaded("Upload failed.");
            }
        })
        .catch(error => {
            if (error.response !== undefined && error.response.status === HttpStatus.UNAUTHORIZED) {
                setIsUploaded("Upload failed. Expert name already exists.");
            } else {
                setIsUploaded("Upload failed.");
            }
        });
    }

    function handleDeleteDecomposition(decompositionName) {
        const service = new RepositoryService();
        service.deleteDecomposition(codebaseName, dendrogramName, decompositionName).then(response => {
            loadDecompositions();
        });
    }

    function handleSelectNewExpertFile(event) {
        setExpertFile(event.target.files[0]);
    }

    function renderBreadCrumbs() {
        return (
            <Breadcrumb>
                <Breadcrumb.Item href="/">
                    Home
                </Breadcrumb.Item>
                <Breadcrumb.Item href="/codebases">
                    Codebases
                </Breadcrumb.Item>
                <Breadcrumb.Item href={`/codebases/${codebaseName}`}>
                    {codebaseName}
                </Breadcrumb.Item>
                <Breadcrumb.Item href={`/codebases/${codebaseName}/dendrograms`}>
                    Dendrograms
                </Breadcrumb.Item>
                <Breadcrumb.Item active>
                    {dendrogramName}
                </Breadcrumb.Item>
            </Breadcrumb>
        );
    }

    function renderCutForm() {
        return (
            <Form onSubmit={handleCutSubmit}>
                <Form.Group as={Row} controlId="height" className="mb-3">
                    <Form.Label column sm={2}>
                        Height
                    </Form.Label>
                    <Col sm={5}>
                        <FormControl 
                            type="number"
                            placeholder="Cut Height"
                            value={height}
                            onChange={handleHeightChange}/>
                    </Col>
                </Form.Group>

                <Form.Group as={Row} controlId="or" className="mb-3">
                    <Form.Label column sm={2}>
                        OR
                    </Form.Label>
                </Form.Group>

                <Form.Group as={Row} controlId="numberOfClusters" className="mb-3">
                    <Form.Label column sm={2}>
                        Number of Clusters
                    </Form.Label>
                    <Col sm={5}>
                        <FormControl 
                            type="number"
                            placeholder="Number of Clusters in Cut"
                            value={numberClusters}
                            onChange={handleNumberClustersChange}/>
                    </Col>
                </Form.Group>

                <Form.Group as={Row} className="mb-3">
                    <Col sm={{ span: 5, offset: 2 }}>
                        <Button type="submit"
                                disabled={(height !== "" && numberClusters !== "") ||
                                            (height === "" && numberClusters === "") ||
                                            cutSuccess === "Processing..."}>
                            Cut
                        </Button>
                        <Form.Text className="ms-2">
                            {cutSuccess}
                        </Form.Text>
                    </Col>
                </Form.Group>
            </Form>
        );
    }

    function renderExpertForm() {
        return (
            <Form onSubmit={handleExpertSubmit}>
                <Form.Group as={Row} controlId="newExpertName" className="mb-3">
                    <Form.Label column sm={2}>
                        Expert Name
                    </Form.Label>
                    <Col sm={5}>
                        <FormControl 
                            type="text"
                            maxLength="30"
                            placeholder="Expert Name"
                            value={newExpert}
                            onChange={handleChangeNewExpert}/>
                    </Col>
                </Form.Group>

                <Form.Group as={Row} controlId="newExpertFile" className="mb-3">
                    <Form.Label column sm={2}>
                        Expert File (Optional)
                    </Form.Label>
                    <Col sm={5}>
                        <FormControl 
                            type="file"
                            onChange={handleSelectNewExpertFile}
                        />
                    </Col>
                </Form.Group>

                <Form.Group as={Row} className="mb-4">
                    <Col sm={{ span: 5, offset: 2 }}>
                        <Button 
                            type="submit"
                            disabled={isUploaded === "Uploading..." || newExpert === ""}
                        >
                            Create Expert
                        </Button>
                        <Form.Text className="ms-2">
                            {isUploaded}
                        </Form.Text>
                    </Col>
                </Form.Group>
            </Form>
        );
    }

    function renderCuts() {
        return (
            <Row>
                {
                    decompositions.map(decomposition =>
                        <Col key={decomposition.name} md="auto">
                            <Card style={{ width: '15rem', marginBottom: "16px" }}>
                                <Card.Body>
                                    <Card.Title>
                                        {decomposition.name}
                                    </Card.Title>
                                    <Button
                                        href={`/codebases/${codebaseName}/dendrograms/${dendrogramName}/decompositions/${decomposition.name}`}
                                        className="mb-2"
                                    >
                                        Go to Decomposition
                                    </Button>
                                    <br/>
                                    <Button
                                        onClick={() => handleDeleteDecomposition(decomposition.name)}
                                        variant="danger"
                                    >
                                        Delete
                                    </Button>
                                </Card.Body>
                            </Card>
                        </Col>
                    )
                }
            </Row>
        );
    }

    const metricRows = decompositions.map(decomposition => {

        let amountOfSingletonClusters = 0;
        let maxClusterSize = 0;

        Object.values(decomposition.clusters).forEach(c => {
            const numberOfEntities = c.entities.length;

            if (numberOfEntities === 1) amountOfSingletonClusters++;

            if (numberOfEntities > maxClusterSize) maxClusterSize = numberOfEntities;
        })

        return {
            decomposition: decomposition.name,
            clusters: Object.keys(decomposition.clusters).length,
            singleton: amountOfSingletonClusters,
            max_cluster_size: maxClusterSize,
            ss: decomposition.silhouetteScore,
            cohesion: decomposition.cohesion,
            coupling: decomposition.coupling,
            complexity: decomposition.complexity,
            performance: decomposition.performance
        }
    });

    return (
        <div>
            {renderBreadCrumbs()}

            <h4 style={{color: "#666666"}}>
                Cut Dendrogram
            </h4>

            {renderCutForm()}

            <h4 style={{color: "#666666"}}>
                Create Expert Cut
            </h4>

            {renderExpertForm()}

            <img
                width="100%"
                src={URL + "codebase/" + codebaseName + "/dendrogram/" + dendrogramName + "/image?" + new Date().getTime()}
                alt="Dendrogram"
            />

            <h4 style={{color: "#666666", marginTop: "16px" }}>
                Cuts
            </h4>

            {renderCuts()}

            <h4 style={{color: "#666666"}}>
                Metrics
            </h4>

            {decompositions.length > 0 &&
                <div>
                    <BootstrapTable bootstrap4 keyField='decomposition' data={ metricRows } columns={ metricColumns } />
                </div>
            }
        </div>
    );
}