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
                {this.props.selectedCluster.name && <span>
                <Button id='1'>{this.props.selectedCluster.name}</Button>
                <span> </span>
                <DropdownButton 
                    bsStyle='primary'
                    title={this.state.operation}
                    id='2'>
                    <MenuItem eventKey="1" onClick={() => this.setOperation(operations.RENAME)}>{operations.RENAME}</MenuItem>
                    <MenuItem eventKey="2" onClick={() => this.setOperation(operations.MERGE)}>{operations.MERGE}</MenuItem>
                    <MenuItem eventKey="3" onClick={() => this.setOperation(operations.SPLIT)}>{operations.SPLIT}</MenuItem>
                </DropdownButton></span>}{' '}

                {this.props.mergeWithCluster.name && <span>
                <Button id='3'>{this.props.mergeWithCluster.name}</Button></span>}

                {this.state.operation === operations.SPLIT && this.props.clusterEntities &&
                <DropdownButton 
                    bsStyle='default'
                    title={'entities'}
                    id='4'>
                    {this.props.clusterEntities.map(e => <MenuItem 
                        key={e.name}
                        eventKey={e.name} 
                        onSelect={() => this.props.handleSelectEntity(e.name)}
                        active={e.active}>{e.name}</MenuItem>)}
                </DropdownButton>} 
                
                {(this.state.operation === operations.RENAME || this.props.mergeWithCluster.name || 
                 (this.props.clusterEntities &&
                    this.props.clusterEntities.reduce((a, e) => e.active ? ++a : a, 0) > 0 && 
                    this.props.clusterEntities.reduce((a, e) => e.active ? ++a : a, 0) < this.props.clusterEntities.length))
                 &&
                <FormControl
                    id="5"
                    type="text"
                    label="Text"
                    placeholder="name"
                    value={this.state.inputValue}
                    onChange={this.handleInputValueChange}
                />}{' '}
                
                {this.state.inputValue.length > 0 &&
                <Button id='6' bsStyle='primary' onClick={this.handleSubmit}>Submit</Button>}

                <Button id='7' onClick={this.handleClose}>Cancel</Button>
            </Form>
       );
    }
}