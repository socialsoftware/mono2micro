import {URL} from "../../../constants/constants";
import React, {useState} from "react";
import Form from "react-bootstrap/Form";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import FormControl from "react-bootstrap/FormControl";
import Button from "react-bootstrap/Button";
import {useParams} from "react-router-dom";
import {RepositoryService} from "../../../services/RepositoryService";
import HttpStatus from "http-status-codes";


export const SciPyDecompositionForm = ({type, loadDecompositions}) => {
    let { similarityName } = useParams();

    const [createExpert, setCreateExpert] = useState(undefined);

    const [height, setHeight] = useState("");
    const [numberClusters, setNumberClusters] = useState("");
    const [expertName, setExpertName] = useState("");
    const [isUploaded, setIsUploaded] = useState("");
    const [expertFile, setExpertFile] = useState(null);

    function handleHeightChange(event) {
        setHeight(event.target.value);
    }

    function handleNumberClustersChange(event) {
        setNumberClusters(event.target.value);
    }

    function handleChangeNewExpert(event) {
        setExpertName(event.target.value);
    }

    function handleCreationSubmit(event) {
        event.preventDefault();

        setIsUploaded("Processing...");

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

        service.createDecomposition({
            type,
            decompositionType: type,
            similarityName,
            cutType,
            cutValue
        }).then(response => {
            if (response.status === HttpStatus.OK) {
                loadDecompositions();
                setIsUploaded("");
                setCreateExpert(undefined);
            } else {
                setIsUploaded("Failed to create decomposition.");
            }
        })
        .catch(() => {
            setIsUploaded("Failed to create decomposition.");
        });
    }

    function handleExpertSubmit(event){
        event.preventDefault();

        setIsUploaded("Uploading...");

        const service = new RepositoryService();
        service.createExpertDecomposition(
            similarityName,
            expertName,
            expertFile
        ).then(response => {
            if (response.status === HttpStatus.OK) {
                loadDecompositions();
                setIsUploaded("");
                setCreateExpert(undefined);
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

    function handleSelectNewExpertFile(event) {
        setExpertFile(event.target.files[0]);
    }

    function renderCutForm() {
        return (
            <Form className={"mt-4"} onSubmit={handleCreationSubmit}>

                <img
                    className={"d-block mb-4"}
                    width="70%"
                    src={URL + "/similarity/" + similarityName + "/image?" + new Date().getTime()}
                    alt="Strategy"
                />

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
                                    isUploaded === "Processing..."}>
                            Generate
                        </Button>
                        <Form.Text className="ms-2">
                            {isUploaded}
                        </Form.Text>
                    </Col>
                </Form.Group>
            </Form>
        );
    }

    function renderExpertForm() {
        return (
            <Form className={"mt-4"} onSubmit={handleExpertSubmit}>
                <Form.Group as={Row} controlId="newExpertName" className="mb-3">
                    <Form.Label column sm={2}>
                        Expert Name
                    </Form.Label>
                    <Col sm={5}>
                        <FormControl
                            type="text"
                            maxLength="30"
                            placeholder="Expert Name"
                            value={expertName}
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
                            disabled={isUploaded === "Uploading..." || expertName === ""}
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

    return (
        <div>
            <Button className={"me-3"} onClick={() => {setCreateExpert(false); setIsUploaded("")}}>
                Generate Decomposition
            </Button>

            <Button onClick={() => {setCreateExpert(true); setIsUploaded("")}}>
                Add Expert Decomposition
            </Button>

            {createExpert === false && renderCutForm() }

            {createExpert === true && renderExpertForm() }
        </div>
    );
}