import React from 'react';
import { Button, DropdownButton, Dropdown, Form, FormControl, ButtonGroup, ButtonToolbar, InputGroup} from 'react-bootstrap';

export const operations = {
    NONE: 'operation',
    RENAME: 'rename by',
    MERGE: 'merge with',
    SPLIT: 'split by'
};

export class ClusterOperationsMenu extends React.Component {
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
            <ButtonToolbar>
                {this.props.selectedCluster.name && <span>
            
                <Button className="mr-1">{this.props.selectedCluster.name}</Button>
            
                <DropdownButton className="mr-1" as={ButtonGroup}
                    title={this.state.operation}>
                    <Dropdown.Item eventKey="1" onClick={() => this.setOperation(operations.RENAME)}>{operations.RENAME}</Dropdown.Item>
                    <Dropdown.Item eventKey="2" onClick={() => this.setOperation(operations.MERGE)}>{operations.MERGE}</Dropdown.Item>
                    <Dropdown.Item eventKey="3" onClick={() => this.setOperation(operations.SPLIT)}>{operations.SPLIT}</Dropdown.Item>
                </DropdownButton></span>}

                {this.props.mergeWithCluster.name &&
                <Button className="mr-1">{this.props.mergeWithCluster.name}</Button>}

                {this.state.operation === operations.SPLIT && this.props.clusterEntities &&
                <DropdownButton className="mr-1" as={ButtonGroup}
                    title={'entities'}>
                    {this.props.clusterEntities.map(e => <Dropdown.Item 
                        key={e.name}
                        eventKey={e.name} 
                        onSelect={() => this.props.handleSelectEntity(e.name)}
                        active={e.active}>{e.name}</Dropdown.Item>)}
                </DropdownButton>} 
                
                {(this.state.operation === operations.RENAME || this.props.mergeWithCluster.name || 
                (this.props.clusterEntities &&
                    this.props.clusterEntities.reduce((a, e) => e.active ? ++a : a, 0) > 0 && 
                    this.props.clusterEntities.reduce((a, e) => e.active ? ++a : a, 0) < this.props.clusterEntities.length))
                &&
                <InputGroup className="mr-1">
                    <FormControl
                        type="text"
                        label="Text"
                        placeholder="name"
                        value={this.state.inputValue}
                        onChange={this.handleInputValueChange}/>
                </InputGroup>}
                
                {this.state.inputValue.length > 0 &&
                <Button className="mr-1" onClick={this.handleSubmit}>Submit</Button>}

                <Button onClick={this.handleClose}>Cancel</Button>
            </ButtonToolbar>
       );
    }
}