import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { ViewsMenu, views } from './ViewsMenu';
import { ClusterView, clusterViewHelp } from './ClusterView';
import { TransactionView, transactionViewHelp } from './TransactionView';
import { EntityView, entityViewHelp } from './EntityView';
import { OverlayTrigger, Button, InputGroup, FormControl, ButtonToolbar, Popover, Container, Row, Col, Breadcrumb, BreadcrumbItem } from 'react-bootstrap';

var HttpStatus = require('http-status-codes');

export class Views extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            graphName: this.props.match.params.graphName,
            inputValue: this.props.match.params.graphName,
            dendrogramName: this.props.match.params.dendrogramName,
            renameGraphMode: false,
            view: views.CLUSTERS,
            help: clusterViewHelp
        }

        this.handleSelectView = this.handleSelectView.bind(this);
        this.getHelpText = this.getHelpText.bind(this);
        this.handleDoubleClick = this.handleDoubleClick.bind(this);
        this.handleRenameGraph = this.handleRenameGraph.bind(this);
        this.handleRenameGraphSubmit = this.handleRenameGraphSubmit.bind(this);
        this.handleClose = this.handleClose.bind(this);
    }

    componentWillReceiveProps(nextProps) {
        this.setState({
            dendrogramName: nextProps.match.params.dendrogramName,
            graphName: nextProps.match.params.name,
            inputValue: nextProps.match.params.name
        });
    }

    handleSelectView(value) {
        this.setState({
            view: value,
            help: this.getHelpText(value)
        });
    }

    getHelpText(view) {
        switch(view) {
            case views.CLUSTERS:
                return clusterViewHelp;
            case views.TRANSACTION:
                return transactionViewHelp;
            case views.ENTITY:
                return entityViewHelp;
            default:
                return null;
        }
    }

    handleDoubleClick() {
        this.setState({
            renameGraphMode: true
        });
    }

    handleRenameGraph(event) {
        this.setState({ 
            inputValue: event.target.value 
        });
    }

    handleRenameGraphSubmit() {
        const service = new RepositoryService();
        service.renameGraph(this.state.dendrogramName, this.state.graphName, this.state.inputValue).then(response => {
            if (response.status === HttpStatus.OK) {
                this.setState({
                    renameGraphMode: false,
                    graphName: this.state.inputValue
                });
            } else {
                this.setState({
                    renameGraphMode: false
                });
            }
        })
        .catch(error => {
            console.log(error);
            this.setState({
                renameGraphMode: false
            });
        });
    }

    handleClose() {
        this.setState({
            renameGraphMode: false,
            inputValue: this.state.graphName
        });
    }

    render() {
        const BreadCrumbs = () => {
            return (
              <div>
                <Breadcrumb>
                  <Breadcrumb.Item href="/">Home</Breadcrumb.Item>
                  <Breadcrumb.Item href="/dendrograms">Dendrograms</Breadcrumb.Item>
                  <Breadcrumb.Item href={`/dendrogram/${this.state.dendrogramName}`}>{this.state.dendrogramName}</Breadcrumb.Item>
                  <BreadcrumbItem active>{this.state.graphName}</BreadcrumbItem>
                </Breadcrumb>
              </div>
            );
        };

        const helpPopover = (
            <Popover id="popover-basic" title={this.state.view}>
              {this.getHelpText(this.state.view)}
            </Popover>
        );

        const showGraphName = (<span><span style={{fontSize: '30px', fontWeight: 'bold'}}>{this.state.graphName}</span><span style={{fontSize: '12px', fontWeight: 'lighter'}}>(double click to rename)</span></span>);
        
        const editGraphName = (
            <ButtonToolbar>
                <InputGroup className="mr-1">
                    <FormControl 
                        type="text"
                        placeholder="Rename Graph"
                        value={this.state.inputValue}
                        onChange={this.handleRenameGraph}/>
                </InputGroup>
                <Button className="mr-1" onClick={this.handleRenameGraphSubmit}>Rename</Button>
                <Button onClick={this.handleClose}>Cancel</Button>
            </ButtonToolbar>);

        return (
            <Container>
                <BreadCrumbs />
                <Row>
                    <Col>
                        <div onDoubleClick={this.handleDoubleClick}>
                            {this.state.renameGraphMode ? editGraphName : showGraphName}
                        </div><br />
                    </Col>
                    <Col>
                        <OverlayTrigger trigger="click" placement="left" overlay={helpPopover}>
                            <Button className="float-right" variant="success">Help</Button>
                        </OverlayTrigger>
                    </Col>
                </Row>
                <Row>
                    <Col>
                        <ViewsMenu
                            handleSelectView={this.handleSelectView}
                        />
                    </Col>
                </Row>
                <Row>
                    <Col>
                        {this.state.view === views.CLUSTERS &&
                            <ClusterView 
                                dendrogramName={this.state.dendrogramName} 
                                graphName={this.state.graphName} />
                        }
                        {this.state.view === views.TRANSACTION &&
                            <TransactionView 
                                dendrogramName={this.state.dendrogramName} 
                                graphName={this.state.graphName} />
                        }
                        {this.state.view === views.ENTITY &&
                            <EntityView 
                                dendrogramName={this.state.dendrogramName} 
                                graphName={this.state.graphName} />
                        }
                    </Col>
                </Row>
            </Container>
        );
    }
}