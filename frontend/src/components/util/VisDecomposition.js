import { DataSet, Network } from 'vis';
import React, { Component, createRef } from "react";

/*const nodes = new DataSet([
  { id: 1, label: 'Node 1', level: -2 },
  { id: 2, label: 'Node 2', level: -1 },
  { id: 3, label: 'Node 3', level: 0 },
  { id: 4, label: 'Node 4', level: 0 },
  { id: 5, label: 'Node 5', level: 0 }
]);

// create an array with edges
const edges = new DataSet([
  { from: 1, to: 2 },
  { from: 1, to: 3 },
  { from: 1, to: 4 },
  { from: 2, to: 5 }
]);

const data = {
  nodes: nodes,
  edges: edges
}*/

const options = {
    physics:{
      enabled: false
    },

    interaction:{
      //dragNodes: false,
      //dragView: false
    },

    layout: {
        hierarchical: {
            direction: 'UD',
            //nodeSpacing: 200,
            //blockShifting: false,
            //edgeMinimization: false,
            //sortMethod: "directed"
        }
    }
};


class VisDecomposition extends Component {
  
  constructor(props) {
    super(props);
    this.graph = this.props.graph;
    this.clusterNames = [];
    this.network = {};
    this.appRef = createRef();
    this.loadGraph = this.loadGraph.bind(this);
  }

  componentDidMount() {
    this.loadGraph();
  }

  componentWillReceiveProps(nextProps) {
    this.graph = nextProps.graph;
    this.loadGraph();
  }

  loadGraph() {
    this.clusterNames = [];
    this.graph.clusters.map((cluster) =>
      {this.clusterNames.push({id: cluster.name, label: cluster.name})}
    );
    this.data = {
      nodes: new DataSet(this.clusterNames),
      edges: new DataSet([])
    };
    this.network = new Network(this.appRef.current, this.data, options);
    this.network.on("click", function (params) {
      //params.event = "[original event]";
      console.log('click event, getNodeAt returns: ' + this.getNodeAt(params.pointer.DOM));
    });
  }

  render() {
    return (
      <div>
        <div ref={this.appRef} />
      </div>
    );
  }
}

export default VisDecomposition