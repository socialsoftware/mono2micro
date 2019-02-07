import React from 'react';
import { GraphDiagram } from './GraphDiagram';

export class Graph extends React.Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (
            <div>
                <GraphDiagram name={this.props.match.params.name} />
            </div>
        );
    }
}