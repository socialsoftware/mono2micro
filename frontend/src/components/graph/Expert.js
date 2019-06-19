import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { Form, ButtonGroup, Button, InputGroup, FormControl, Breadcrumb, BreadcrumbItem, Dropdown, DropdownButton, ButtonToolbar } from 'react-bootstrap';


export class Expert extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            expertName: this.props.match.params.expertName,
            expert: {},
            newClusterName: "",
            moveToCluster: "",
            selectedEntities: []
        };

        this.handleChangeNewClusterName = this.handleChangeNewClusterName.bind(this);
        this.handleNewClusterSubmit = this.handleNewClusterSubmit.bind(this);
        this.handleMoveEntitiesSubmit = this.handleMoveEntitiesSubmit.bind(this);
        this.handleSelectCluster = this.handleSelectCluster.bind(this);
    }

    componentDidMount() {
        this.loadExpert();
    }

    loadExpert() {
        const service = new RepositoryService();
        service.getExpert(this.state.expertName).then(response => {
            this.setState({
                expert: response.data
            });
        });
    }

    handleChangeNewClusterName(event) {
        this.setState({
            newClusterName: event.target.value
        });
    }

    handleNewClusterSubmit() {
        const service = new RepositoryService();
        service.addCluster(this.state.expertName, this.state.newClusterName).then(response => {
            this.loadExpert();
        });
    }

    handleMoveToCluster(cluster) {
        this.setState({
            moveToCluster: cluster
        });
    }

    handleMoveEntitiesSubmit() {
        const service = new RepositoryService();
        service.moveEntities(this.state.expertName, this.state.selectedEntities, this.state.moveToCluster).then(response => {
            this.setState({
                selectedEntities: []
            })
            this.loadExpert();
        });
    }

    handleDeleteCluster(cluster) {
        const service = new RepositoryService();
        service.deleteCluster(this.state.expertName, cluster).then(response => {
            this.loadExpert();
        });
    }

    handleSelectCluster(event) {
        if (this.state.selectedEntities.includes(event.target.id)) {
            let filteredArray = this.state.selectedEntities.filter(c => c !== event.target.id);
            this.setState({
                selectedEntities: filteredArray
            });
        } else {
            this.setState({
                selectedEntities: [...this.state.selectedEntities, event.target.id]
            });
        }
    }

    render() {
        const BreadCrumbs = () => {
            return (
              <div>
                <Breadcrumb>
                  <Breadcrumb.Item href="/">Home</Breadcrumb.Item>
                  <Breadcrumb.Item href="/experts">Experts</Breadcrumb.Item>
                  <BreadcrumbItem active>{this.state.expertName}</BreadcrumbItem>
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
                        <InputGroup.Text id="newcluster">New Cluster</InputGroup.Text>
                    </InputGroup.Prepend>
                    <FormControl
                        type="text"
                        value={this.state.newClusterName}
                        onChange={this.handleChangeNewClusterName}/>
                </InputGroup>

                <Button className="mr-1" onClick={this.handleNewClusterSubmit}>Submit</Button>
                </ButtonToolbar>

                {Object.keys(this.state.expert).length !== 0 &&
                    <span>
                    <div>
                    <Button className="mr-1 mt-2">Move selected entities to</Button>

                    <DropdownButton as={ButtonGroup} title={this.state.moveToCluster === "" ? "Cluster" : this.state.moveToCluster} className="mr-1 mt-2">
                        {Object.keys(this.state.expert.clusters).map(p => <Dropdown.Item 
                            key={p}
                            eventKey={p}
                            onSelect={() => this.handleMoveToCluster(p)}>{p}</Dropdown.Item>)}
                    </DropdownButton>

                    <Button disabled={this.state.selectedEntities.length === 0 || this.state.moveToCluster === ""} className="mt-2" onClick={this.handleMoveEntitiesSubmit}>Submit</Button>
                    </div>

                    {Object.keys(this.state.expert.clusters).map(cluster =>
                        <span>
                        <span style={{fontSize: '30px', fontWeight: 'bold'}}>{cluster}

                        {this.state.expert.clusters[cluster].length === 0 && <Button onClick={() => this.handleDeleteCluster(cluster)} className="mr-4" variant="danger" size="sm">-</Button>}
                        </span>
                        
                            {this.state.expert.clusters[cluster].map(entity =>
                                <Form.Check checked={this.state.selectedEntities.includes(entity)} style={{ paddingLeft: "3em" }} onClick={this.handleSelectCluster} label={entity} type="checkbox" id={entity} />
                            )}
                         
                        </span>
                    )}
                    </span>
                }
            </div>
        );
    }
}