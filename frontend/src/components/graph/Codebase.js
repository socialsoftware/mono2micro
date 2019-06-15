import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { ButtonGroup, Button, InputGroup, FormControl, Breadcrumb, BreadcrumbItem, Dropdown, DropdownButton, ButtonToolbar } from 'react-bootstrap';


export class Codebase extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            codebaseName: this.props.match.params.codebaseName,
            codebase: {},
            newProfileName: "",
            moveController: "",
            moveToProfile: "",
        };

        this.handleChangeNewProfileName = this.handleChangeNewProfileName.bind(this);
        this.handleNewProfileSubmit = this.handleNewProfileSubmit.bind(this);
        this.handleMoveControllerSubmit = this.handleMoveControllerSubmit.bind(this);
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

    handleMoveController(controller) {
        this.setState({
            moveController: controller
        });
    }

    handleMoveToProfile(profile) {
        this.setState({
            moveToProfile: profile
        });
    }

    handleMoveControllerSubmit() {
        const service = new RepositoryService();
        service.moveController(this.state.codebaseName, this.state.moveController, this.state.moveToProfile).then(response => {
            this.loadCodebase();
        });
    }

    handleDeleteProfile(profile) {
        const service = new RepositoryService();
        service.deleteProfile(this.state.codebaseName, profile).then(response => {
            this.loadCodebase();
        });
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
                    <Button className="mr-1 mt-2">Move</Button>

                    <DropdownButton as={ButtonGroup} title={this.state.moveController} className="mr-1 mt-2">
                        {Object.values(this.state.codebase.profiles).flat().map(controller => <Dropdown.Item 
                            key={controller}
                            eventKey={controller}
                            onSelect={() => this.handleMoveController(controller)}>{controller}</Dropdown.Item>)}
                    </DropdownButton>

                    <Button className="mr-1 mt-2">To</Button>

                    <DropdownButton as={ButtonGroup} title={this.state.moveToProfile} className="mr-1 mt-2">
                        {Object.keys(this.state.codebase.profiles).map(p => <Dropdown.Item 
                            key={p}
                            eventKey={p}
                            onSelect={() => this.handleMoveToProfile(p)}>{p}</Dropdown.Item>)}
                    </DropdownButton>

                    <Button className="mt-2" onClick={this.handleMoveControllerSubmit}>Submit</Button>
                    </div>

                    {Object.keys(this.state.codebase.profiles).map(profile =>
                        <span>
                        <span style={{fontSize: '30px', fontWeight: 'bold'}}>{profile}</span>

                        <Button onClick={() => this.handleDeleteProfile(profile)} className="mr-4" variant="danger" size="sm">-</Button>

                        <ul>
                            {this.state.codebase.profiles[profile].map(controller =>
                                <li>{controller}</li>
                            )}
                        </ul> 
                        </span>
                    )}
                    </span>
                }
            </div>
        );
    }
}