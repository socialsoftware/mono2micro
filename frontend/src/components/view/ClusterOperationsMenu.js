import React from 'react';
import InputGroup from 'react-bootstrap/InputGroup';
import FormControl from 'react-bootstrap/FormControl';
import ButtonToolbar from 'react-bootstrap/ButtonToolbar';
import Button from 'react-bootstrap/Button';
import ButtonGroup from 'react-bootstrap/ButtonGroup';
import Dropdown from 'react-bootstrap/Dropdown';
import DropdownButton from 'react-bootstrap/DropdownButton';


import Select from 'react-select';

export const operations = {
    NONE: 'operation',
    RENAME: 'rename',
    MERGE: 'merge',
    SPLIT: 'split',
    TRANSFER: 'transfer'
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
        const {
            mergeWithCluster,
            transferToCluster,
            clusterEntities,
            handleSelectEntities,
            selectedCluster,
        } = this.props;

        const {
            operation,
            inputValue,
        } = this.state;

        console.log(clusterEntities);

        const numberOfActiveClusterEntities = clusterEntities.reduce((a, e) => e.active ? ++a : a, 0);

        return (
            <ButtonToolbar>
                {selectedCluster.name && 
                    <span>
                        <Button className="mr-1">{selectedCluster.name}</Button>
                        <DropdownButton className="mr-1" as={ButtonGroup}
                            title={operation}>
                            <Dropdown.Item eventKey="1" onClick={() => this.setOperation(operations.RENAME)}>
                                {operations.RENAME}
                            </Dropdown.Item>
                            <Dropdown.Item eventKey="2" onClick={() => this.setOperation(operations.MERGE)}>
                                {operations.MERGE}
                            </Dropdown.Item>
                            <Dropdown.Item eventKey="3" onClick={() => this.setOperation(operations.SPLIT)}>
                                {operations.SPLIT}
                            </Dropdown.Item>
                            <Dropdown.Item eventKey="4" onClick={() => this.setOperation(operations.TRANSFER)}>
                                {operations.TRANSFER}
                            </Dropdown.Item>
                        </DropdownButton>
                    </span>
                }

                {
                    mergeWithCluster.name &&
                        <Button className="mr-1">
                            {mergeWithCluster.name}
                        </Button>
                }

                {
                    transferToCluster.name &&
                        <Button className="mr-1">
                            {transferToCluster.name}
                        </Button>
                }

                {((operation === operations.SPLIT && clusterEntities) ||
                 (operation === operations.TRANSFER && transferToCluster.name)) &&
                    <div style={{width: "200px"}}>
                        <Select
                            isMulti
                            name="entities"
                            options={clusterEntities}
                            closeMenuOnSelect={false}
                            onChange={handleSelectEntities}
                            placeholder="entities"
                        />
                    </div>
                }
                
                {(operation === operations.RENAME || mergeWithCluster.name || (clusterEntities &&
                    numberOfActiveClusterEntities > 0 && 
                    numberOfActiveClusterEntities < clusterEntities.length))
                    && operation !== operations.TRANSFER &&
                    <InputGroup className="mr-1">
                        <FormControl
                            type="text"
                            label="Text"
                            placeholder="name"
                            value={inputValue}
                            onChange={this.handleInputValueChange}/>
                    </InputGroup>
                }
                
                {(inputValue.length > 0 || (operation === operations.TRANSFER && (clusterEntities &&
                    numberOfActiveClusterEntities > 0 && 
                    numberOfActiveClusterEntities < clusterEntities.length))) &&
                    
                    <Button className="mr-1" onClick={this.handleSubmit}>
                        Submit
                    </Button>
                }

                <Button onClick={this.handleClose}>
                    Cancel
                </Button>
            </ButtonToolbar>
       );
    }
}