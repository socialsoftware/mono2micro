import React from 'react';
import { Button, DropdownButton, Dropdown, Form, FormControl } from 'react-bootstrap';

export const views = {
    NONE: 'View',
    CLUSTERS: 'Clusters View',
    TRANSACTION: 'Transaction View',
    ENTITY: 'Entity View'
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
           <Form inline>
                <DropdownButton 
                    bsStyle='primary'
                    title={this.state.view}
                    id='0'>
                    <Dropdown.Item eventKey="1" onClick={() => this.setView(views.CLUSTERS)}>{views.CLUSTERS}</Dropdown.Item>
                    <Dropdown.Item eventKey="2" onClick={() => this.setView(views.TRANSACTION)}>{views.TRANSACTION}</Dropdown.Item>
                    <Dropdown.Item eventKey="3" onClick={() => this.setView(views.ENTITY)}>{views.ENTITY}</Dropdown.Item>
                </DropdownButton>
            </Form>
       );
    }
}