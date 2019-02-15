import React from 'react';
import { ClusterView } from './ClusterView';
import { ViewsMenu, views } from './ViewsMenu';
import { TransactionView } from './TransactionView';

export class Views extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            view: views.CLUSTERS
        }

        this.handleSelectView = this.handleSelectView.bind(this);
    }

    handleSelectView(value) {
        this.setState({
            view: value
        });
    }

    render() {
        return (
            <div>
                <ViewsMenu
                    handleSelectView={this.handleSelectView}
                />
                {this.state.view === views.CLUSTERS &&
                    <ClusterView name={this.props.match.params.name} />
                }
                {this.state.view === views.TRANSACTION &&
                    <TransactionView name={this.props.match.params.name} />
                }
            </div>
        );
    }
}