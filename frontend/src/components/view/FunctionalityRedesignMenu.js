import React from 'react';
import {
    Button,
    ButtonGroup,
    ButtonToolbar,
    Dropdown,
    DropdownButton,
    ListGroup
} from "react-bootstrap";
import Select from 'react-select';

export const redesignOperations = {
    NONE: 'operation',
    SQ: 'Sequence Change',
    AC: 'Add Compensating',
    DCGI: 'Define Coarse-Grained Interactions'
};

const selectStyles = {
    control: provided => ({ ...provided, minWidth: 240, marginLeft: 5, marginRight: 5})
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
            DCGICluster: ""
        };

        this.handleCancel = this.handleCancel.bind(this);
        this.setOperation = this.setOperation.bind(this);
        this.setCompensatingCluster = this.setCompensatingCluster.bind(this);
        this.handleSelectAddCompensatingEntities = this.handleSelectAddCompensatingEntities.bind(this);
        this.DCGISelectCluster = this.DCGISelectCluster.bind(this);
        this.handleDCGILocalTransactionSelect = this.handleDCGILocalTransactionSelect.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    setOperation(value){
        this.handleCancel();
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
        console.log("Teste2");
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
        }
        this.props.handleSubmit(input);
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
            if(selectedClustersSet.size === 2){
                return false
            }
        }
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

        if(this.state.a !== null){
            DCGIAlreadySelectedLocalTransactions = this.state.a;
        }

        return (
            <ButtonToolbar>
                <Button className="mr-1">{this.props.selectedLocalTransaction.id + ": " + this.props.selectedLocalTransaction.cluster}</Button>
                <DropdownButton className="mr-1" as={ButtonGroup} title={this.state.operation}>
                    <Dropdown.Item eventKey="1" onClick={() => this.setOperation(redesignOperations.SQ)}>{redesignOperations.SQ}</Dropdown.Item>
                    <Dropdown.Item eventKey="2" onClick={() => this.setOperation(redesignOperations.AC)}>{redesignOperations.AC}</Dropdown.Item>
                    <Dropdown.Item eventKey="3" onClick={() => this.setOperation(redesignOperations.DCGI)}>{redesignOperations.DCGI}</Dropdown.Item>
                </DropdownButton>

                {this.props.newCaller !== null &&
                    <ButtonGroup className="mr-1">
                        <ListGroup>
                            <ListGroup.Item>New caller: {this.props.newCaller.id + ": " + this.props.newCaller.cluster}</ListGroup.Item>
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

                <Button
                    disabled={this.showSubmit()}
                    className="mr-1"
                    onClick={this.handleSubmit}>Submit
                </Button>

                <Button onClick={this.handleCancel}>Cancel</Button>

            </ButtonToolbar>
        )
    }
}
