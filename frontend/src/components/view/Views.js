import React from 'react';
import { ClusterView, clusterViewHelp } from './ClusterView';
import { TransactionView, transactionViewHelp } from './TransactionView';
import { EntityView, entityViewHelp } from './EntityView';
import { Dropdown, DropdownButton, OverlayTrigger, Button, Popover, Container, Row, Col, Breadcrumb} from 'react-bootstrap';

export const views = {
    CLUSTERS: 'Clusters View',
    TRANSACTION: 'Transaction View',
    ENTITY: 'Entity View'
};

export const types = {
    CLUSTER: 0,
    CONTROLLER: 1,
    ENTITY: 2
};

export class Views extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            codebaseName: this.props.match.params.codebaseName,
            dendrogramName: this.props.match.params.dendrogramName,
            graphName: this.props.match.params.graphName,
            view: views.CLUSTERS
        }

        this.handleSelectView = this.handleSelectView.bind(this);
        this.getHelpText = this.getHelpText.bind(this);
    }

    handleSelectView(value) {
        this.setState({
            view: value
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

    renderBreadCrumbs = () => {
        return (
            <Breadcrumb>
                <Breadcrumb.Item href="/">Home</Breadcrumb.Item>
                <Breadcrumb.Item href="/codebases">Codebases</Breadcrumb.Item>
                <Breadcrumb.Item href={`/codebases/${this.state.codebaseName}`}>{this.state.codebaseName}</Breadcrumb.Item>
                <Breadcrumb.Item href={`/codebases/${this.state.codebaseName}/dendrograms`}>Dendrograms</Breadcrumb.Item>
                <Breadcrumb.Item href={`/codebases/${this.state.codebaseName}/dendrograms/${this.state.dendrogramName}`}>{this.state.dendrogramName}</Breadcrumb.Item>
                <Breadcrumb.Item active>{this.state.graphName}</Breadcrumb.Item>
            </Breadcrumb>
        );
    }

    render() {
        const helpPopover = (
            <Popover id="helpPopover" title={this.state.view}>
                {this.getHelpText(this.state.view)}
            </Popover>
        );  

        return (
            <Container>
                {this.renderBreadCrumbs()}
                <Row className="mb-2">
                    <Col>
                        <h4 style={{color: "#666666"}}>{this.state.graphName}</h4>
                    </Col>
                    <Col>
                        <OverlayTrigger trigger="click" placement="left" overlay={helpPopover}>
                            <Button className="float-right" variant="success">Help</Button>
                        </OverlayTrigger>
                    </Col>
                </Row>
                <Row className="mb-2">
                    <Col>
                        <DropdownButton title={this.state.view}>
                            <Dropdown.Item onClick={() => this.handleSelectView(views.CLUSTERS)}>{views.CLUSTERS}</Dropdown.Item>
                            <Dropdown.Item onClick={() => this.handleSelectView(views.TRANSACTION)}>{views.TRANSACTION}</Dropdown.Item>
                            <Dropdown.Item onClick={() => this.handleSelectView(views.ENTITY)}>{views.ENTITY}</Dropdown.Item>
                        </DropdownButton>
                    </Col>
                </Row>
                <Row>
                    <Col>
                        {this.state.view === views.CLUSTERS &&
                            <ClusterView
                                codebaseName={this.state.codebaseName}
                                dendrogramName={this.state.dendrogramName} 
                                graphName={this.state.graphName} />
                        }
                        {this.state.view === views.TRANSACTION &&
                            <TransactionView
                                codebaseName={this.state.codebaseName}
                                dendrogramName={this.state.dendrogramName} 
                                graphName={this.state.graphName} />
                        }
                        {this.state.view === views.ENTITY &&
                            <EntityView
                                codebaseName={this.state.codebaseName}
                                dendrogramName={this.state.dendrogramName} 
                                graphName={this.state.graphName} />
                        }
                    </Col>
                </Row>
            </Container>
        );
    }
}