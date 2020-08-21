import React from 'react';
import {
    Button,
    ButtonGroup,
    ButtonToolbar,
    Container,
    Dropdown,
    DropdownButton,
    ListGroup,
    Col, FormControl, InputGroup
} from "react-bootstrap";
import Select from 'react-select';

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

export class FunctionalityRedesignMenu extends React.Component{

    constructor(props) {
        super(props);

        this.state = {
            operation: redesignOperations.NONE,
            addCompensatingEntities: [],
            addCompensatingCluster: addCompensatingPlaceHolder,
            showSubmit: false,
            DCGICluster: "",
            selectPivotTransaction: redesignOperations.PIVOT,
            inputValue: ''
        };

        this.handleCancel = this.handleCancel.bind(this);
        this.setOperation = this.setOperation.bind(this);
        this.setCompensatingCluster = this.setCompensatingCluster.bind(this);
        this.handleSelectAddCompensatingEntities = this.handleSelectAddCompensatingEntities.bind(this);
        this.DCGISelectCluster = this.DCGISelectCluster.bind(this);
        this.handleDCGILocalTransactionSelect = this.handleDCGILocalTransactionSelect.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleInputValueChange = this.handleInputValueChange.bind(this);
    }

    setOperation(value){
        this.props.handleSelectOperation(value);

        this.setState({
           operation: value
        });
    }

    setCompensatingCluster(value){
        this.setState({ addCompensatingCluster: value.cluster})
    }

    handleSelectAddCompensatingEntities(value){
        if(value === null || value.length === 0){
            this.setState({
                addCompensatingEntities: value,
                showSubmit: false
            });
        } else {
            const entities = [];
            value.forEach(e => entities.push([e.value,"W"]));
            this.setState({
                addCompensatingEntities: JSON.stringify(entities),
                showSubmit: true
            });
        }
    }

    handleSubmit(){
        let input;
        switch (this.state.operation) {
            case redesignOperations.AC:
                input = {
                    cluster: this.state.addCompensatingCluster,
                    entities: this.state.addCompensatingEntities
                }
                break;

            case redesignOperations.SQ:
                input =  null;
                break;

            case redesignOperations.DCGI:
                break;

            case redesignOperations.PIVOT:
                input = this.state.inputValue;
                break;

            case redesignOperations.RENAME:
                input = this.state.inputValue;
                break;

            default:
                break;
        }
        this.props.handleSubmit(input);
        this.handleCancel();
    }

    handleCancel(){
        this.props.handleCancel();
        this.setState({
            operation: redesignOperations.NONE,
            addCompensatingEntities: [],
            addCompensatingCluster: addCompensatingPlaceHolder,
            showSubmit: false,
            DCGICluster: ""
        });
    }

    showSubmit(){
        if(this.state.showSubmit === true)
            return false;
        else if(this.props.newCaller !== null)
            return false;
        else if(this.props.DCGISelectedLocalTransactions.length !== 0){
            const selectedClustersSet = new Set();
            this.props.DCGISelectedLocalTransactions.forEach(e => selectedClustersSet.add(e.cluster));
            if(selectedClustersSet.size === 2 && this.props.DCGISelectedLocalTransactions.length >= 3){
                return false;
            }
        }
        else if(this.state.operation === redesignOperations.RENAME && this.state.inputValue !== '')
            return false;

        return true;
    }

    DCGISelectCluster(value){
        this.setState({
            DCGICluster: value
        });

        this.props.DCGISelectCluser(value);
    }

    handleDCGILocalTransactionSelect(value){
        this.props.handleDCGISelectLocalTransaction(value);
    }

    handleInputValueChange(event) {
        this.setState({ inputValue: event.target.value });
    }

