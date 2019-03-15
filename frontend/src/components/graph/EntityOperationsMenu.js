import React from 'react';
import { Button, DropdownButton, Dropdown, FormControl, ButtonToolbar, ButtonGroup } from 'react-bootstrap';

export class EntityOperationsMenu extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            showSubmit: false,
            entityList: this.props.entities.map(e => e.name).sort(),
            entity: 'Select Entity',
            entityAmount: "All"
        }

        this.setEntity = this.setEntity.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    componentWillReceiveProps(nextProps) {
      if (this.state.entityAmount === "All") {
        this.setState({
          entityList: nextProps.entities.map(e => e.name).sort()
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
          entityList: this.props.entities.filter(e => this.props.amountList[e.name] === value).map(e => e.name).sort()
        });
      }
    }

    handleSubmit() {
        this.props.handleEntitySubmit(this.state.entity);
    }


    render() {
      const entityAmountList = [...new Set(Object.values(this.props.amountList))].sort((a, b) => a - b).map(amount =>
        <Dropdown.Item key={amount} onClick={() => this.setEntityAmount(amount)}>{amount}</Dropdown.Item>  
      );

      const entitiesListDropdown = this.state.entityList.map(e =>
          <Dropdown.Item key={e} onClick={() => this.setEntity(e)}>{e}</Dropdown.Item>
      );

      return (
          <ButtonToolbar>
              <DropdownButton className="mr-1" as={ButtonGroup} title={'Exclude Controllers'}>
                {this.props.controllers.map(c => <Dropdown.Item 
                    key={c.name}
                    eventKey={c.name}
                    onSelect={() => this.props.handleExcludeController(c.name)}
                    active={this.props.excludeControllerList.includes(c.name)}>{c.name}</Dropdown.Item>)}
              </DropdownButton>

              <DropdownButton className="mr-1" as={ButtonGroup} title={this.state.entityAmount}>
                <Dropdown.Item key={"All"} onClick={() => this.setEntityAmount("All")}>{"All"}</Dropdown.Item>
                {entityAmountList}
              </DropdownButton>

              <Dropdown className="mr-1" as={ButtonGroup}>
                <Dropdown.Toggle>{this.state.entity}</Dropdown.Toggle>
                <Dropdown.Menu as={CustomSearchMenu}>
                  {entitiesListDropdown}
                </Dropdown.Menu>
              </Dropdown>

              {this.state.showSubmit &&
                  <Button onClick={this.handleSubmit}>Create View</Button>
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
        <div style={style} className={className} aria-labelledby={labeledBy}>
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
                !value || child.props.children.toLowerCase().startsWith(value),
            )}
          </ul>
        </div>
      );
    }
}