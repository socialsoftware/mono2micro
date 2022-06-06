import React, {useEffect, useState} from 'react';
import FormControl from 'react-bootstrap/FormControl';
import ButtonToolbar from 'react-bootstrap/ButtonToolbar';
import Button from 'react-bootstrap/Button';
import ButtonGroup from 'react-bootstrap/ButtonGroup';
import Dropdown from 'react-bootstrap/Dropdown';
import DropdownButton from 'react-bootstrap/DropdownButton';

export const TransactionOperationsMenu = ({handleFunctionalitySubmit, functionalitiesClusters}) => {
    const [showSubmit, setShowSubmit] = useState(false);
    const [functionalityList, setFunctionalityList] = useState([]);
    const [functionality, setFunctionality] = useState('Select Functionality');
    const [functionalityAmount, setFunctionalityAmount] = useState("All");

    useEffect(() => onMount(), []);

    function onMount() {
        if (functionalityAmount === "All") {
            setFunctionalityList(Object.keys(functionalitiesClusters).sort());
        }
    }

    function componentWillReceiveProps(nextProps) {
        if (functionalityAmount === "All") {
            setFunctionalityList(Object.keys(nextProps.functionalitiesClusters).sort());
        }
    }

    function changeFunctionalityAmount(value) {
        setFunctionalityAmount(value);
        setShowSubmit(false);
        setFunctionality("Select Functionality");
        if (value === "All")
            setFunctionalityList(Object.keys(functionalitiesClusters).sort());
        else
            setFunctionalityList(Object.keys(functionalitiesClusters).filter(key => functionalitiesClusters[key].length === value).sort());
    }

    function handleSubmit() {
        handleFunctionalitySubmit(functionality);
    }


    const functionalityAmountList = [...new Set(Object.keys(functionalitiesClusters).map(key => functionalitiesClusters[key].length))].sort((a, b) => a - b).map(amount =>
        <Dropdown.Item key={amount} onClick={() => changeFunctionalityAmount(amount)}>
            {amount}
        </Dropdown.Item>
    );

    const functionalitiesListDropdown = functionalityList.map(c =>
        <Dropdown.Item key={c} onClick={() => {setShowSubmit(true); setFunctionality(c);}}>
            {c}
        </Dropdown.Item>
    );

    return (
        <ButtonToolbar style={{zIndex: 2, position: "absolute", left: "1.3rem", top: "13.3rem"}}>
            <DropdownButton className="me-1" as={ButtonGroup} title={functionalityAmount}>
                <Dropdown.Item key={"All"} onClick={() => changeFunctionalityAmount("All")}>
                    {"All"}
                </Dropdown.Item>
                {functionalityAmountList}
            </DropdownButton>

            <Dropdown className="me-1" as={ButtonGroup}>
                <Dropdown.Toggle>
                    {functionality}
                </Dropdown.Toggle>
                <Dropdown.Menu as={CustomSearchMenuForwardingRef}>
                    {functionalitiesListDropdown}
                </Dropdown.Menu>
            </Dropdown>

            {showSubmit &&
            <Button onClick={handleSubmit}>
                Create View
            </Button>
            }
        </ButtonToolbar>
    );
}

const CustomSearchMenu = ({ children, style, className, 'aria-labelledby': labeledBy, innerRef }) => {
    const [value, setValue] = useState('');

    function handleChange(e) {
        setValue(e.target.value.toLowerCase().trim());
    }

    return (
        <div style={style} className={className} aria-labelledby={labeledBy} ref={innerRef}>
            <FormControl
                autoFocus
                className="mx-3 my-2 w-auto"
                placeholder="Type to filter..."
                onChange={handleChange}
                value={value}
            />
            <ul className="list-unstyled">
                {React.Children.toArray(children).filter(
                    child =>
                        !value || child.props.children.toLowerCase().includes(value),
                )}
            </ul>
        </div>
    );
}

const CustomSearchMenuForwardingRef = React.forwardRef((props, ref) => (
    <CustomSearchMenu {...props} innerRef={ref}/>
));