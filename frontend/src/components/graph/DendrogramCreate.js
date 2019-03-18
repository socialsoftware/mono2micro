import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { Button, Form, InputGroup, FormControl, Breadcrumb, BreadcrumbItem } from 'react-bootstrap';

var HttpStatus = require('http-status-codes');

const BreadCrumbs = () => {
    return (
      <div>
        <Breadcrumb style={{ backgroundColor: '#a32a2a' }}>
          <Breadcrumb.Item href="/">Home</Breadcrumb.Item>
          <Breadcrumb.Item href="/dendrograms">Dendrograms</Breadcrumb.Item>
          <BreadcrumbItem active>Create</BreadcrumbItem>
        </Breadcrumb>
      </div>
    );
};

export class DendrogramCreate extends React.Component {
    constructor(props) {
        super(props);
        this.state = { 
            selectedFile: null, 
            isUploaded: "",
            dendrogramName: ""
        };

        this.handleSelectedFile = this.handleSelectedFile.bind(this);
        this.handleUpload= this.handleUpload.bind(this);
        this.handleChangeDendrogramName = this.handleChangeDendrogramName.bind(this);
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
        data.append('dendrogramName', this.state.dendrogramName);

        this.setState({
            isUploaded: "Uploading..."
        });
        
        service.createDendrogram(data).then(response => {
            if (response.status === HttpStatus.CREATED) {
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

    handleChangeDendrogramName(event) {
        this.setState({ 
            dendrogramName: event.target.value 
        });
    }

    render() {
        return (
            <Form onSubmit={this.handleUpload}>
                <BreadCrumbs />
                <h2>Create Dendrogram</h2>
                <h3 className="mb-4">Upload data file from Callgraph Eclipse Plugin to create the dendrogram.</h3>
                
                <InputGroup className="mb-3">
                <InputGroup.Prepend>
                    <InputGroup.Text id="basic-addon1">Dendrogram Name</InputGroup.Text>
                </InputGroup.Prepend>
                    <FormControl 
                        type="text"
                        value={this.state.dendrogramName}
                        onChange={this.handleChangeDendrogramName}/>
                </InputGroup>
                
                <Form.Group controlId="formDendrogramCreate">
                    <Form.Control type="file" onChange={this.handleSelectedFile} />
                </Form.Group>
                
                <Button variant="primary" type="submit" disabled={this.state.dendrogramName === "" || this.state.selectedFile === null}>
                    Submit
                </Button>
                <br />
                {this.state.isUploaded}
            </Form>
        )
    }
}