    render() {

        let modifiedEntitiesClustersDropdown;
        if(this.state.addCompensatingCluster !== addCompensatingPlaceHolder){
            modifiedEntitiesClustersDropdown =
                this.props.modifiedEntities.filter(e => e.cluster === this.state.addCompensatingCluster)[0].modifiedEntities
                    .map((e) => {
                        return {value: e, label: e};
                    });
        }

        let DCGILocalTransactionsDropdown;
        if(this.props.DCGILocalTransactionsForTheSelectedClusters !== null){
            DCGILocalTransactionsDropdown =
                this.props.DCGILocalTransactionsForTheSelectedClusters
                    .filter(e => !this.props.DCGISelectedLocalTransactions.map(entry => entry.id).includes(e.id))
                    .map((e) => {
                        return {value: e.id, label: e.id + ": " + e.cluster};
                    });
        }

        let DCGIAlreadySelectedLocalTransactions = this.props.DCGISelectedLocalTransactions
            .map((e) => {
               return {value: e.id, label: e.id + ": " + e.cluster}
            });

        return (
            <div>
                <ButtonToolbar className="mb-2">
                    <Button className="mr-1">{this.props.selectedLocalTransaction.name}</Button>
                    <DropdownButton className="mr-1" as={ButtonGroup} title={this.state.operation}>
                        <Dropdown.Item eventKey="1" onClick={() => this.setOperation(redesignOperations.SQ)}>{redesignOperations.SQ}</Dropdown.Item>
                        <Dropdown.Item eventKey="2" onClick={() => this.setOperation(redesignOperations.AC)}>{redesignOperations.AC}</Dropdown.Item>
                        <Dropdown.Item eventKey="3" onClick={() => this.setOperation(redesignOperations.DCGI)}>{redesignOperations.DCGI}</Dropdown.Item>
                        <Dropdown.Item eventKey="4" onClick={() => this.setOperation(redesignOperations.PIVOT)}>{redesignOperations.PIVOT}</Dropdown.Item>
                        <Dropdown.Item eventKey="5" onClick={() => this.setOperation(redesignOperations.RENAME)}>{redesignOperations.RENAME}</Dropdown.Item>
                    </DropdownButton>

                    {this.props.newCaller !== null &&
                        <ButtonGroup className="mr-1">
                            <ListGroup>
                                <ListGroup.Item>New caller: {this.props.newCaller.name}</ListGroup.Item>
                            </ListGroup>
                        </ButtonGroup>
                    }

                    {this.props.modifiedEntities !== null &&
                        <DropdownButton className="mr-1" as={ButtonGroup} title={this.state.addCompensatingCluster}>
                            {this.props.modifiedEntities.map(e =>
                                <Dropdown.Item
                                    key={e.cluster}
                                    onSelect={() => this.setCompensatingCluster(e)}>{e.cluster}</Dropdown.Item>)}
                        </DropdownButton>
                    }

                    {this.state.operation === redesignOperations.DCGI &&
                        <Button className="mr-1">{"Cluster 1: " + this.props.selectedLocalTransaction.cluster}</Button>
                    }

                    {this.props.DCGIAvailableClusters !== null &&
                        <DropdownButton className="mr-1" as={ButtonGroup} title={"Cluster 2: " + this.state.DCGICluster}>
                            {this.props.DCGIAvailableClusters.map(e =>
                                <Dropdown.Item
                                    key={e}
                                    onSelect={() => this.DCGISelectCluster(e)}>{e}</Dropdown.Item>)}
                        </DropdownButton>
                    }

                    {this.state.operation === redesignOperations.RENAME &&
                        <InputGroup className="mr-1">
                            <FormControl
                                type="text"
                                label="Text"
                                placeholder="name"
                                value={this.state.inputValue}
                                onChange={this.handleInputValueChange}/>
                        </InputGroup>
                    }
                </ButtonToolbar>

                {this.state.addCompensatingCluster !== addCompensatingPlaceHolder &&
                    <Select
                        isMulti
                        options={modifiedEntitiesClustersDropdown}
                        className="basic-multi-select"
                        classNamePrefix="select"
                        styles={selectStyles}
                        placeholder="Entities"
                        onChange={this.handleSelectAddCompensatingEntities}
                    />
                }

                {this.props.DCGILocalTransactionsForTheSelectedClusters !== null &&
                    <Select
                        value={DCGIAlreadySelectedLocalTransactions}
                        isMulti
                        options={DCGILocalTransactionsDropdown}
                        className="basic-multi-select"
                        classNamePrefix="select"
                        styles={selectStyles}
                        placeholder="Local Transactions"
                        onChange={this.handleDCGILocalTransactionSelect}
                    />
                }

                {this.state.operation === redesignOperations.PIVOT && this.props.selectedRedesign.pivotTransaction === "" &&
                    <InputGroup className="mb-2">
                        <FormControl
                            type="text"
                            label="Text"
                            placeholder="Specify a name for your redesign"
                            value={this.state.inputValue}
                            onChange={this.handleInputValueChange}/>
                    </InputGroup>
                }

                <div>
                    {(this.state.operation === redesignOperations.SQ || this.state.operation === redesignOperations.AC ||
                        this.state.operation === redesignOperations.DCGI || this.state.operation === redesignOperations.RENAME) &&
                        <Button
                            disabled={this.showSubmit()}
                            className="mr-1"
                            onClick={this.handleSubmit}>Submit
                        </Button>
                    }

                    {this.state.operation === redesignOperations.PIVOT &&
                        <Button
                            className="mr-1"
                            variant="warning"
                            onClick={this.handleSubmit}
                            disabled={this.props.selectedRedesign.pivotTransaction === "" && this.state.inputValue === ""}> Finish Redesign
                        </Button>
                    }

                    {this.state.operation !== redesignOperations.NONE  &&
                        <Button className="mr-1" onClick={this.handleCancel}>Cancel</Button>
                    }
                </div>
            </div>
        )
    }
}
