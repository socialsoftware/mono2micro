import React from 'react';
import { Button, DropdownButton, Dropdown, Form, FormControl, ButtonToolbar, ButtonGroup } from 'react-bootstrap';

export class TransactionOperationsMenu extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            showSubmit: false,
            controller: 'Select Controller'
        }

        this.setController = this.setController.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    setController(value) {
        this.setState({
            showSubmit: true,
            controller: value
        });
    }
    
    handleSubmit() {
        this.props.handleControllerSubmit(this.state.controller);
    }

    render() {
        const controllersList = this.props.controllers.map(c =>
            <Dropdown.Item key={c.name} onClick={() => this.setController(c.name)}>{c.name}</Dropdown.Item>
        );
       return (
            <ButtonToolbar>
                <DropdownButton className="mr-1" as={ButtonGroup}
                    title={this.state.controller}>
                    {controllersList}
                </DropdownButton>

                {this.state.showSubmit &&
                    <Button onClick={this.handleSubmit}>Create View</Button>
                }
            </ButtonToolbar>
       );
    }
}