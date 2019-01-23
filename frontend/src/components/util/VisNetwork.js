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
            conditions: '',
        };

        this.handleDoubleClick = this.handleDoubleClick.bind(this);
        this.handleCloseConditionsModal = this.handleCloseConditionsModal.bind(this);
        this.handleSelectNode = this.handleSelectNode.bind(this);
    }

    handleDoubleClick(event) {
        this.props.onSelection(event.nodes[0]);
    }

    handleCloseConditionsModal() {
        this.setState({
            showConditions: false,
            conditions: ''
        });
    }

    handleSelectNode(event) {
        const nodeId = event.nodes[0];
        if (nodeId) {
            this.setState({
                showConditions: true,
                conditions: this.props.graph.nodes.filter(n => n.id === nodeId)[0].title
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
                {this.state.showConditions && <ModalMessage title='Conditions' message={this.state.conditions} onClose={this.handleCloseConditionsModal} />}
                <div ref = {this.appRef}/>
            </div>
        );
    }
}