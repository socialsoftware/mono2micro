import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { Form, ButtonGroup, Button, InputGroup, FormControl, Breadcrumb, BreadcrumbItem, Dropdown, DropdownButton, ButtonToolbar } from 'react-bootstrap';


export class Codebase extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            codebaseName: this.props.match.params.codebaseName,
            codebase: {},
            newProfileName: "",
            moveToProfile: "",
            selectedControllers: []
        };

        this.handleChangeNewProfileName = this.handleChangeNewProfileName.bind(this);
        this.handleNewProfileSubmit = this.handleNewProfileSubmit.bind(this);
        this.handleMoveControllersSubmit = this.handleMoveControllersSubmit.bind(this);
        this.handleSelectProfile = this.handleSelectProfile.bind(this);
    }

    componentDidMount() {
        this.loadCodebase();
    }

    loadCodebase() {
        const service = new RepositoryService();
        service.getCodebase(this.state.codebaseName).then(response => {
            this.setState({
                codebase: response.data
            });
        });
    }

    handleChangeNewProfileName(event) {
        this.setState({
            newProfileName: event.target.value
        });
    }

    handleNewProfileSubmit() {
        const service = new RepositoryService();
        service.addProfile(this.state.codebaseName, this.state.newProfileName).then(response => {
            this.loadCodebase();
        });
    }

    handleMoveToProfile(profile) {
        this.setState({
            moveToProfile: profile
        });
    }

    handleMoveControllersSubmit() {
        const service = new RepositoryService();
        service.moveControllers(this.state.codebaseName, this.state.selectedControllers, this.state.moveToProfile).then(response => {
            this.setState({
                selectedControllers: []
            })
            this.loadCodebase();
        });
    }

    handleDeleteProfile(profile) {
        const service = new RepositoryService();
        service.deleteProfile(this.state.codebaseName, profile).then(response => {
            this.loadCodebase();
        });
    }

    handleSelectProfile(event) {
        if (this.state.selectedControllers.includes(event.target.id)) {
            let filteredArray = this.state.selectedControllers.filter(c => c !== event.target.id);
            this.setState({
                selectedControllers: filteredArray
            });
        } else {
            this.setState({
                selectedControllers: [...this.state.selectedControllers, event.target.id]
            });
        }
    }

    render() {
        const BreadCrumbs = () => {
            return (
              <div>
                <Breadcrumb>
                  <Breadcrumb.Item href="/">Home</Breadcrumb.Item>
                  <Breadcrumb.Item href="/codebases">Codebases</Breadcrumb.Item>
                  <BreadcrumbItem active>{this.state.codebaseName}</BreadcrumbItem>
                </Breadcrumb>
              </div>
            );
        };

        return (
            <div>
                <BreadCrumbs />

                <ButtonToolbar>
                <InputGroup className="mr-1">
                    <InputGroup.Prepend>
                        <InputGroup.Text id="newprofile">New Profile</InputGroup.Text>
                    </InputGroup.Prepend>
                    <FormControl
                        type="text"
                        value={this.state.newProfileName}
                        onChange={this.handleChangeNewProfileName}/>
                </InputGroup>

                <Button className="mr-1" onClick={this.handleNewProfileSubmit}>Submit</Button>
                </ButtonToolbar>

                {Object.keys(this.state.codebase).length !== 0 &&
                    <span>
                    <div>
                    <Button className="mr-1 mt-2">Move selected controllers to</Button>

                    <DropdownButton as={ButtonGroup} title={this.state.moveToProfile === "" ? "Profile" : this.state.moveToProfile} className="mr-1 mt-2">
                        {Object.keys(this.state.codebase.profiles).map(p => <Dropdown.Item 
                            key={p}
                            eventKey={p}
                            onSelect={() => this.handleMoveToProfile(p)}>{p}</Dropdown.Item>)}
                    </DropdownButton>

                    <Button disabled={this.state.selectedControllers.length === 0 || this.state.moveToProfile === ""} className="mt-2" onClick={this.handleMoveControllersSubmit}>Submit</Button>
                    </div>

                    {Object.keys(this.state.codebase.profiles).map(profile =>
                        <span>
                        <span style={{fontSize: '30px', fontWeight: 'bold'}}>{profile}

                        {this.state.codebase.profiles[profile].length === 0 && <Button onClick={() => this.handleDeleteProfile(profile)} className="mr-4" variant="danger" size="sm">-</Button>}
                        </span>
                        
                            {this.state.codebase.profiles[profile].map(controller =>
                                <Form.Check checked={this.state.selectedControllers.includes(controller)} style={{ paddingLeft: "3em" }} onClick={this.handleSelectProfile} label={controller} type="checkbox" id={controller} />
                            )}
                         
                        </span>
                    )}
                    </span>
                }
            </div>
        );
    }
}