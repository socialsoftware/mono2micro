import React, {useContext, useEffect, useState} from 'react';
import FormControl from 'react-bootstrap/FormControl';
import ButtonToolbar from 'react-bootstrap/ButtonToolbar';
import Button from 'react-bootstrap/Button';
import ButtonGroup from 'react-bootstrap/ButtonGroup';
import Dropdown from 'react-bootstrap/Dropdown';
import DropdownButton from 'react-bootstrap/DropdownButton';
import AppContext from "./../AppContext";

export const EntityOperationsMenu = ({handleEntitySubmit, entities, amountList}) => {
    const context = useContext(AppContext);

    const [showSubmit, setShowSubmit] = useState(false);
    const [entityList, setEntityList] = useState([]);
    const [entityID, setEntityID] = useState(-1);
    const [entityAmount, setEntityAmount] = useState("All");

    //Executed on mount
    useEffect(() => {
        if (entityAmount === "All") {
            setEntityList(entities.sort());
        }
    }, []);

    function componentWillReceiveProps(nextProps) {
        if (entityAmount === "All") {
            setEntityList(nextProps.entities.sort());
        }
    }

    function changeEntityAmount(value) {
        setEntityAmount(value);
        setShowSubmit(false);
        setEntityID(-1);
        if (value === "All") {
            setEntityList(entities.sort());
        } else {
            setEntityList(entities.filter(e => amountList[e] === value).sort());
        }
    }

    function handleSubmit() {
        handleEntitySubmit(entityID);
    }

    const { translateEntity } = context;

    const entityAmountList = [...new Set(Object.values(amountList))].sort((a, b) => a - b).map(amount =>
        <Dropdown.Item
            key={amount}
            onClick={() => changeEntityAmount(amount)}
        >
            {amount}
        </Dropdown.Item>
    );

    const entitiesListDropdown = entityList.map(entityID =>
        <Dropdown.Item
            key={entityID}
            onClick={() => { setShowSubmit(true); setEntityID(entityID); } }
        >
            {translateEntity(entityID)}
        </Dropdown.Item>
    );

    return (
        <ButtonToolbar>
            <DropdownButton className="me-1" as={ButtonGroup} title={entityAmount}>
                <Dropdown.Item
                    key={"All"}
                    onClick={() => changeEntityAmount("All")}
                >
                    {"All"}
                </Dropdown.Item>
                {entityAmountList}
            </DropdownButton>

            <Dropdown className="me-1" as={ButtonGroup}>
                <Dropdown.Toggle>
                    {
                        entityID === -1
                        ? "Select Entity"
                        : translateEntity(entityID)
                    }
                </Dropdown.Toggle>
                <Dropdown.Menu as={CustomSearchMenuForwardingRef}>
                    {entitiesListDropdown}
                </Dropdown.Menu>
            </Dropdown>
            {
                showSubmit &&
                <Button
                    onClick={handleSubmit}
                >
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