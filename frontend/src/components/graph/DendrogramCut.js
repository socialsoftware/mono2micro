import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { DENDROGRAM_URL } from '../../constants/constants';
import { Button, DropdownButton, MenuItem, Form, FormControl, FormGroup} from 'react-bootstrap';

var HttpStatus = require('http-status-codes');


export class DendrogramCut extends React.Component {
    constructor(props) {
        super(props);
        this.state = { cutValue: "", cutSuccess: "" };

        this.handleCutValueChange = this.handleCutValueChange.bind(this);
        this.handleCutSubmit = this.handleCutSubmit.bind(this);
    }

    handleCutValueChange(event) {
        this.setState({cutValue: event.target.value});
    }
    
    handleCutSubmit(event) {
        event.preventDefault();
        const service = new RepositoryService();
        service.cutDendrogram(this.state.cutValue).then(response => {
            if (response.status === HttpStatus.OK) {
                this.setState({
                    cutSuccess: "Dendrogram cut successful."
                });
                this.props.location.headerFunction.handleGetGraphsFunction();
            } else {
                this.setState({
                    cutSuccess: "Failed to cut dendrogram."
                });
            }
        })
        .catch(error => {
            console.log(error);
            this.setState({
                cutSuccess: "Failed to cut dendrogram."
            });
        });
    }

    render() {
        return (
            <div>
                <h2>Cut Dendrogram</h2>
                <Form onSubmit={this.handleCutSubmit}>
                    <Form.Group controlId="formDendrogramCut">
                        <Form.Control type="number" value={this.state.cutValue} onChange={this.handleCutValueChange} />
                    </Form.Group>
                    <Button variant="primary" type="submit">
                        Submit
                    </Button>
                </Form>
                <br />
                {this.state.cutSuccess}
                <br />
                <br />
                <img src={DENDROGRAM_URL + "?" + new Date().getTime()} alt="Dendrogram" />
            </div>
        );
    };
}