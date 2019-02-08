import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import VisDendrogram from './../util/VisDendrogram';
import { DENDROGRAM_URL } from '../../constants/constants';

var HttpStatus = require('http-status-codes');


export class DendrogramCut extends React.Component {
    constructor(props) {
        super(props);
        this.state = { nodes: null, edges: null, loaded: false, cutValue: "" };

        const service = new RepositoryService();
        service.loadDendrogram().then(response => {
            this.setState({
                nodes: response.data.nodes,
                edges: response.data.edges,
                loaded: true
            });
        });

        this.handleCutValueChange = this.handleCutValueChange.bind(this);
        this.handleCutSubmit = this.handleCutSubmit.bind(this);
    }

    handleCutValueChange(event) {
        this.setState({cutValue: event.target.value});
    }
    
    handleCutSubmit(event) {
        event.preventDefault();
        const service = new RepositoryService();
        service.cutDendrogram(this.state.cutValue).then(response => {
            if (response.status === HttpStatus.OK) {
                this.props.location.headerFunction.handleGetGraphsFunction();
                alert("Dendrogram cut successful.");
            } else {
                alert("Failed to cut dendrogram.");
            }
        });
    }

    render() {
        return (
            <div>
                <h2>Cut Dendrogram</h2>
                <form onSubmit={this.handleCutSubmit}>
                    <label>
                    Cut value:
                    <input type="number" value={this.state.cutValue} onChange={this.handleCutValueChange} />
                    </label>
                    <input type="submit" value="Submit" />
                </form>
                {this.state.loaded ? <VisDendrogram nodes={this.state.nodes} edges={this.state.edges} /> : "Loading"}
                <img src={DENDROGRAM_URL + "?" + new Date().getTime()} alt="Dendrogram" />
                
            </div>
        );
    };
}