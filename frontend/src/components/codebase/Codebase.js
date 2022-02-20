import React from 'react';
import Breadcrumb from 'react-bootstrap/Breadcrumb';
import Card from 'react-bootstrap/Card';
import Button from 'react-bootstrap/Button';
import {useParams} from "react-router-dom";

function renderBreadCrumbs(codebaseName) {
    return (
        <Breadcrumb>
            <Breadcrumb.Item href="/">
                Home
            </Breadcrumb.Item>
            <Breadcrumb.Item href="/codebases">
                Codebases
            </Breadcrumb.Item>
            <Breadcrumb.Item active>
                {codebaseName}
            </Breadcrumb.Item>
        </Breadcrumb>
    );
}

export function Codebase() {
    let { codebaseName } = useParams();

    return (
        <div>
            {renderBreadCrumbs(codebaseName)}
            <h4 style={{color: "#666666"}}>Codebase</h4>

            <Card key={codebaseName} style={{ width: '18rem' }}>
                <Card.Body>
                    <Card.Title>
                        {codebaseName}
                    </Card.Title>
                    <Button
                        href={`/codebases/${codebaseName}/profiles`}
                        className="mb-2"
                    >
                        Change Controller Profiles
                    </Button>
                    <br/>
                    <Button
                        href={`/codebases/${codebaseName}/dendrograms`}
                        className="mb-2"
                    >
                        Go to Dendrograms
                    </Button>
                </Card.Body>
            </Card>
        </div>
    );
}