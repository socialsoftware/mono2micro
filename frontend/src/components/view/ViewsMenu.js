import React from 'react';
import { DropdownButton, Dropdown } from 'react-bootstrap';

export const views = {
    NONE: 'View',
    CLUSTERS: 'Clusters View',
    TRANSACTION: 'Transaction View',
    ENTITY: 'Entity View'
};

export const types = {
    CLUSTER: 0,
    CONTROLLER: 1,
    ENTITY: 2
};

export class ViewsMenu extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            view: views.CLUSTERS
        }

        this.setView = this.setView.bind(this);
    }

    setView(value) {
        this.props.handleSelectView(value);
        this.setState({
            view: value
        });
    }

    render() {
       return (
                <DropdownButton className="mb-2"
                    title={this.state.view}>
                    <Dropdown.Item eventKey="1" onClick={() => this.setView(views.CLUSTERS)}>{views.CLUSTERS}</Dropdown.Item>
                    <Dropdown.Item eventKey="2" onClick={() => this.setView(views.TRANSACTION)}>{views.TRANSACTION}</Dropdown.Item>
                    <Dropdown.Item eventKey="3" onClick={() => this.setView(views.ENTITY)}>{views.ENTITY}</Dropdown.Item>
                </DropdownButton>
       );
    }
}