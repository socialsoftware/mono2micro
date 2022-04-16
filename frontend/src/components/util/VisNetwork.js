import { Network } from "vis";
import React, { Component, createRef } from 'react';
import { ModalMessage } from './ModalMessage';
import { views, types } from '../view/Views'

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
        const {
            edges: clickedEdges,
            nodes: clickedNodes,
        } = event;

        const {
            edges: graphEdges,
            nodes: graphNodes,
        } = this.props.visGraph;

        const color = { border: "#24CC48", background: "#24CC48" };

        if (clickedNodes.length === 0) {  //edge selected
            graphNodes.update([
                {
                    id: graphEdges.get(clickedEdges[0]).from, 
                    color, 
                },
                {
                    id: graphEdges.get(clickedEdges[0]).to,
                    color,
                }
            ]);
        } else {  //node selected
            let touchedNodes = clickedEdges.map(e => {
                if (graphEdges.get(e).from === clickedNodes[0]) {
                    return {
                        id: graphEdges.get(e).to,
                        color,
                    };
                }
                
                return {
                    id: graphEdges.get(e).from,
                    color,
                };
            });

            graphNodes.update(touchedNodes);
        }
    }

    handleDeselectEdge(event) {
        const {
            edges: clickedEdges,
            previousSelection: {
                nodes: previousSelectedNodes,
                edges: previousSelectedEdges,
            },
        } = event;

        const {
            edges: graphEdges,
            nodes: graphNodes,
        } = this.props.visGraph;

        const color = { border: "#2B7CE9", background: "#D2E5FF" };

        if (previousSelectedNodes.length === 0) {  // edge selected
            graphNodes.update([
                {
                    id: graphEdges.get(previousSelectedEdges[0]).from, 
                    color,
                },
                {
                    id: graphEdges.get(previousSelectedEdges[0]).to,
                    color,
                }
            ]);

        } else {  // node selected
            let touchedNodes = previousSelectedEdges.flatMap(e => {
                return [
                    {
                        id: graphEdges.get(e).to,
                        color,
                    }, 
                    {
                        id: graphEdges.get(e).from,
                        color,
                    }
                ];
            });

            graphNodes.update(touchedNodes);
        }

        if (clickedEdges.length === 1) {
            graphNodes.update([
                {
                    id: graphEdges.get(clickedEdges[0]).from, 
                    color: { border: "#24CC48", background: "#24CC48" }},
                {
                    id: graphEdges.get(clickedEdges[0]).to,
                    color: { border: "#24CC48", background: "#24CC48" }}
            ]);
        }
    }

    handleStabilization(event) {
        this.network.setOptions( { physics: false } );
    }

    handleDoubleClick(event) {
        const {
            nodes: clickedNodes, // Array of node labels working as IDs (idk why :shrug:)
            edges: clickedEdges, // Array of weird IDs
        } = event;

        const {
            view,
            visGraph: {
                nodes: graphNodes,
                edges: graphEdges,
            }
        } = this.props;

        const showModalMessage = true;
        let ModalMessageTitle;
        let ModalMessageText;

        
        if (clickedNodes.length === 0 && clickedEdges.length > 0) {  // edge double click
            const edge = graphEdges.get(clickedEdges[0]);
            
            const fromNodeId = edge.from;
            const toNodeId = edge.to;
            ModalMessageText = edge.title;
            
            const fromNode = graphNodes.get(fromNodeId);
            const toNode = graphNodes.get(toNodeId);

            if (view === views.CLUSTERS) {
                ModalMessageTitle = 'Controllers in common';
            
            } else if (view === views.TRANSACTION) {

                ModalMessageTitle = 'Entities of cluster ' + 
                                    toNode.label + ' accessed by ' +
                                    `${fromNode.type === types.CONTROLLER ? "controller " : "cluster "}` +
                                    fromNode.label;
                
            } else if (view === views.ENTITY) {
                ModalMessageTitle = 'Controllers that access entity ' + fromNode.label + ' and cluster ' + toNode.label;
            }
              else if (view === views.COMMIT_CLUSTERS) {
                ModalMessageTitle = 'Files of cluster ' + toNode.label;
            }


        } else if (clickedNodes.length > 0) {  // node double click
            const node = graphNodes.get(clickedNodes[0]);
            
            ModalMessageText = node.title;
            const clickedNodeLabel = node.label;
            const clickedNodeType = node.type;

            if (view === views.CLUSTERS) {
                ModalMessageTitle = 'Entities of ' + clickedNodeLabel;

            } else if (view === views.TRANSACTION) {
                if (clickedNodeType === types.CLUSTER) {
                    ModalMessageTitle = 'Entities of ' + clickedNodeLabel;

                } else if (clickedNodeType === types.CONTROLLER) {
                    ModalMessageTitle = 'Entities accessed by controller ' + clickedNodeLabel;
                }

            } else if (view === views.ENTITY) {
                if (clickedNodeType === types.ENTITY) {
                    ModalMessageTitle = 'Controllers that access ' + clickedNodeLabel;

                } else if (clickedNodeType === types.CLUSTER) {
                    ModalMessageTitle = 'Entities of ' + clickedNodeLabel;
                }
            }
            else if (view === views.COMMIT_CLUSTERS) {
                ModalMessageTitle = 'Files of ' + node.label;
                ModalMessageText = "<b> Entities: </b> <br/>";
                for (let entity of node.entities) {
                    ModalMessageText += entity + "<br/>";
                }
                ModalMessageText += "<br/>"
                if (node.others.length > 0) {
                    ModalMessageText += "<b> Other files: </b> <br/>";
                    for (let other of node.others) {
                        ModalMessageText += other + "<br/>";
                    }
                }
            }
        }

        if (ModalMessageTitle && ModalMessageText) {
            this.setState({
                showModalMessage,
                ModalMessageTitle,
                ModalMessageText,
            });
        }
    }

    componentDidMount(){
        try {
            this.network = new Network(this.appRef.current, this.props.visGraph, this.props.options);
            this.network.on("doubleClick", this.handleDoubleClick);
            this.network.on("selectNode", this.handleSelectNode);
            this.network.on("deselectNode", this.handleDeselectNode);
            this.network.on("selectEdge", this.handleSelectEdge);
            this.network.on("deselectEdge", this.handleDeselectEdge);
            this.network.on("stabilizationIterationsDone", this.handleStabilization);
            this.network.storePositions();

        } catch (e) {
            console.error(e);
        } 
    }

    componentDidUpdate(prevProps) {
        if (this.props.visGraph !== prevProps.visGraph) {
            try {
                this.network = new Network(this.appRef.current, this.props.visGraph, this.props.options);
                this.network.on("doubleClick", this.handleDoubleClick);
                this.network.on("selectNode", this.handleSelectNode);
                this.network.on("deselectNode", this.handleDeselectNode);
                this.network.on("selectEdge", this.handleSelectEdge);
                this.network.on("deselectEdge", this.handleDeselectEdge);
                this.network.on("stabilizationIterationsDone", this.handleStabilization);
                this.network.storePositions();
    
            } catch (e) {
                console.error(e);
            } 
        }
    }

    render() {
        return (
            <>
                {
                    this.state.showModalMessage && (
                        <ModalMessage
                            title={this.state.ModalMessageTitle}
                            message={this.state.ModalMessageText}
                            onClose={this.handleCloseModal}
                        />
                    )
                }
                <div ref = {this.appRef}/>
            </>
        );
    }
}