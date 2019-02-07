import { Network } from 'vis';
import React, { Component, createRef } from 'react';
import { ModalMessage } from './ModalMessage';

export class VisNetwork extends Component {
    constructor(props) {
        super(props);
        this.network = {};
        this.appRef = createRef();

        this.state = {
            showConditions: false,
            entities: '',
        };

        this.handleDoubleClick = this.handleDoubleClick.bind(this);
        this.handleCloseEntitiesModal = this.handleCloseEntitiesModal.bind(this);
        this.handleSelectNode = this.handleSelectNode.bind(this);
    }

    handleDoubleClick(event) {
        this.props.onSelection(event.nodes[0]);
    }

    handleCloseEntitiesModal() {
        this.setState({
            showConditions: false,
            entities: ''
        });
    }

    handleSelectNode(event) {
        const nodeId = event.nodes[0];
        if (nodeId) {
            this.setState({
                showConditions: true,
                entities: this.props.clusters.filter(c => c.name === nodeId)[0].entities.join('<br />')
            });
        }
    }

    componentDidUpdate(prevProps) {
        if (this.props.graph !== prevProps.graph) {
            this.network = new Network(this.appRef.current, this.props.graph, this.props.options);
            this.network.on("selectNode", this.handleSelectNode);
            this.network.on("doubleClick", this.handleDoubleClick);
        }
    }

    render() {
        return ( 
            <div>
                {this.state.showConditions && <ModalMessage title='Entities' message={this.state.entities} onClose={this.handleCloseEntitiesModal} />}
                <div ref = {this.appRef}/>
            </div>
        );
    }
}