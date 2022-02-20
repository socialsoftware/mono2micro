import React, {useContext, useState} from 'react';
import InputGroup from 'react-bootstrap/InputGroup';
import Form from 'react-bootstrap/Form';
import ButtonToolbar from 'react-bootstrap/ButtonToolbar';
import Button from 'react-bootstrap/Button';
import ButtonGroup from 'react-bootstrap/ButtonGroup';
import Dropdown from 'react-bootstrap/Dropdown';
import DropdownButton from 'react-bootstrap/DropdownButton';
import Select from 'react-select';
import AppContext from "../AppContext";

export const operations = {
    NONE: 'operation',
    RENAME: 'rename',
    MERGE: 'merge',
    SPLIT: 'split',
    TRANSFER: 'transfer'
};

export const ClusterOperationsMenu = ({selectedCluster, mergeWithCluster, transferToCluster, clusterEntities,
                                          handleSelectOperation, handleSelectEntities, handleSubmit, handleCancel}) => {

    const context = useContext(AppContext);
    const { translateEntity } = context;
    const [operation, setOperation]  = useState(operations.NONE);
    const [inputValue, setInputValue]  = useState('');

    function selectOperation(value) {
        handleSelectOperation(value);
        setOperation(value);
        setInputValue('');
    }

    function handleSubmitOperation() {
        handleSubmit(operation, inputValue);
    }

    function handleInputValueChange(event) {
        setInputValue(event.target.value);
    }

    function handleClose() {
        handleCancel();
        setInputValue('');
        setOperation(operations.NONE);
    }

    function numberOfActiveClusterEntities() {
        return clusterEntities.reduce((a, e) => e.active ? ++a : a, 0);
    }

    const clusterEntitiesName = clusterEntities.map(e => { return {...e, label: translateEntity(e.label) }});

    return (
        <ButtonToolbar>
            {selectedCluster.name &&
                <span>
                    <Button className="me-1">{selectedCluster.name}</Button>
                    <DropdownButton className="me-1" as={ButtonGroup}
                        title={operation}>
                        <Dropdown.Item eventKey="1" onClick={() => selectOperation(operations.RENAME)}>
                            {operations.RENAME}
                        </Dropdown.Item>
                        <Dropdown.Item eventKey="2" onClick={() => selectOperation(operations.MERGE)}>
                            {operations.MERGE}
                        </Dropdown.Item>
                        <Dropdown.Item eventKey="3" onClick={() => selectOperation(operations.SPLIT)}>
                            {operations.SPLIT}
                        </Dropdown.Item>
                        <Dropdown.Item eventKey="4" onClick={() => selectOperation(operations.TRANSFER)}>
                            {operations.TRANSFER}
                        </Dropdown.Item>
                    </DropdownButton>
                </span>
            }

            {
                mergeWithCluster.name &&
                    <Button className="me-1">
                        {mergeWithCluster.name}
                    </Button>
            }

            {
                transferToCluster.name &&
                    <Button className="me-1">
                        {transferToCluster.name}
                    </Button>
            }

            {((operation === operations.SPLIT && clusterEntitiesName) ||
             (operation === operations.TRANSFER && transferToCluster.name)) &&
                <div style={{width: "200px"}}>
                    <Select
                        isMulti
                        name="entities"
                        options={clusterEntitiesName}
                        closeMenuOnSelect={false}
                        onChange={handleSelectEntities}
                        placeholder="entities"
                    />
                </div>
            }

            {(operation === operations.RENAME || mergeWithCluster.name || (clusterEntities &&
                numberOfActiveClusterEntities() > 0 &&
                numberOfActiveClusterEntities() < clusterEntities.length))
                && operation !== operations.TRANSFER &&
                <InputGroup className="me-1">
                    <Form.Control
                        type="text"
                        label="Text"
                        placeholder="name"
                        value={inputValue}
                        onChange={handleInputValueChange}/>
                </InputGroup>
            }

            {(inputValue.length > 0 || (operation === operations.TRANSFER && (clusterEntities &&
                numberOfActiveClusterEntities() > 0 &&
                numberOfActiveClusterEntities() < clusterEntities.length))) &&

                <Button className="me-1" onClick={handleSubmitOperation}>
                    Submit
                </Button>
            }

            <Button onClick={handleClose}>
                Cancel
            </Button>
        </ButtonToolbar>
   );
}