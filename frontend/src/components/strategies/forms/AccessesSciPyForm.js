import React, {useEffect} from "react";
import Form from "react-bootstrap/Form";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import FormControl from "react-bootstrap/FormControl";
import Button from "@mui/material/Button";
import {CheckCircle} from "@mui/icons-material";

export const AccessesSciPyForm = ({strategy, setAddedRepresentations, representations, setCanSubmit}) => {
    useEffect(() => {
        if (representations.reduce((p, c) => {return p? strategy.representationTypes.includes(c.type) : false}, true))
            setCanSubmit(true);
    }, []);

    function addRepresentation(representation, event) {
        setAddedRepresentations(prev => {
            prev[representation] = event.target.files[0];
            if (Object.keys(prev).reduce((p, c) => {return p? strategy.representationTypes.includes(c) : false}, true))
                setCanSubmit(true);
            return prev;
        });
    }

    const filledRepresentationTypes = representations.map(representation => representation.type);

    return (
        <React.Fragment>
            {strategy.representationTypes.map(representationType =>
                <React.Fragment key={representationType}>
                    {!filledRepresentationTypes.includes(representationType) &&
                        <Form.Group as={Row} className="mb-3 mt-2 align-items-center">
                            <Form.Label column sm={2}>
                                {representationType + " File"}
                            </Form.Label>
                            <Col sm={5}>
                                <FormControl
                                    type="file"
                                    onChange={event => addRepresentation(representationType, event)}
                                />
                            </Col>
                        </Form.Group>
                    }
                    {filledRepresentationTypes.includes(representationType) &&
                        <Form.Group as={Row} className="mb-3 mt-2 align-items-center">
                            <Form.Label column sm={2}>
                                {representationType + " File"}
                            </Form.Label>
                            <Col sm={5}>
                                <Button variant="contained" disableRipple={true} disableElevation={true} color="success" sx={{ bgcolor: "success.dark" }} endIcon={<CheckCircle/>}>
                                    Already Added
                                </Button>
                            </Col>
                        </Form.Group>
                    }
                </React.Fragment>
            )}
        </React.Fragment>
    );
}
