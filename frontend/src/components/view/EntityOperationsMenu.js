import React from 'react';
import FormControl from 'react-bootstrap/FormControl';
import ButtonToolbar from 'react-bootstrap/ButtonToolbar';
import Button from 'react-bootstrap/Button';
import ButtonGroup from 'react-bootstrap/ButtonGroup';
import Dropdown from 'react-bootstrap/Dropdown';
import DropdownButton from 'react-bootstrap/DropdownButton';

export class EntityOperationsMenu extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            showSubmit: false,
            entityList: this.props.entities.sort(),
            entity: 'Select Entity',
            entityAmount: "All"
        }
        this.setEntity = this.setEntity.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    componentDidMount() {
        if (this.state.entityAmount === "All") {
            this.setState({
                entityList: this.props.entities.sort()
            });
        }
    }

    componentWillReceiveProps(nextProps) {
        if (this.state.entityAmount === "All") {
            this.setState({
                entityList: nextProps.entities.sort()
            });
        }
    }

    setEntity(value) {
        this.setState({
            showSubmit: true,
            entity: value
        });
    }

    setEntityAmount(value) {
        this.setState({
            entityAmount: value,
            showSubmit: false,
            entity: "Select Entity"
        });
        if (value === "All") {
            this.setState({
                entityList: this.props.entities.map(e => e.name).sort()
            });
        } else {
            this.setState({
                entityList: this.props.entities.map(e => e.name).filter(e => this.props.amountList[e] === value).sort()
            });
        }
    }

    handleSubmit() {
        this.props.handleEntitySubmit(this.state.entity);
    }


    render() {
        const {
            entity,
            entityAmount,
            entityList,
            showSubmit,
        } = this.state;

        const entityAmountList = [...new Set(Object.values(this.props.amountList))].sort((a, b) => a - b).map(amount =>
            <Dropdown.Item
                key={amount}
                onClick={() => this.setEntityAmount(amount)}
            >
                {amount}
            </Dropdown.Item>
        );

        const entitiesListDropdown = entityList.map(e =>
            <Dropdown.Item
                key={e}
                onClick={() => this.setEntity(e)}
            >
                {e}
            </Dropdown.Item>
        );
        return (
            <ButtonToolbar>
                <DropdownButton className="mr-1" as={ButtonGroup} title={entityAmount}>
                    <Dropdown.Item
                        key={"All"}
                        onClick={() => this.setEntityAmount("All")}
                    >
                        {"All"}
                    </Dropdown.Item>
                    {entityAmountList}
                </DropdownButton>

                <Dropdown className="mr-1" as={ButtonGroup}>
                    <Dropdown.Toggle>
                        {entity}
                    </Dropdown.Toggle>
                    <Dropdown.Menu as={CustomSearchMenuForwardingRef}>
                        {entitiesListDropdown}
                    </Dropdown.Menu>
                </Dropdown>
                {
                    showSubmit && 
                    <Button
                        onClick={this.handleSubmit}
                    >
                        Create View
                    </Button>
                }
            </ButtonToolbar>
        );
    }
}

class CustomSearchMenu extends React.Component {
    constructor(props, context) {
        super(props, context);

        this.handleChange = this.handleChange.bind(this);

        this.state = { value: '' };
    }

    handleChange(e) {
        this.setState({ value: e.target.value.toLowerCase().trim() });
    }

    render() {
        const {
            children,
            style,
            className,
            'aria-labelledby': labeledBy,
        } = this.props;

        const { value } = this.state;
        return (
            <div style={style} className={className} aria-labelledby={labeledBy} ref={this.props.innerRef}>
                <FormControl
                    autoFocus
                    className="mx-3 my-2 w-auto"
                    placeholder="Type to filter..."
                    onChange={this.handleChange}
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

}

const CustomSearchMenuForwardingRef = React.forwardRef((props, ref) => (
    <CustomSearchMenu {...props} innerRef={ref}/>
));