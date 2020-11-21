import React, { FunctionComponent, useContext } from 'react';
import { Link } from 'react-router-dom';
import Nav from 'react-bootstrap/Nav';
import Navbar from 'react-bootstrap/Navbar';
import AppContext from "./AppContext";
import FormControl from 'react-bootstrap/FormControl';
import Form from "react-bootstrap/Form";

const Header: FunctionComponent = () => {
    const {
        updateEntityTranslationFile,
    } = useContext(AppContext);


    const handleSelectedFile = async (event: React.ChangeEvent<HTMLInputElement>) => {
        const { files } = event.target;

        if (files && files.length > 0) {
            try {
                const fileText = await files[0].text();
                updateEntityTranslationFile(JSON.parse(fileText));
            } catch (err) {
                console.error(err);
            }
        }
    }

    return (
        <Navbar bg="dark" variant="dark" >
            <Navbar.Brand>
                <Link to='/'>
                    Mono2Micro
                </Link>
            </Navbar.Brand>
            <Navbar.Toggle />
            <Navbar.Collapse>
                <Nav>
                    <Nav.Link href="/codebases">
                        Codebases
                    </Nav.Link>
                    <Nav.Link href="/analysis">
                        Microservice Analysis
                    </Nav.Link>
                    <Nav.Link href="/analyser">
                        Analyser
                    </Nav.Link>
                </Nav>
            </Navbar.Collapse>
            <Form inline>
                <FormControl
                    type="file"
                    onChange={handleSelectedFile}
                    style={{ color: "white" }}
                />
            </Form>
        </Navbar>
    );
}

export default Header;