import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { ViewsMenu, views } from './ViewsMenu';
import { ClusterView, cluster_tooltip } from './ClusterView';
import { TransactionView, transaction_tooltip } from './TransactionView';
import { EntityView, entity_tooltip } from './EntityView';
import { OverlayTrigger, Button, InputGroup, FormControl, ButtonToolbar, Popover } from 'react-bootstrap';

var HttpStatus = require('http-status-codes');

export class Views extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            graphName: this.props.match.params.name,
            inputValue: this.props.match.params.name,
            renameGraphMode: false,
            view: views.CLUSTERS,
            tooltip: cluster_tooltip
        }

        this.handleSelectView = this.handleSelectView.bind(this);
        this.getTooltip = this.getTooltip.bind(this);
        this.handleDoubleClick = this.handleDoubleClick.bind(this);
        this.handleRenameGraph = this.handleRenameGraph.bind(this);
        this.handleRenameGraphSubmit = this.handleRenameGraphSubmit.bind(this);
        this.handleClose = this.handleClose.bind(this);
    }

    componentWillReceiveProps(nextProps) {
        this.setState({
            graphName: nextProps.match.params.name,
            inputValue: nextProps.match.params.name
        });
    }

    handleSelectView(value) {
        this.setState({
            view: value,
            tooltip: this.getTooltip(value)
        });
    }

    getTooltip(view) {
        switch(view) {
            case views.CLUSTERS:
                return cluster_tooltip;
            case views.TRANSACTION:
                return transaction_tooltip;
            case views.ENTITY:
                return entity_tooltip;
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
        service.renameGraph(this.state.graphName, this.state.inputValue).then(response => {
            if (response.status === HttpStatus.OK) {
                this.props.location.headerFunction.handleGetGraphsFunction();
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
        const popover = (
            <Popover id="popover-basic" title={this.state.view}>
              {this.getTooltip(this.state.view)}
            </Popover>
        );

        const showGraphName = (<h3>{this.state.graphName}</h3>);
        
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
            <div>
                <div onDoubleClick={this.handleDoubleClick}>
                    {this.state.renameGraphMode ? editGraphName : showGraphName}
                </div><br />
                <ViewsMenu
                    handleSelectView={this.handleSelectView}
                />
                <OverlayTrigger trigger="click" placement="right" overlay={popover}>
                    <Button className="mb-2" variant="success">Help</Button>
                </OverlayTrigger>
                {this.state.view === views.CLUSTERS &&
                    <ClusterView name={this.state.graphName} />
                }
                {this.state.view === views.TRANSACTION &&
                    <TransactionView name={this.state.graphName} />
                }
                {this.state.view === views.ENTITY &&
                    <EntityView name={this.state.graphName} />
                }
            </div>
        );
    }
}