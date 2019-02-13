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
        this.handleSelectEdge = this.handleSelectEdge.bind(this);
        this.handleDeselectEdge = this.handleDeselectEdge.bind(this);
        this.handleStabilization = this.handleStabilization.bind(this);
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

    handleSelectEdge(event) {
        if (event.nodes.length === 0) {  //edge selected
            let from = this.props.graph.edges.get(event.edges[0]).from;
            let to = this.props.graph.edges.get(event.edges[0]).to;
            this.props.graph.nodes.update({id: from, color: {border: "#24CC48", background: "#24CC48"} });
            this.props.graph.nodes.update({id: to, color: {border: "#24CC48", background: "#24CC48"} });
        } else {  //node selected
            let edges = event.edges.map(e => this.props.graph.edges.get(e));
            for (var i = 0; i < edges.length; i++) {
                if (edges[i].from === event.nodes[0]) {
                    this.props.graph.nodes.update({id: edges[i].to, color: {border: "#24CC48", background: "#24CC48"} });
                } else {
                    this.props.graph.nodes.update({id: edges[i].from, color: {border: "#24CC48", background: "#24CC48"} });
                }
            }
        }
    }

    handleDeselectEdge(event) {
        if (event.previousSelection.nodes.length === 0) {  //edge selected
            let from = this.props.graph.edges.get(event.previousSelection.edges[0]).from;
            let to = this.props.graph.edges.get(event.previousSelection.edges[0]).to;
            this.props.graph.nodes.update({id: from, color: {border: "#2B7CE9", background: "#D2E5FF"} });
            this.props.graph.nodes.update({id: to, color: {border: "#2B7CE9", background: "#D2E5FF"} });
        } else {  //node selected
            let edges = event.previousSelection.edges.map(e => this.props.graph.edges.get(e));
            for (var i = 0; i < edges.length; i++) {
                this.props.graph.nodes.update({id: edges[i].from, color: {border: "#2B7CE9", background: "#D2E5FF"} });
                this.props.graph.nodes.update({id: edges[i].to, color: {border: "#2B7CE9", background: "#D2E5FF"} });
            }
        }
    }

    handleStabilization(event) {
        this.network.setOptions( { physics: false } );
    }

    componentDidUpdate(prevProps) {
        if (this.props.graph !== prevProps.graph) {
            this.network = new Network(this.appRef.current, this.props.graph, this.props.options);
            this.network.on("selectNode", this.handleSelectNode);
            this.network.on("selectEdge", this.handleSelectEdge);
            this.network.on("deselectEdge", this.handleDeselectEdge);
            this.network.on("stabilizationIterationsDone", this.handleStabilization);
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