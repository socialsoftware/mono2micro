import { Network } from 'vis';
import React, { Component, createRef } from 'react';
import { ModalMessage } from './ModalMessage';
import { views, types } from '../view/ViewsMenu'

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
        this.handleDeselectNode = this.handleDeselectNode.bind(this);
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

    handleDeselectNode(event) {
        this.props.onDeselection(event.previousSelection.nodes[0]);
    }

    handleSelectEdge(event) {
        if (event.nodes.length === 0) {  //edge selected
            this.props.visGraph.nodes.update([{id: this.props.visGraph.edges.get(event.edges[0]).from, 
                                            color: {border: "#24CC48", background: "#24CC48"}},
                                           {id: this.props.visGraph.edges.get(event.edges[0]).to,
                                            color: {border: "#24CC48", background: "#24CC48"}}
                                          ]);
        } else {  //node selected
            let touchedNodes = event.edges.map(e => {
                if (this.props.visGraph.edges.get(e).from === event.nodes[0])
                    return {id: this.props.visGraph.edges.get(e).to, color: {border: "#24CC48", background: "#24CC48"}};
                else
                    return {id: this.props.visGraph.edges.get(e).from, color: {border: "#24CC48", background: "#24CC48"}};
            });
            this.props.visGraph.nodes.update(touchedNodes);
        }
    }

    handleDeselectEdge(event) {
        if (event.previousSelection.nodes.length === 0) {  //edge selected
            this.props.visGraph.nodes.update([{id: this.props.visGraph.edges.get(event.previousSelection.edges[0]).from, 
                                            color: {border: "#2B7CE9", background: "#D2E5FF"}},
                                           {id: this.props.visGraph.edges.get(event.previousSelection.edges[0]).to,
                                            color: {border: "#2B7CE9", background: "#D2E5FF"}}
                                          ]);
        } else {  //node selected
            let touchedNodes = event.previousSelection.edges.map(e => {
                    return [{id: this.props.visGraph.edges.get(e).to, color: {border: "#2B7CE9", background: "#D2E5FF"}}, 
                           {id: this.props.visGraph.edges.get(e).from, color: {border: "#2B7CE9", background: "#D2E5FF"}}];
            });
            this.props.visGraph.nodes.update(touchedNodes.flat());
        }

        if (event.edges.length === 1) {
            this.props.visGraph.nodes.update([{id: this.props.visGraph.edges.get(event.edges[0]).from, 
                                            color: {border: "#24CC48", background: "#24CC48"}},
                                           {id: this.props.visGraph.edges.get(event.edges[0]).to,
                                            color: {border: "#24CC48", background: "#24CC48"}}
                                          ]);
        }
    }

    handleStabilization(event) {
        this.network.setOptions( { physics: false } );
    }

    handleDoubleClick(event) {
        if (event.nodes.length === 0 && event.edges.length > 0) {  //edge double click
            if (this.props.view === views.CLUSTERS) {
                this.setState({
                    showModalMessage: true,
                    ModalMessageTitle: 'Controllers in common',
                    ModalMessageText: this.props.visGraph.edges.get(event.edges[0]).title
                });
            } else if (this.props.view === views.TRANSACTION) {
                let from = this.props.visGraph.edges.get(event.edges[0]).from;
                let to = this.props.visGraph.edges.get(event.edges[0]).to;
                this.setState({
                    showModalMessage: true,
                    ModalMessageTitle: 'Entities of ' + to + ' accessed by controller ' + from,
                    ModalMessageText: this.props.visGraph.edges.get(event.edges[0]).title
                });
            } else if (this.props.view === views.ENTITY) {
                let from = this.props.visGraph.edges.get(event.edges[0]).from;
                let to = this.props.visGraph.edges.get(event.edges[0]).to;
                this.setState({
                    showModalMessage: true,
                    ModalMessageTitle: 'Controllers that access ' + from + ' and ' + to,
                    ModalMessageText: this.props.visGraph.edges.get(event.edges[0]).title
                });
            }
        } else if (event.nodes.length > 0) {  //node double click
            if (this.props.view === views.CLUSTERS) {
                this.setState({
                    showModalMessage: true,
                    ModalMessageTitle: 'Entities of ' + this.props.visGraph.nodes.get(event.nodes[0]).label,
                    ModalMessageText: this.props.visGraph.nodes.get(event.nodes[0]).title
                });
            } else if (this.props.view === views.TRANSACTION) {
                if (this.props.visGraph.nodes.get(event.nodes[0]).type === types.CLUSTER) {
                    this.setState({
                        showModalMessage: true,
                        ModalMessageTitle: 'Entities of ' + this.props.visGraph.nodes.get(event.nodes[0]).label,
                        ModalMessageText: this.props.visGraph.nodes.get(event.nodes[0]).title
                    });
                } else if (this.props.visGraph.nodes.get(event.nodes[0]).type === types.CONTROLLER) {
                    this.setState({
                        showModalMessage: true,
                        ModalMessageTitle: 'Entities accessed by controller ' + this.props.visGraph.nodes.get(event.nodes[0]).label,
                        ModalMessageText: this.props.visGraph.nodes.get(event.nodes[0]).title
                    });
                }
            } else if (this.props.view === views.ENTITY) {
                if (this.props.visGraph.nodes.get(event.nodes[0]).type === types.ENTITY) {
                    this.setState({
                        showModalMessage: true,
                        ModalMessageTitle: 'Controllers that access ' + this.props.visGraph.nodes.get(event.nodes[0]).label,
                        ModalMessageText: this.props.visGraph.nodes.get(event.nodes[0]).title
                    });
                } else if (this.props.visGraph.nodes.get(event.nodes[0]).type === types.CLUSTER) {
                    this.setState({
                        showModalMessage: true,
                        ModalMessageTitle: 'Entities of ' + this.props.visGraph.nodes.get(event.nodes[0]).label,
                        ModalMessageText: this.props.visGraph.nodes.get(event.nodes[0]).title
                    });
                }
            }
        }
    }

    componentDidMount(){
        this.network = new Network(this.appRef.current, this.props.visGraph, this.props.options);
        this.network.on("doubleClick", this.handleDoubleClick);
        this.network.on("selectNode", this.handleSelectNode);
        this.network.on("deselectNode", this.handleDeselectNode);
        this.network.on("selectEdge", this.handleSelectEdge);
        this.network.on("deselectEdge", this.handleDeselectEdge);
        this.network.on("stabilizationIterationsDone", this.handleStabilization);
        this.network.storePositions();
    }

    componentDidUpdate(prevProps) {
        if (this.props.visGraph !== prevProps.visGraph) {
            this.network = new Network(this.appRef.current, this.props.visGraph, this.props.options);
            this.network.on("doubleClick", this.handleDoubleClick);
            this.network.on("selectNode", this.handleSelectNode);
            this.network.on("deselectNode", this.handleDeselectNode);
            this.network.on("selectEdge", this.handleSelectEdge);
            this.network.on("deselectEdge", this.handleDeselectEdge);
            this.network.on("stabilizationIterationsDone", this.handleStabilization);
            this.network.storePositions();
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