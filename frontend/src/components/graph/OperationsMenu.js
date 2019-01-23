import React from 'react';
import { Button, DropdownButton, MenuItem, Form, FormControl } from 'react-bootstrap';

export const operations = {
    NONE: 'operation',
    RENAME: 'rename by',
    MERGE: 'merge with',
    SPLIT: 'split by'
};

export class OperationsMenu extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            operation: operations.NONE,
            inputValue: ''
        };

        this.setOperation = this.setOperation.bind(this);
        this.handleInputValueChange = this.handleInputValueChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleClose= this.handleClose.bind(this);
    }

    setOperation(value) {
        this.props.handleSelectOperation(value);
        this.setState({ 
            operation: value,
            inputValue: ''
        });
    }

    handleInputValueChange(event) {
        this.setState({ inputValue: event.target.value });
    }

    handleSubmit() {
        this.props.handleSubmit(this.state.operation, this.state.inputValue);
    }

    handleClose() {
        this.props.handleCancel();
        this.setState({
            inputValue: '',
            operation: operations.NONE,
        });
    }

    render() {
       return (
           <Form inline>
                {this.props.selectedGoal.name && <span>
                <Button id='1'>{this.props.selectedGoal.name}</Button>
                <span> </span>
                <DropdownButton 
                    bsStyle='primary'
                    title={this.state.operation}
                    id='2'>
                    <MenuItem eventKey="1" onClick={() => this.setOperation(operations.RENAME)}>{operations.RENAME}</MenuItem>
                    <MenuItem eventKey="2" onClick={() => this.setOperation(operations.MERGE)}>{operations.MERGE}</MenuItem>
                    <MenuItem eventKey="3" onClick={() => this.setOperation(operations.SPLIT)}>{operations.SPLIT}</MenuItem>
                </DropdownButton></span>}{' '}

                {this.props.mergeWithGoal.name && <span>
                <Button id='3'>{this.props.mergeWithGoal.name}</Button></span>}

                {this.state.operation === operations.SPLIT && this.props.goalConditions &&
                <DropdownButton 
                    bsStyle='default'
                    title={'conditions'}
                    id='4'>
                    {this.props.selectedGoal.type === 'ProductGoal' &&
                    this.props.goalConditions.map(c => <MenuItem 
                        eventKey={c.path} 
                        onSelect={() => this.props.handleSelectCondition(c.path)}
                        active={c.active}>{c.path}</MenuItem>)}
                    {this.props.selectedGoal.type === 'AssociationGoal' &&
                    this.props.goalConditions.map(c => <MenuItem 
                        eventKey={c.name} 
                        onSelect={() => this.props.handleSelectCondition(c.name)}
                        active={c.active}>{c.name}</MenuItem>)}
                </DropdownButton>}            
                
                {(this.state.operation === operations.RENAME || this.props.mergeWithGoal.name || 
                (this.props.goalConditions.reduce((a, c) => c.active ? ++a : a, 0) > 0 && 
                this.props.goalConditions.reduce((a, c) => c.active ? ++a : a, 0) < this.props.goalConditions.length)) &&
                <FormControl
                    id="5"
                    type="text"
                    label="Text"
                    placeholder="name"
                    value={this.state.inputValue}
                    onChange={this.handleInputValueChange}
                />}{' '}
                
                {this.state.inputValue.length > 0 && <span>
                <Button id='6' bsStyle='primary' onClick={this.handleSubmit}>Submit</Button> 
                <span> </span>
                <Button id='7' onClick={this.handleClose}>Cancel</Button> </span>}

            </Form>
       );
    }
}