import React from 'react';
import { Button, DropdownButton, MenuItem, Form, FormControl } from 'react-bootstrap';

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
            <MenuItem key={c.name} onClick={() => this.setController(c.name)}>{c.name}</MenuItem>
        );
       return (
           <Form inline>
                <br />
                <DropdownButton 
                    bsStyle='primary'
                    title={this.state.controller}
                    id='1'>
                    {controllersList}
                </DropdownButton>
                <span> </span>
                {this.state.showSubmit &&
                    <Button id='2' bsStyle='primary' onClick={this.handleSubmit}>Create View</Button>
                }
            </Form>
       );
    }
}