import React, {useEffect, useState} from 'react';
import FormControl from 'react-bootstrap/FormControl';
import ButtonToolbar from 'react-bootstrap/ButtonToolbar';
import Button from 'react-bootstrap/Button';
import ButtonGroup from 'react-bootstrap/ButtonGroup';
import Dropdown from 'react-bootstrap/Dropdown';
import DropdownButton from 'react-bootstrap/DropdownButton';

export const TransactionOperationsMenu = ({handleControllerSubmit, controllersClusters}) => {
    const [showSubmit, setShowSubmit] = useState(false);
    const [controllerList, setControllerList] = useState([]);
    const [controller, setController] = useState('Select Controller');
    const [controllerAmount, setControllerAmount] = useState("All");

    useEffect(() => onMount(), []);

    function onMount() {
        if (controllerAmount === "All") {
            setControllerList(Object.keys(controllersClusters).sort());
        }
    }

    function componentWillReceiveProps(nextProps) {
        if (controllerAmount === "All") {
            setControllerList(Object.keys(nextProps.controllersClusters).sort());
        }
    }

    function changeControllerAmount(value) {
        setControllerAmount(value);
        setShowSubmit(false);
        setController("Select Controller");
        if (value === "All")
            setControllerList(Object.keys(controllersClusters).sort());
        else
            setControllerList(Object.keys(controllersClusters).filter(key => controllersClusters[key].length === value).sort());
    }

    function handleSubmit() {
        handleControllerSubmit(controller);
    }


    const controllerAmountList = [...new Set(Object.keys(controllersClusters).map(key => controllersClusters[key].length))].sort((a, b) => a - b).map(amount =>
        <Dropdown.Item key={amount} onClick={() => changeControllerAmount(amount)}>
            {amount}
        </Dropdown.Item>
    );

    const controllersListDropdown = controllerList.map(c =>
        <Dropdown.Item key={c} onClick={() => {setShowSubmit(true); setController(c);}}>
            {c}
        </Dropdown.Item>
    );

    return (
        <ButtonToolbar>
            <DropdownButton className="me-1" as={ButtonGroup} title={controllerAmount}>
                <Dropdown.Item key={"All"} onClick={() => changeControllerAmount("All")}>
                    {"All"}
                </Dropdown.Item>
                {controllerAmountList}
            </DropdownButton>

            <Dropdown className="me-1" as={ButtonGroup}>
                <Dropdown.Toggle>
                    {controller}
                </Dropdown.Toggle>
                <Dropdown.Menu as={CustomSearchMenuForwardingRef}>
                    {controllersListDropdown}
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