import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { ButtonGroup, Card, Button, Form, InputGroup, FormControl, Breadcrumb, BreadcrumbItem, FormGroup, Dropdown, DropdownButton } from 'react-bootstrap';
import { Dendrograms } from './Dendrograms';

var HttpStatus = require('http-status-codes');

const BreadCrumbs = () => {
    return (
      <div>
        <Breadcrumb>
          <Breadcrumb.Item href="/">Home</Breadcrumb.Item>
          <BreadcrumbItem active>Profiles</BreadcrumbItem>
        </Breadcrumb>
      </div>
    );
};

export class Profiles extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            profileGroupName: "",
            selectedFile: null, 
            isUploaded: "",
            profiles: [],
            profile: ""
        };

        this.handleSelectedFile = this.handleSelectedFile.bind(this);
        this.handleUpload= this.handleUpload.bind(this);
        this.handleChangeProfileGroupName = this.handleChangeProfileGroupName.bind(this);
        this.handleDeleteProfileGroup = this.handleDeleteProfileGroup.bind(this);
    }

    componentDidMount() {
        this.loadProfiles();
    }

    loadProfiles() {
        const service = new RepositoryService();
        service.getProfileGroupNames().then(response => {
            this.setState({
                profiles: response.data,
                profile: response.data === [] ? "" : response.data[0]
            });
        });
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
        data.append('profileGroupName', this.state.profileGroupName);

        this.setState({
            isUploaded: "Uploading..."
        });
        
        service.createProfileGroup(data).then(response => {
            if (response.status === HttpStatus.CREATED) {
                this.loadProfiles();
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
            if (error.response !== undefined && error.response.status === HttpStatus.UNAUTHORIZED) {
                this.setState({
                    isUploaded: "Upload failed. Profile Group Name already exists."
                });
            } else {
                this.setState({
                    isUploaded: "Upload failed."
                });
            }
        });
    }

    handleChangeProfileGroupName(event) {
        this.setState({ 
            profileGroupName: event.target.value 
        });
    }

    handleDeleteProfileGroup(profile) {
        const service = new RepositoryService();
        service.deleteProfileGroup(profile).then(response => {
            this.loadProfiles();
        });
    }

    changeProfile(value) {
        this.setState({
            profile: value
        });
    }

    render() {
        const profiles = this.state.profiles.map(profile =>
            <Button active={this.state.profile === profile} onClick={() => this.changeProfile(profile)}>{profile}</Button>
        );

        return (
            <div>
                <BreadCrumbs />
                <h2 className="mb-4">Profiles Manager</h2>
                <Form onSubmit={this.handleUpload}>
                    <InputGroup className="mb-3">
                    <InputGroup.Prepend>
                        <InputGroup.Text id="profile-group">Profile Group Name</InputGroup.Text>
                    </InputGroup.Prepend>
                        <FormControl 
                            type="text"
                            maxLength="18"
                            value={this.state.profileGroupName}
                            onChange={this.handleChangeProfileGroupName}/>
                    </InputGroup>

                    <InputGroup className="mb-3">
                    <InputGroup.Prepend>
                        <InputGroup.Text id="datafile">Data Collection File</InputGroup.Text>
                    </InputGroup.Prepend>
                        <FormGroup>
                            <FormControl 
                                type="file"
                                onChange={this.handleSelectedFile}/>
                        </FormGroup>
                    </InputGroup>
                    
                    <Button variant="primary" type="submit" disabled={this.state.isUploaded === "Uploading..." || this.state.profileGroupName === "" || this.state.selectedFile === null}>
                        Submit
                    </Button>
                    <br />
                    {this.state.isUploaded}
                    <br /><br />
                </Form>

                {profiles.length !== 0 &&
                    <div>
                    <div style={{overflow: "auto"}} className="mb-3">
                    <ButtonGroup>
                        {profiles}
                    </ButtonGroup>
                    </div>

                    <Card className="mb-5" key={this.state.profile} style={{ width: '15rem' }}>
                        <Card.Body>
                            <Card.Title>{this.state.profile}</Card.Title>
                            <Button href={`/profile/${this.state.profile}`} className="mr-4" variant="primary">See Profile</Button>
                            <Button onClick={() => this.handleDeleteProfileGroup(this.state.profile)} variant="danger">Delete</Button>
                        </Card.Body>
                    </Card>
                    </div>
                }
            </div>
        )
    }
}