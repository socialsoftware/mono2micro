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

        this.handleCloseEntitiesModal = this.handleCloseEntitiesModal.bind(this);
        this.handleSelectNode = this.handleSelectNode.bind(this);
        this.handleHoverNode = this.handleHoverNode.bind(this);
        this.handleBlurNode = this.handleBlurNode.bind(this);
    }

    handleCloseEntitiesModal() {
        this.setState({
            showConditions: false,
            entities: ''
        });
    }

    handleSelectNode(event) {
        this.props.onSelection(event.nodes[0]);
    }

    handleHoverNode(event) {
        this.props.graph.nodes.update({id: event.node, label: this.props.clusters.filter(c => c.name === event.node)[0].entities.map(e => e.name).join('\n')});
    }

    handleBlurNode(event) {
        this.props.graph.nodes.update({id: event.node, label: event.node});
    }

    componentDidUpdate(prevProps) {
        if (this.props.graph !== prevProps.graph) {
            this.network = new Network(this.appRef.current, this.props.graph, this.props.options);
            this.network.on("selectNode", this.handleSelectNode);
            this.network.on("hoverNode", this.handleHoverNode);
            this.network.on("blurNode", this.handleBlurNode);
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