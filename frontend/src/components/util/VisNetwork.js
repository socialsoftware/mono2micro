import { Network } from 'vis';
import React, { Component, createRef } from 'react';
import { ModalMessage } from './ModalMessage';

export class VisNetwork extends Component {
    constructor(props) {
        super(props);
        this.network = {};
        this.appRef = createRef();

        this.state = {
            showModalMessage: false,
            ModalMessageTitle: '',
            ModalMessageText: ''
        };

        this.handleCloseModal = this.handleCloseModal.bind(this);
        this.handleSelectNode = this.handleSelectNode.bind(this);
        this.handleSelectEdge = this.handleSelectEdge.bind(this);
        this.handleDeselectEdge = this.handleDeselectEdge.bind(this);
        this.handleStabilization = this.handleStabilization.bind(this);
        this.handleDoubleClick = this.handleDoubleClick.bind(this);
    }

    handleCloseModal() {
        this.setState({
            showModalMessage: false,
            ModalMessageTitle: '',
            ModalMessageText: ''
        });
    }

    handleSelectNode(event) {
        this.props.onSelection(event.nodes[0]);
    }

    handleSelectEdge(event) {
        if (event.nodes.length === 0) {  //edge selected
            this.props.graph.nodes.update([{id: this.props.graph.edges.get(event.edges[0]).from, 
                                            color: {border: "#24CC48", background: "#24CC48"}},
                                           {id: this.props.graph.edges.get(event.edges[0]).to,
                                            color: {border: "#24CC48", background: "#24CC48"}}
                                          ]);
        } else {  //node selected
            let touchedNodes = event.edges.map(e => {
                if (this.props.graph.edges.get(e).from === event.nodes[0])
                    return {id: this.props.graph.edges.get(e).to, color: {border: "#24CC48", background: "#24CC48"}};
                else
                    return {id: this.props.graph.edges.get(e).from, color: {border: "#24CC48", background: "#24CC48"}};
            });
            this.props.graph.nodes.update(touchedNodes);
        }
    }

    handleDeselectEdge(event) {
        if (event.previousSelection.nodes.length === 0) {  //edge selected
            this.props.graph.nodes.update([{id: this.props.graph.edges.get(event.previousSelection.edges[0]).from, 
                                            color: {border: "#2B7CE9", background: "#D2E5FF"}},
                                           {id: this.props.graph.edges.get(event.previousSelection.edges[0]).to,
                                            color: {border: "#2B7CE9", background: "#D2E5FF"}}
                                          ]);
        } else {  //node selected
            let touchedNodes = event.previousSelection.edges.map(e => {
                    return [{id: this.props.graph.edges.get(e).to, color: {border: "#2B7CE9", background: "#D2E5FF"}}, 
                           {id: this.props.graph.edges.get(e).from, color: {border: "#2B7CE9", background: "#D2E5FF"}}];
            });
            this.props.graph.nodes.update(touchedNodes.flat());
        }

        if (event.edges.length === 1) {
            this.props.graph.nodes.update([{id: this.props.graph.edges.get(event.edges[0]).from, 
                                            color: {border: "#24CC48", background: "#24CC48"}},
                                           {id: this.props.graph.edges.get(event.edges[0]).to,
                                            color: {border: "#24CC48", background: "#24CC48"}}
                                          ]);
        }
    }

    handleStabilization(event) {
        this.network.setOptions( { physics: false } );
    }

    handleDoubleClick(event) {
        if (event.nodes.length === 0) {  //edge double click
            this.setState({
                showModalMessage: true,
                ModalMessageTitle: 'Controllers in common',
                ModalMessageText: this.props.graph.edges.get(event.edges[0]).title
            });
        } else {  //node double click
            this.setState({
                showModalMessage: true,
                ModalMessageTitle: 'Entities of ' + event.nodes[0],
                ModalMessageText: this.props.graph.nodes.get(event.nodes[0]).title
            });
        }
    }

    componentDidUpdate(prevProps) {
        if (this.props.graph !== prevProps.graph) {
            this.network = new Network(this.appRef.current, this.props.graph, this.props.options);
            this.network.on("doubleClick", this.handleDoubleClick);
            this.network.on("selectNode", this.handleSelectNode);
            this.network.on("selectEdge", this.handleSelectEdge);
            this.network.on("deselectEdge", this.handleDeselectEdge);
            this.network.on("stabilizationIterationsDone", this.handleStabilization);
        }
    }

    render() {
        return ( 
            <div>
                {this.state.showModalMessage && <ModalMessage title={this.state.ModalMessageTitle} message={this.state.ModalMessageText} onClose={this.handleCloseModal} />}
                <div ref = {this.appRef}/>
            </div>
        );
    }
}