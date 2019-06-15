import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { Button, Form, InputGroup, FormControl, Breadcrumb, BreadcrumbItem, Dropdown, DropdownButton } from 'react-bootstrap';

var HttpStatus = require('http-status-codes');

const BreadCrumbs = () => {
    return (
      <div>
        <Breadcrumb>
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
            isUploaded: "",
            dendrogramName: "",
            linkageType: "",
            accessMetricWeight: "",
            readWriteMetricWeight: "",
            sequenceMetricWeight: "",
            dendrogramNames: [],
            codebases: [],
            codebase: {},
            selectedProfiles: []
        };

        this.handleUpload= this.handleUpload.bind(this);
        this.handleChangeDendrogramName = this.handleChangeDendrogramName.bind(this);
        this.handleLinkageType = this.handleLinkageType.bind(this);
        this.handleChangeAccessMetricWeight = this.handleChangeAccessMetricWeight.bind(this);
        this.handleChangeReadWriteMetricWeight = this.handleChangeReadWriteMetricWeight.bind(this);
        this.handleChangeSequenceMetricWeight = this.handleChangeSequenceMetricWeight.bind(this);
    }

    componentDidMount() {
        const service = new RepositoryService();
        service.getDendrogramNames().then(response => {
            this.setState({
                dendrogramNames: response.data
            });
        });

        service.getCodebases().then(response => {
            this.setState({
                codebases: response.data
            });
        });
    }

    handleUpload(event){
        event.preventDefault()
        const service = new RepositoryService();

        this.setState({
            isUploaded: "Uploading..."
        });
        
        service.createDendrogram(this.state.dendrogramName, this.state.linkageType, this.state.accessMetricWeight, this.state.readWriteMetricWeight, this.state.sequenceMetricWeight, this.state.codebase.name, this.state.selectedProfiles).then(response => {
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
                    isUploaded: "Upload failed. Dendrogram name already exists."
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

    handleChangeSequenceMetricWeight(event) {
        this.setState({
           sequenceMetricWeight: event.target.value
        });
    }

    setCodebase(codebase) {
        this.setState({
            codebase: codebase
        })
    }

    selectProfile(profile) {
        if (this.state.selectedProfiles.includes(profile)) {
            this.state.selectedProfiles.splice(this.state.selectedProfiles.indexOf(profile), 1);
        } else {
            this.state.selectedProfiles.push(profile);
        }
        this.setState({
            selectedProfiles: this.state.selectedProfiles
        });
    }

    render() {
        return (
            <Form onSubmit={this.handleUpload}>
                <BreadCrumbs />
                <h2 className="mb-4">Create Dendrogram</h2>
                
                <InputGroup className="mb-3">
                <InputGroup.Prepend>
                    <InputGroup.Text id="basic-addon1">Dendrogram Name</InputGroup.Text>
                </InputGroup.Prepend>
                    <FormControl 
                        type="text"
                        maxLength="18"
                        value={this.state.dendrogramName}
                        onChange={this.handleChangeDendrogramName}/>
                </InputGroup>

                <InputGroup className="mb-3">
                <InputGroup.Prepend>
                    <InputGroup.Text id="basic-addon-2">Select Codebase</InputGroup.Text>
                </InputGroup.Prepend>
                    <DropdownButton
                        title={Object.keys(this.state.codebase).length === 0 ? "Codebase" : this.state.codebase.name}
                        id="input-group-dropdown-1"
                        >
                        {this.state.codebases.map(codebase => <Dropdown.Item onClick={() => this.setCodebase(codebase)}>{codebase.name}</Dropdown.Item>)}
                    </DropdownButton>
                </InputGroup>

                <InputGroup className="mb-3">
                <InputGroup.Prepend>
                    <InputGroup.Text id="basic-addon--2">Select Profiles</InputGroup.Text>
                </InputGroup.Prepend>
                    <DropdownButton title={'Profiles'}>
                        {Object.keys(this.state.codebase).length === 0 ? [] : Object.keys(this.state.codebase.profiles).map(profile => <Dropdown.Item 
                            key={profile}
                            eventKey={profile}
                            onSelect={() => this.selectProfile(profile)}
                            active={this.state.selectedProfiles.includes(profile)}>{profile}</Dropdown.Item>)}
                    </DropdownButton>
                </InputGroup>

                <div key={`linkage-type`} className="mb-3">
                    <span className="mr-3">Linkage Type:</span>
                    <Form.Check inline onClick={this.handleLinkageType} name="formHorizontalRadios" label="Average" type="radio" id="average" />
                    <Form.Check inline onClick={this.handleLinkageType} name="formHorizontalRadios" label="Single" type="radio" id="single" />
                    <Form.Check inline onClick={this.handleLinkageType} name="formHorizontalRadios" label="Complete" type="radio" id="complete" />
                </div>

                <InputGroup className="mb-3">
                <InputGroup.Prepend>
                    <InputGroup.Text id="basic-addon2">Undistinct Access Metric Weight (%): </InputGroup.Text>
                </InputGroup.Prepend>
                    <FormControl 
                        type="number"
                        value={this.state.accessMetricWeight}
                        onChange={this.handleChangeAccessMetricWeight}/>
                </InputGroup>

                <InputGroup className="mb-3">
                <InputGroup.Prepend>
                    <InputGroup.Text id="basic-addon3">Read/Write Access Metric Weight (%): </InputGroup.Text>
                </InputGroup.Prepend>
                    <FormControl 
                        type="number"
                        value={this.state.readWriteMetricWeight}
                        onChange={this.handleChangeReadWriteMetricWeight}/>
                </InputGroup>

                <InputGroup className="mb-3">
                <InputGroup.Prepend>
                    <InputGroup.Text id="basic-addon4">Sequence Access Metric Weight (%): </InputGroup.Text>
                </InputGroup.Prepend>
                    <FormControl 
                        type="number"
                        value={this.state.sequenceMetricWeight}
                        onChange={this.handleChangeSequenceMetricWeight}/>
                </InputGroup>
                
                <Button variant="primary" 
                        type="submit" 
                        disabled={this.state.isUploaded === "Uploading..." || 
                                  this.state.dendrogramName === "" || 
                                  this.state.linkageType === "" || 
                                  this.state.accessMetricWeight === "" || 
                                  this.state.readWriteMetricWeight === "" || 
                                  this.state.sequenceMetricWeight === "" || 
                                  Number(this.state.accessMetricWeight) + Number(this.state.readWriteMetricWeight) + Number(this.state.sequenceMetricWeight) !== 100 || 
                                  Object.keys(this.state.codebase).length === 0 || 
                                  this.state.selectedProfiles.length === 0}>
                    Submit
                </Button>
                <br />
                {this.state.isUploaded}
            </Form>
        )
    }
}