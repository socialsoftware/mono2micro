export const EDGE_LENGTH = 400;

export const networkOptions = {
    height: "940",
    edges: {
        smooth: false,
        width: 0.5,
        arrows: {
            from: {
                enabled: false,
                scaleFactor: 0.5
            }
        },
    },
    interaction: {
        hover: true,
        multiselect: true,
    },
    physics: {
        enabled: true,
        //stabilization: {
        //    enabled: false // this greatly improves loading time, at the expense of a malformed initial graph
        //},
        barnesHut: {
            springLength: 500,
            springConstant: 0.001
        },
        solver: 'barnesHut'
    },
    layout: {
        improvedLayout: false
    }
};

export const types = {
    NONE: 0,
    CLUSTER: 1,
    FUNCTIONALITY: 2,
    ENTITY: 3,
    EDGE: 4,
    MULTIPLE: 5, // When selecting multiple nodes
    BETWEEN_CLUSTERS: 6,
    BETWEEN_ENTITIES: 7,
    BETWEEN_CLUSTER_ENTITY: 8,
};

export const createEntity = (entity, clusterName, position = undefined) => {
    let node = {
        id: entity.id,
        group: clusterName,
        type: types.ENTITY,
        label: entity.name,
        value: 1
    };
    if (position !== undefined) {
        node.x = position.x;
        node.y = position.y;
    }
    return node;
}

export const createCluster = (cluster, position = undefined) => {
    let node = {
        id: cluster.name,
        group: cluster.name,
        type: types.CLUSTER,
        elements: cluster.elements,
        label: cluster.name,
        value: cluster.elements.length,
        borderWidth: 3,
        shape: "box",
        scaling: { label: { enabled: true, min: 20, max: 80 } }
    }
    if (position !== undefined) {
        node.x = position.x;
        node.y = position.y;
    }
    return node;
}

export function expandCluster(clickedComponent, property, network, visGraph, edgeWeights, clusterNode) {
    if (clusterNode === undefined)
        clusterNode = clickedComponent.node;
    const position = network.getPosition(clusterNode.id);
    const relatedNodesAndEdges = getRelatedNodesAndEdges(visGraph, [clusterNode.id]);
    visGraph.nodes.remove(clusterNode.id);
    visGraph.edges.remove(relatedNodesAndEdges.edges.map(edge => edge.id));

    const nodes = clusterNode.elements.map(entity => createEntity(entity, clusterNode.id, position));
    visGraph.nodes.add(nodes);

    const newGraphEdges = generateNewAndAffectedEdges(edgeWeights, property, nodes, relatedNodesAndEdges.nodes);
    visGraph.edges.add(newGraphEdges);
}

export function expandAll(property, network, visGraph, edgeWeights) {
    let clusterNodes = [], newNodes = [], existingNodes = [];
    visGraph.nodes.get().forEach(node => {
        if (node.type === types.CLUSTER) {
            const position = network.getPosition(node.id);
            newNodes.push(...node.elements.map(entity => createEntity(entity, node.id, position)));
            clusterNodes.push(node);
        }
        else existingNodes.push(node);
    });

    visGraph.nodes.remove(clusterNodes.map(cluster => cluster.id));
    visGraph.edges.remove(visGraph.edges.get().filter(edge => edge.type !== types.BETWEEN_ENTITIES).map(edge => edge.id));
    visGraph.nodes.add(newNodes);

    const newGraphEdges = generateNewAndAffectedEdges(edgeWeights, property, newNodes, existingNodes);

    visGraph.edges.add(newGraphEdges);
}

export function collapseCluster(clickedComponent, property, visGraph, edgeWeights, clusterId) {
    if (clusterId === undefined)
        clusterId = clickedComponent.node.group;
    const entityNodes = visGraph.nodes.get().filter(node => node.group === clusterId);
    const clusterEntitiesIDs = entityNodes.map(node => node.id);
    const relatedNodesAndEdges = getRelatedNodesAndEdges(visGraph, clusterEntitiesIDs);
    const newClusterNode = createCluster({name: entityNodes[0].group, elements: entityNodes.map(node => {return {id: node.id, name: node.label}})});

    const newGraphEdges = generateNewAndAffectedEdges(edgeWeights, property, [newClusterNode], relatedNodesAndEdges.nodes);

    visGraph.nodes.remove(clusterEntitiesIDs);
    visGraph.edges.remove(relatedNodesAndEdges.edges.map(edge => edge.id));

    visGraph.nodes.add(newClusterNode);
    visGraph.edges.add(newGraphEdges);
}

export function collapseAll(property, visGraph, edgeWeights) {
    let existingClusterNodes = [], clusterNodeInformation = {};
    const entityNodes = visGraph.nodes.get().filter(node => {
        if (node.type === types.CLUSTER) {
            existingClusterNodes.push(node);
            return false;
        }
        else {
            let info = clusterNodeInformation[node.group];
            if (info === undefined)
                clusterNodeInformation[node.group] = {name: node.group, elements: [{id: node.id, name: node.label}]}
            else info.elements.push({id: node.id, name: node.label});
            return true;
        }
    });
    visGraph.nodes.remove(entityNodes.map(entity => entity.id));
    visGraph.edges.remove(visGraph.edges.get().filter(edge => edge.type !== types.BETWEEN_CLUSTERS).map(edge => edge.id));

    const newClusterNodes = Object.values(clusterNodeInformation).map(cluster => createCluster(cluster));
    const newGraphEdges = generateNewAndAffectedEdges(edgeWeights, property, newClusterNodes, existingClusterNodes);

    visGraph.nodes.add(newClusterNodes);
    visGraph.edges.add(newGraphEdges);
}

