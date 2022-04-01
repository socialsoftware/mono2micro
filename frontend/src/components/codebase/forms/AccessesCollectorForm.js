import React from "react";
import Form from "react-bootstrap/Form";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import FormControl from "react-bootstrap/FormControl";

export const AccessesCollectorForm = ({collector, setCollector}) => {

    function addSource(source, event) {
        let updatedCollector = collector.copy();
        updatedCollector.addedSources = ({...collector.addedSources, [source]: event.target.files[0]});
        setCollector(updatedCollector);
    }

    return (
        <React.Fragment>
            { collector.sources.map(source =>
                <Form.Group key={source} as={Row} className="mb-3 mt-2 align-items-center">
                    <Form.Label column sm={2}>
                        {source + " File"}
                    </Form.Label>
                    <Col sm={5}>
                        <FormControl
                            type="file"
                            onChange={event => addSource(source, event)}
                        />
                    </Col>
                </Form.Group>
            )}
        </React.Fragment>
    );
}
