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
            dendrogramName: "",
            linkageType: "",
            accessMetricWeight: "",
            readWriteMetricWeight: "",
        };

        this.handleSelectedFile = this.handleSelectedFile.bind(this);
        this.handleUpload= this.handleUpload.bind(this);
        this.handleChangeDendrogramName = this.handleChangeDendrogramName.bind(this);
        this.handleLinkageType = this.handleLinkageType.bind(this);
        this.handleChangeAccessMetricWeight = this.handleChangeAccessMetricWeight.bind(this);
        this.handleChangeReadWriteMetricWeight = this.handleChangeReadWriteMetricWeight.bind(this);
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
        data.append('linkageType', this.state.linkageType);
        data.append('accessMetricWeight', this.state.accessMetricWeight);
        data.append('readWriteMetricWeight', this.state.readWriteMetricWeight);

        this.setState({
            isUploaded: "Uploading..."
        });
        
        service.createDendrogram(data).then(response => {
            if (response.status === HttpStatus.CREATED) {
                this.setState({
                    isUploaded: "Upload completed successfully."
                });
                this.props.history.push('/dendrograms');
            } else {
                this.setState({
                    isUploaded: "Upload failed."
                });
            }
        })
        .catch(error => {
            if (error.response !== undefined && error.response.status === HttpStatus.UNAUTHORIZED) {
                this.setState({
                    isUploaded: "Upload failed. Dendrogram Name already exists."
                });
            } else {
                this.setState({
                    isUploaded: "Upload failed."
                });
            }
        });
    }

    handleChangeDendrogramName(event) {
        this.setState({ 
            dendrogramName: event.target.value 
        });
    }

    handleLinkageType(event) {
        this.setState({
            linkageType: event.target.id
        });
    }

    handleChangeAccessMetricWeight(event) {
        this.setState({
            accessMetricWeight: event.target.value
        });
    }

    handleChangeReadWriteMetricWeight(event) {
        this.setState({
            readWriteMetricWeight: event.target.value
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

                <div key={`linkage-type`} className="mb-3">
                    <span className="mr-3">Linkage Type:</span>
                    <Form.Check inline onClick={this.handleLinkageType} name="formHorizontalRadios" label="Average" type="radio" id="average" />
                    <Form.Check inline onClick={this.handleLinkageType} name="formHorizontalRadios" label="Single" type="radio" id="single" />
                    <Form.Check inline onClick={this.handleLinkageType} name="formHorizontalRadios" label="Complete" type="radio" id="complete" />
                </div>

                <InputGroup className="mb-3">
                <InputGroup.Prepend>
                    <InputGroup.Text id="basic-addon2">Undistinct Access Metric Weight: </InputGroup.Text>
                </InputGroup.Prepend>
                    <FormControl 
                        type="number"
                        value={this.state.accessMetricWeight}
                        onChange={this.handleChangeAccessMetricWeight}/>
                </InputGroup>

                <InputGroup className="mb-3">
                <InputGroup.Prepend>
                    <InputGroup.Text id="basic-addon3">Read/Write Access Metric Weight: </InputGroup.Text>
                </InputGroup.Prepend>
                    <FormControl 
                        type="number"
                        value={this.state.readWriteMetricWeight}
                        onChange={this.handleChangeReadWriteMetricWeight}/>
                </InputGroup>
                
                <Button variant="primary" type="submit" disabled={this.state.isUploaded === "Uploading..." || this.state.dendrogramName === "" || this.state.linkageType === "" || parseFloat(this.state.accessMetricWeight) + parseFloat(this.state.readWriteMetricWeight) !== 1 || this.state.selectedFile === null}>
                    Submit
                </Button>
                <br />
                {this.state.isUploaded}
            </Form>
        )
    }
}