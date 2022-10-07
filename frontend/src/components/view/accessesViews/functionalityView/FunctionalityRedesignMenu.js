import React, {useContext, useState} from 'react';
import {
    Button,
    ButtonGroup,
    ButtonToolbar,
    Dropdown,
    DropdownButton,
    ListGroup,
    FormControl,
    InputGroup
} from "react-bootstrap";
import Select from 'react-select';
import AppContext from "../../../AppContext";

export const redesignOperations = {
    NONE: 'operation',
    SQ: 'Sequence Change',
    AC: 'Add Compensating',
    DCGI: 'Define Coarse-Grained Interactions',
    PIVOT: 'Select the Pivot Transaction',
    RENAME: 'Rename'
};

const selectStyles = {
    control: provided => ({ ...provided, marginTop: 7, marginBottom: 7})
};

const addCompensatingPlaceHolder = "Cluster";

export const FunctionalityRedesignMenu = ({
    selectedRedesign,
    selectedLocalTransaction,
    newCaller,
    modifiedEntities,
    DCGIAvailableClusters,
    DCGILocalTransactionsForTheSelectedClusters,
    DCGISelectedLocalTransactions,
    handleSelectOperation,
    handleCancel,
    handleSubmit,
    DCGISelectCluster,
    handleDCGISelectLocalTransaction}) => {

    const context = useContext(AppContext);
    const { translateEntity } = context;
    const [operation, setOperation] = useState(redesignOperations.NONE);
    const [addCompensatingEntities, setAddCompensatingEntities] = useState([]);
    const [addCompensatingCluster, setAddCompensatingCluster] = useState(addCompensatingPlaceHolder);
    const [showSubmit, setShowSubmit] = useState(false);
    const [DCGICluster, setDCGICluster] = useState("");
    const [inputValue, setInputValue] = useState('');

    function changeOperation(value){
        handleSelectOperation(value);

        setOperation(value);
    }

    function changeCompensatingCluster(value){
        setAddCompensatingCluster(value.cluster);
    }

    function handleSelectAddCompensatingEntities(value){
        if(value === null || value.length === 0){
            setAddCompensatingEntities(value);
            setShowSubmit(false);
        } else {
            const entities = [];
            value.forEach(e => entities.push(parseInt(e.value)));
            setAddCompensatingEntities(entities);
            setShowSubmit(true);
        }
    }

    function handleFunctionalitySubmit(){
        let input;
        switch (operation) {
            case redesignOperations.AC:
                input = {
                    cluster: addCompensatingCluster,
                    entities: addCompensatingEntities
                }
                break;

            case redesignOperations.SQ:
                input =  null;
                break;

            case redesignOperations.DCGI:
                break;

            case redesignOperations.PIVOT:
                input = inputValue;
                break;

            case redesignOperations.RENAME:
                input = inputValue;
                break;

            default:
                break;
        }
        handleSubmit(input);
        handleFunctionalityCancel();
    }

    function handleFunctionalityCancel(){
        handleCancel();
        setOperation(redesignOperations.NONE);
        setAddCompensatingEntities([]);
        setAddCompensatingCluster(addCompensatingPlaceHolder);
        setShowSubmit(false);
        setDCGICluster("");
    }

    function disableShowSubmit(){
        if(showSubmit === true)
            return false;
        else if(newCaller !== null)
            return false;
        else if(DCGISelectedLocalTransactions.length !== 0){
            const selectedClustersSet = new Set();
            DCGISelectedLocalTransactions.forEach(e => selectedClustersSet.add(e.clusterName));
            if(selectedClustersSet.size === 2 && DCGISelectedLocalTransactions.length >= 3){
                return false;
            }
        }
        else if(operation === redesignOperations.RENAME && inputValue !== '')
            return false;

        return true;
    }

    function selectDCGISelectCluster(value){
        setDCGICluster(value);

        DCGISelectCluster(value);
    }

    function handleDCGILocalTransactionSelect(value){
        handleDCGISelectLocalTransaction(value);
    }

    function handleInputValueChange(event) {
        setInputValue(event.target.value);
    }



    let modifiedEntitiesClustersDropdown;
    if(addCompensatingCluster !== addCompensatingPlaceHolder){
        modifiedEntitiesClustersDropdown =
            modifiedEntities.find(e => e.cluster === addCompensatingCluster).modifiedEntities
                .map((e) => {
                    return {value: e, label: translateEntity(e)};
                });
    }

    let DCGILocalTransactionsDropdown;
    if(DCGILocalTransactionsForTheSelectedClusters !== null){
        DCGILocalTransactionsDropdown =
            DCGILocalTransactionsForTheSelectedClusters
                .filter(e => !DCGISelectedLocalTransactions.map(entry => entry.id).includes(e.id))
                .map((e) => {
                    return {value: e.id, label: e.name};
                });
    }

    let DCGIAlreadySelectedLocalTransactions = DCGISelectedLocalTransactions
        .map((e) => {
            return {value: e.id, label: e.name}
        });

    const operationsToShow = selectedLocalTransaction.id === (-1).toString();

    return (
        <div>
            <ButtonToolbar className="mb-2">
                <Button className="me-1">{selectedLocalTransaction.name}</Button>
                <DropdownButton className="me-1" as={ButtonGroup} title={operation}>
                    <Dropdown.Item eventKey="1" disabled = {operationsToShow} onClick={() => changeOperation(redesignOperations.SQ)}>{redesignOperations.SQ}</Dropdown.Item>
                    <Dropdown.Item eventKey="2" disabled = {operationsToShow} onClick={() => changeOperation(redesignOperations.AC)}>{redesignOperations.AC}</Dropdown.Item>
                    <Dropdown.Item eventKey="3" disabled = {operationsToShow} onClick={() => changeOperation(redesignOperations.DCGI)}>{redesignOperations.DCGI}</Dropdown.Item>
                    <Dropdown.Item eventKey="4" onClick={() => changeOperation(redesignOperations.PIVOT)}>{redesignOperations.PIVOT}</Dropdown.Item>
                    <Dropdown.Item eventKey="5" disabled = {operationsToShow} onClick={() => changeOperation(redesignOperations.RENAME)}>{redesignOperations.RENAME}</Dropdown.Item>
                </DropdownButton>

                {newCaller !== null &&
                <ButtonGroup className="me-1">
                    <ListGroup>
                        <ListGroup.Item>New caller: {newCaller.name}</ListGroup.Item>
                    </ListGroup>
                </ButtonGroup>
                }

                {modifiedEntities !== null &&
                <DropdownButton className="me-1" as={ButtonGroup} title={addCompensatingCluster}>
                    {modifiedEntities.map(e =>
                        <Dropdown.Item
                            key={e.cluster}
                            onClick={() => changeCompensatingCluster(e)}>{e.cluster}</Dropdown.Item>)}
                </DropdownButton>
                }

                {operation === redesignOperations.DCGI &&
                <Button className="me-1">{"From Cluster: " + selectedLocalTransaction.clusterName}</Button>
                }

                {DCGIAvailableClusters !== null &&
                <DropdownButton className="me-1" as={ButtonGroup} title={"To Cluster: " + DCGICluster}>
                    {DCGIAvailableClusters.map(e =>
                        <Dropdown.Item
                            key={e}
                            onClick={() => selectDCGISelectCluster(e)}>{e}</Dropdown.Item>)}
                </DropdownButton>
                }

                {operation === redesignOperations.RENAME &&
                <InputGroup className="me-1">
                    <FormControl
                        type="text"
                        label="Text"
                        placeholder="name"
                        value={inputValue}
                        onChange={handleInputValueChange}/>
                </InputGroup>
                }
            </ButtonToolbar>

            {addCompensatingCluster !== addCompensatingPlaceHolder &&
            <Select
                isMulti
                options={modifiedEntitiesClustersDropdown}
                className="basic-multi-select"
                classNamePrefix="select"
                styles={selectStyles}
                placeholder="Entities"
                onChange={handleSelectAddCompensatingEntities}
            />
            }

            {DCGILocalTransactionsForTheSelectedClusters !== null &&
            <Select
                value={DCGIAlreadySelectedLocalTransactions}
                isMulti
                options={DCGILocalTransactionsDropdown}
                className="basic-multi-select"
                classNamePrefix="select"
                styles={selectStyles}
                placeholder="Local Transactions"
                onChange={handleDCGILocalTransactionSelect}
            />
            }

            {operation === redesignOperations.PIVOT && selectedRedesign.pivotTransaction === -1 &&
            <InputGroup className="mb-2">
                <FormControl
                    type="text"
                    label="Text"
                    placeholder="Specify a name for your redesign"
                    value={inputValue}
                    onChange={handleInputValueChange}/>
            </InputGroup>
            }

            <div>
                {(operation === redesignOperations.SQ || operation === redesignOperations.AC ||
                    operation === redesignOperations.DCGI || operation === redesignOperations.RENAME) &&
                <Button
                    disabled={disableShowSubmit()}
                    className="me-1"
                    onClick={handleFunctionalitySubmit}>Submit
                </Button>
                }

                {operation === redesignOperations.PIVOT &&
                <Button
                    className="me-1"
                    variant="warning"
                    onClick={handleFunctionalitySubmit}
                    disabled={selectedRedesign.pivotTransaction === "" && inputValue === ""}> Finish Redesign
                </Button>
                }

                {operation !== redesignOperations.NONE  &&
                <Button className="me-1" onClick={handleFunctionalityCancel}>Cancel</Button>
                }
            </div>
        </div>
    )
}
