import {Modal, ModalBody, ProgressBar} from "react-bootstrap";
import React from "react";

export const ModalProgressBar = ({now}) => {
    return (
        <Modal
            show={now !== 0}
            keyboard={false}
            onHide={() => {}}
            centered
        >
            <ModalBody>
                <ProgressBar
                    now={now}
                    striped
                    animated
                />
            </ModalBody>
        </Modal>
    );
}