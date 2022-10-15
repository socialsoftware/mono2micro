import { Network } from "vis-network/standalone";
import React, {createRef, useEffect, useState} from 'react';
import { ModalMessage } from './ModalMessage';
import {types} from "./GraphUtils";
import {views} from "../accessesViews/AccessesViews";

export const VisNetwork = ({visGraph, options, onSelection, onDeselection, view}) => {
    let network = {};
    const appRef = createRef();

    const [showModalMessage, setShowModalMessage] = useState(false);
    const [ModalMessageTitle, setModalMessageTitle] = useState('');
    const [ModalMessageText, setModalMessageText] = useState('');

    useEffect(() => {
        try {
            network = new Network(appRef.current, visGraph, options);
            network.on("doubleClick", handleDoubleClick);
            network.on("selectNode", handleSelectNode);
            network.on("deselectNode", handleDeselectNode);
            network.on("deselectEdge", handleDeselectEdge);
            network.on("stabilizationIterationsDone", handleStabilization);
            network.storePositions();

        } catch (e) {
            console.error(e);
        }
    }, [visGraph]);

    function handleCloseModal() {
        setShowModalMessage(false);
        setModalMessageTitle('');
        setModalMessageText('');
    }

    function handleSelectNode(event) {
        onSelection(event.nodes[0]);
    }

    function handleDeselectNode(event) {
        onDeselection(event.previousSelection.nodes[0]);
    }

    function handleSelectEdge(event) {
        const {
            edges: clickedEdges,
            nodes: clickedNodes,
        } = event;

        const {
            edges: graphEdges,
            nodes: graphNodes,
        } = visGraph;

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

    function handleDeselectEdge(event) {
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
        } = visGraph;

        const color = { border: "#2B7CE9", background: "#D2E5FF" };

        if (previousSelectedNodes.length === 0) {  // edge selected
            graphNodes.update([
                {
                    id: previousSelectedEdges[0].fromId,
                    color,
                },
                {
                    id: previousSelectedEdges[0].toId,
                    color,
                }
            ]);

        } else {  // node selected
            let touchedNodes = previousSelectedEdges.flatMap(e => {
                return [ {id: e.toId, color}, {id: e.fromId, color} ];
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

    function handleStabilization(event) {
        network.setOptions( { physics: false } );
    }

    function handleDoubleClick(event) {
        const {
            nodes: clickedNodes, // Array of node labels working as IDs (idk why :shrug:)
            edges: clickedEdges, // Array of weird IDs
        } = event;

        const {
            edges: graphEdges,
            nodes: graphNodes,
        } = visGraph;

        const newShowModalMessage = true;
        let newModalMessageTitle;
        let newModalMessageText;

        if (clickedNodes.length === 0 && clickedEdges.length > 0) {  // edge double click
            const edge = graphEdges.get(clickedEdges[0]);
            
            const fromNodeId = edge.from;
            const toNodeId = edge.to;
            newModalMessageText = edge.title;
            
            const fromNode = graphNodes.get(fromNodeId);
            const toNode = graphNodes.get(toNodeId);

            if (view === views.FUNCTIONALITY) {

                newModalMessageTitle = 'Entities of cluster ' +
                                    toNode.label + ' accessed by ' +
                                    `${fromNode.type === types.FUNCTIONALITY ? "functionality " : "cluster "}` +
                                    fromNode.label;
                
            }

        } else if (clickedNodes.length > 0) {  // node double click
            const node = graphNodes.get(clickedNodes[0]);
            
            newModalMessageText = node.title;
            const clickedNodeLabel = node.label;
            const clickedNodeType = node.type;

            if (view === views.FUNCTIONALITY) {
                if (clickedNodeType === types.CLUSTER) {
                    newModalMessageTitle = 'Entities of ' + clickedNodeLabel;

                } else if (clickedNodeType === types.FUNCTIONALITY) {
                    newModalMessageTitle = 'Entities accessed by functionality ' + clickedNodeLabel;
                }

            }
        }

        if (newModalMessageTitle && newModalMessageText) {
            setShowModalMessage(newShowModalMessage);
            setModalMessageTitle(newModalMessageTitle);
            newModalMessageText = "<p>" + newModalMessageText.replaceAll('\n', "</br>") + "</p>";
            setModalMessageText(newModalMessageText);
        }
    }

    return (
        <>
            <ModalMessage
                show={showModalMessage}
                setShow={setShowModalMessage}
                title={ModalMessageTitle}
                message={ModalMessageText}
                onClose={handleCloseModal}
            />
            <div ref = {appRef}/>
        </>
    );
}