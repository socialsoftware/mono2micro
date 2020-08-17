import React from 'react';
import Parser from 'html-react-parser';
import Modal from 'react-bootstrap/Modal';
import Button from 'react-bootstrap/Button';


export class ModalMessage extends React.Component {
    constructor(props, context) {
      super(props, context);
  
      this.handleShow = this.handleShow.bind(this);
      this.handleClose = this.handleClose.bind(this);
  
      this.state = {
        show: true
      };
    }
  
    handleClose() {
      this.setState({ show: false });
      this.props.onClose();
    }
  
    handleShow() {
      this.setState({ show: true });
    }
  
    render() {  
      return (
          <Modal show={this.state.show} onHide={this.handleClose}>
            <Modal.Header closeButton>
              <Modal.Title>{this.props.title}</Modal.Title>
            </Modal.Header>
            <Modal.Body>
              <p>
                  {Parser(this.props.message)}
              </p>
            </Modal.Body>
            <Modal.Footer>
              <Button onClick={this.handleClose}>Close</Button>
            </Modal.Footer>
          </Modal>
      );
    }
  }