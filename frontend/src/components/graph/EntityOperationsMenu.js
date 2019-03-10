import React from 'react';
import { Button, DropdownButton, Dropdown, FormControl, ButtonToolbar, ButtonGroup } from 'react-bootstrap';

export class EntityOperationsMenu extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            showSubmit: false,
            controllerList: [],
            controller: 'Select Controller',
            controllerAmount: "All"
        }

        this.setController = this.setController.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    componentWillReceiveProps(nextProps) {
      if (this.state.controllerAmount === "All") {
        this.setState({
          controllerList: Object.keys(nextProps.controllerClusters).sort()
        });
      }
    }

    setController(value) {
        this.setState({
            showSubmit: true,
            controller: value
        });
    }

    setControllerAmount(value) {
      this.setState({
        controllerAmount: value,
        showSubmit: false,
        controller: "Select Controller"
      });
      if (value === "All") {
        this.setState({
          controllerList: Object.keys(this.props.controllerClusters).sort()
        });
      } else {
        this.setState({
          controllerList: Object.keys(this.props.controllerClusters).filter(key => this.props.controllerClusters[key].length === value).sort()
        });
      }
    }
    
    handleSubmit() {
        this.props.handleControllerSubmit(this.state.controller);
    }

    render() {
      const controllerAmountList = [...new Set(Object.keys(this.props.controllerClusters).map(key => this.props.controllerClusters[key].length))].sort().map(amount =>
        <Dropdown.Item key={amount} onClick={() => this.setControllerAmount(amount)}>{amount}</Dropdown.Item>  
      );
        
      const controllersListDropdown = this.state.controllerList.map(c =>
          <Dropdown.Item key={c} onClick={() => this.setController(c)}>{c}</Dropdown.Item>
      );

      return (
          <ButtonToolbar>
              <DropdownButton className="mr-1" as={ButtonGroup} title={this.state.controllerAmount}>
                <Dropdown.Item key={"All"} onClick={() => this.setControllerAmount("All")}>{"All"}</Dropdown.Item>
                {controllerAmountList}
              </DropdownButton>

              <Dropdown className="mr-1" as={ButtonGroup}>
                <Dropdown.Toggle>{this.state.controller}</Dropdown.Toggle>
                <Dropdown.Menu as={CustomSearchMenu}>
                  {controllersListDropdown}
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