import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { Button, DropdownButton, MenuItem, Form, FormControl, FormGroup} from 'react-bootstrap';

var HttpStatus = require('http-status-codes');

export class DendrogramCreate extends React.Component {
    constructor(props) {
        super(props);
        this.state = { selectedFile: null, isUploaded: "" };

        this.handleSelectedFile = this.handleSelectedFile.bind(this);
        this.handleUpload= this.handleUpload.bind(this);
    }

    handleSelectedFile(event) {
        this.setState({
            selectedFile: event.target.files[0],
            isUploaded: ""
        });
    }

    handleUpload(event){
        event.preventDefault()
        const service = new RepositoryService();
        var data = new FormData();
        data.append('file', this.state.selectedFile);

        this.setState({
            isUploaded: "Uploading..."
        });
        
        service.createDendrogram(data).then(response => {
            if (response.status === HttpStatus.CREATED) {
                this.props.location.headerFunction.handleGetGraphsFunction();
                this.setState({
                    isUploaded: "Upload completed successfully."
                });
            } else {
                this.setState({
                    isUploaded: "Upload failed."
                });
            }
        })
        .catch(error => {
            console.log(error);
            this.setState({
                isUploaded: "Upload failed."
            });
        });
    }

    render() {
        return (
            <Form onSubmit={this.handleUpload}>
                <h2>Create Dendrogram</h2>
                <h3>Upload data file from Callgraph Eclipse Plugin to create the dendrogram.</h3>
                <Form.Group controlId="formDendrogramCreate">
                    <Form.Control type="file" onChange={this.handleSelectedFile} />
                </Form.Group>
                <Button variant="primary" type="submit">
                    Submit
                </Button>
                <br />
                {this.state.isUploaded}
            </Form>
        )
    }
}