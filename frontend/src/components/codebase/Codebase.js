
import React from 'react';
import Breadcrumb from 'react-bootstrap/Breadcrumb';
import Card from 'react-bootstrap/Card';
import Button from 'react-bootstrap/Button';

export class Codebase extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            codebaseName: this.props.match.params.codebaseName
        };
    }

    renderBreadCrumbs = () => {
        return (
            <Breadcrumb>
                <Breadcrumb.Item href="/">
                    Home
                </Breadcrumb.Item>
                <Breadcrumb.Item href="/codebases">
                    Codebases
                </Breadcrumb.Item>
                <Breadcrumb.Item active>
                    {this.state.codebaseName}
                </Breadcrumb.Item>
            </Breadcrumb>
        );
    }

    render() {
        return (
            <div>
                {this.renderBreadCrumbs()}
                <h4 style={{color: "#666666"}}>Codebase</h4>

                <Card key={this.state.codebaseName} style={{ width: '18rem' }}>
                    <Card.Body>
                        <Card.Title>
                            {this.state.codebaseName}
                        </Card.Title>
                        <Button
                            href={`/codebases/${this.state.codebaseName}/profiles`} 
                            className="mb-2"
                        >
                            Change Controller Profiles
                        </Button>
                        <br/>
                        <Button 
                            href={`/codebases/${this.state.codebaseName}/dendrograms`} 
                            className="mb-2"
                        >
                            Go to Dendrograms
                        </Button>
                    </Card.Body>
                </Card>
            </div>
        );
    }
}