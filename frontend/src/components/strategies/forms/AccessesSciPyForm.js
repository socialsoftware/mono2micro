import React, {useEffect} from "react";
import Form from "react-bootstrap/Form";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import FormControl from "react-bootstrap/FormControl";
import Button from "@mui/material/Button";
import {CheckCircle} from "@mui/icons-material";

export const AccessesSciPyForm = ({strategy, setAddedSources, sources, setCanSubmit}) => {
    useEffect(() => {
        if (sources.reduce((p, c) => {return p? strategy.sourceTypes.includes(c.type) : false}, true))
            setCanSubmit(true);
    }, []);

    function addSource(source, event) {
        setAddedSources(prev => {
            prev[source] = event.target.files[0];
            if (Object.keys(prev).reduce((p, c) => {return p? strategy.sourceTypes.includes(c) : false}, true))
                setCanSubmit(true);
            return prev;
        });
    }

    const filledSourceTypes = sources.map(source => source.type);

    return (
        <React.Fragment>
            {strategy.sourceTypes.map(sourceType =>
                <React.Fragment key={sourceType}>
                    {!filledSourceTypes.includes(sourceType) &&
                        <Form.Group as={Row} className="mb-3 mt-2 align-items-center">
                            <Form.Label column sm={2}>
                                {sourceType + " File"}
                            </Form.Label>
                            <Col sm={5}>
                                <FormControl
                                    type="file"
                                    onChange={event => addSource(sourceType, event)}
                                />
                            </Col>
                        </Form.Group>
                    }
                    {filledSourceTypes.includes(sourceType) &&
                        <Form.Group as={Row} className="mb-3 mt-2 align-items-center">
                            <Form.Label column sm={2}>
                                {sourceType + " File"}
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
