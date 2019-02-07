import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import VisDecomposition from './../util/VisDecomposition';

export class Decomposition extends React.Component {
    constructor(props) {
        super(props);

        this.state = { graphName: this.props.match.params.name, graph: [], loaded: false };

        this.getGraph(this.state.graphName);

        this.getGraph = this.getGraph.bind(this);
    }

    getGraph(name) {
        const service = new RepositoryService();
        service.getGraph(name).then(response => {
            const graph = response.data;
            this.setState({
                graph: graph,
                loaded: true
            });
        });
    }

    componentWillReceiveProps(nextProps) {
        this.setState({ graphName: nextProps.match.params.name });
        this.getGraph(nextProps.match.params.name);
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