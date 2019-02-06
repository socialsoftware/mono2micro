import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import VisDecomposition from './../util/VisDecomposition';

export class Decomposition extends React.Component {
    constructor(props) {
        super(props);
        this.state = { graphName: this.props.match.params.name, graph: [], loaded: false };
        const service = new RepositoryService();
        service.getGraph(this.state.graphName).then(response => {
            const graph = response.data;
                this.setState({
                    graph: graph,
                    loaded: true
                });
            console.log(response);
        });
    }

    render() {
        return (
            <div>
                <h5>Graph of {this.state.graphName}</h5>
                {this.state.loaded ? <VisDecomposition graph={this.state.graph} /> : "Loading"}
            </div>
        );
    }
}