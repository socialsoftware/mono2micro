import React from 'react';
import { GraphDiagram } from './GraphDiagram';

export class Graph extends React.Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (
            <div>
                <h5>Graph of {this.props.match.params.name}</h5>
                <GraphDiagram spec={this.props.match.params.name} />
            </div>
        );
    }
}