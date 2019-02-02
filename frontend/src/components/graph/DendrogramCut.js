import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import VisDendrogram from './../util/VisDendrogram';
import { DENDROGRAM_URL } from '../../constants/constants';


export class DendrogramCut extends React.Component {
    constructor(props) {
        super(props);
        this.state = { nodes: null, edges: null, loaded: false };

        const service = new RepositoryService();
        service.loadDendrogram().then(response => {
            this.setState({
                nodes: response.data.nodes,
                edges: response.data.edges,
                loaded: true
            });
        });
    }

    render() {
        return (
            <div>
                <h2>Cut Dendrogram</h2>
                {this.state.loaded ? <VisDendrogram nodes={this.state.nodes} edges={this.state.edges} /> : "Loading"}
                <img src={DENDROGRAM_URL} />
            </div>
        );
    };
}