export function getRelatedNodesAndEdges(visGraph, nodeIds) {
    const relatedEdges = visGraph.edges.get().filter(edge => nodeIds.includes(edge.from) || nodeIds.includes(edge.to));
    const relatedNodes = [...new Set(relatedEdges.map(edge => nodeIds.includes(edge.from)? visGraph.nodes.get(edge.to) : visGraph.nodes.get(edge.from)))]
        .filter(node => !nodeIds.includes(node.id));
    return ({nodes: relatedNodes, edges: relatedEdges});
}

export function generateAllEdges(edgeWeights, property, nodes, graphEdges = [], removableEdges = undefined) {
    for (let i = 0; i < nodes.length; i++)
        for (let j = i + 1; j < nodes.length; j++)
            generateEdge(edgeWeights, property, nodes[i], nodes[j], graphEdges, removableEdges);
    return graphEdges;
}

export function generateNewAndAffectedEdges(edgeWeights, property, newNodes, affectedNodes, graphEdges = [], removableEdges = undefined) {
    for (let i = 0; i < newNodes.length; i++) // New entities
        for (let j = 0; j < affectedNodes.length; j++) // Affected neighbours
            generateEdge(edgeWeights, property, newNodes[i], affectedNodes[j], graphEdges, removableEdges);

    for (let i = 0; i < newNodes.length; i++) // Between new entities
        for (let j = i + 1; j < newNodes.length; j++)
            generateEdge(edgeWeights, property, newNodes[i], newNodes[j], graphEdges, removableEdges);
    return graphEdges;
}

export function generateEdge(edgeWeights, property, node1, node2, graphEdges, removableEdges = undefined) {
    // Both Clusters edge
    if (node1.type === types.CLUSTER && node2.type === types.CLUSTER) {
        let allFunctionalitiesInCommon = [], fullLength = 0, counter = 0;
        node1.elements.map(e => e.id).forEach(entity1 => {
            node2.elements.map(e => e.id).forEach(entity2 => {
                const edge = edgeWeights.find(edge => edge.e1ID === entity1 && edge.e2ID === entity2 || edge.e1ID === entity2 && edge.e2ID === entity1);
                if (edge !== undefined) {
                    allFunctionalitiesInCommon.push(...edge[property]);
                    fullLength += edge.dist;
                    counter++;
                    if ('superclass' in edge && edge.superclass.length > 0) {
                        allFunctionalitiesInCommon.push(...edge.superclass);
                        fullLength += edge.dist;
                        counter++;
                    }
                }
            });
        });

        if (allFunctionalitiesInCommon.length !== 0) {
            const uniqueFunctionalitiesInCommon = [...new Set(allFunctionalitiesInCommon)];
            graphEdges.push(createEdge(node1.id, node2.id, types.BETWEEN_CLUSTERS, fullLength / counter, uniqueFunctionalitiesInCommon.length));
        }
        else if (removableEdges !== undefined)
            removableEdges.push(getEdgeId(node1.id, node2.id));
    }
    // Both Entities edge
    else if (node1.type === types.ENTITY && node2.type === types.ENTITY) {
        const edge = edgeWeights.find(edge => edge.e1ID === node1.id && edge.e2ID === node2.id || edge.e1ID === node2.id && edge.e2ID === node1.id);
        if (edge !== undefined) {
            graphEdges.push(createEdge(node1.id, node2.id, types.BETWEEN_ENTITIES, edge.dist, edge[property].length));
        }
        else if (removableEdges !== undefined)
            removableEdges.push(getEdgeId(node1.id, node2.id));
    }
    // Cluster and Entity edge
    else {
        let allFunctionalitiesInCommon = [], fullLength = 0, counter = 0;
        const cluster = node1.type === types.CLUSTER ? node1 : node2, entity = cluster === node1 ? node2 : node1;
        cluster.elements.forEach(clusterEntity => {
            const edge = edgeWeights.find(edge => edge.e1ID === clusterEntity.id && edge.e2ID === entity.id || edge.e1ID === entity.id && edge.e2ID === clusterEntity.id);
            if (edge !== undefined) {
                allFunctionalitiesInCommon.push(...edge[property]);
                fullLength += edge.dist;
                counter++;
                if ('superclass' in edge && edge.superclass.length > 0) {
                    allFunctionalitiesInCommon.push(...edge.superclass);
                    fullLength += edge.dist;
                    counter++;
                }
            }
        });

        if (allFunctionalitiesInCommon.length !== 0) {
            const uniqueFunctionalitiesInCommon = [...new Set(allFunctionalitiesInCommon)]; // filters repeated values
            graphEdges.push(createEdge(cluster.id, entity.id, types.BETWEEN_CLUSTER_ENTITY, fullLength / counter, uniqueFunctionalitiesInCommon.length));
        }
        else if (removableEdges !== undefined)
            removableEdges.push(getEdgeId(node1.id, node2.id));
    }
}

const getEdgeId = (from, to) => {
    if (from < to)
        return from + "&" + to;
    return to + "&" + from;
}

export const createEdge = (from, to, type, length, value) => {
    if (from < to)
        return { from: from, to: to, id: from + "&" + to, type, length: length * EDGE_LENGTH, value };
    return { from: to, to: from, id: to + "&" + from, type, length: length * EDGE_LENGTH, value };
}
