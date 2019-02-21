import React from 'react';
import { Button, DropdownButton, Dropdown, FormControl, ButtonToolbar, ButtonGroup } from 'react-bootstrap';

export class TransactionOperationsMenu extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            showSubmit: false,
            controller: 'Select Controller'
        }

        this.setController = this.setController.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    setController(value) {
        this.setState({
            showSubmit: true,
            controller: value
        });
    }
    
    handleSubmit() {
        this.props.handleControllerSubmit(this.state.controller);
    }

    render() {
        const controllersList = this.props.controllers.map(c =>
            <Dropdown.Item key={c.name} onClick={() => this.setController(c.name)}>{c.name}</Dropdown.Item>
        );
       return (
            <ButtonToolbar>
                <DropdownButton className="mr-1" as={ButtonGroup}
                    title={this.state.controller}>
                    <Dropdown.Menu as={CustomSearchMenu}>
                        {controllersList}
                    </Dropdown.Menu>
                    
                </DropdownButton>

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