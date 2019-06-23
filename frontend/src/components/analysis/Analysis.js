import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { Row, Col, Form, DropdownButton, Dropdown, Button, Breadcrumb } from 'react-bootstrap';

var HttpStatus = require('http-status-codes');

export class Analysis extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            codebases: [],
            codebase: {},
            experts: [],
            graphs: [],
            isUploaded: "",
            graph1: {},
            graph2: {},
            result: {}
        };

        this.handleSubmit = this.handleSubmit.bind(this);
    }

    componentDidMount() {
        this.loadCodebases()
    }

    loadCodebases() {
        const service = new RepositoryService();
        service.getCodebases().then(response => {
            this.setState({
                codebases: response.data
            });
        });
    }

    setCodebase(codebase) {
        this.setState({
            codebase: codebase,
            experts: codebase.experts,
            graphs: codebase.dendrograms.map(dend => dend.graphs).flat()
        });
    }

    setGraph1(graph) {
        this.setState({
            graph1: graph
        });
    }

    setGraph2(graph) {
        this.setState({
            graph2: graph
        });
    }

    handleSubmit(event) {
        event.preventDefault();

        this.setState({
            isUploaded: "Uploading..."
        });

        let requestData = {};
        requestData["graphName1"] = this.state.graph1.name;
        requestData["graphName2"] = this.state.graph2.name;
        if (this.state.graph1.dendrogramName !== undefined)
            requestData["dendrogramName1"] = this.state.graph1.dendrogramName;
        if (this.state.graph2.dendrogramName !== undefined)
            requestData["dendrogramName2"] = this.state.graph2.dendrogramName;
        
        

        const service = new RepositoryService();
        service.getAnalysis(this.state.codebase.name, requestData).then(response => {
            if (response.status === HttpStatus.OK) {
                this.setState({
                    result: response.data,
                    isUploaded: "Upload completed successfully."
                });
            } else {
                this.setState({
                    isUploaded: "Upload failed."
                });
            }
        })
        .catch(error => {
            this.setState({
                isUploaded: "Upload failed."
            });
        });
    }

    render() {
        const BreadCrumbs = () => {
            return (
                <div>
                    <Breadcrumb>
                        <Breadcrumb.Item href="/">Home</Breadcrumb.Item>
                        <Breadcrumb.Item active>Microservice Analysis</Breadcrumb.Item>
                    </Breadcrumb>
                </div>
            );
        };

        return (
            <div>
                <BreadCrumbs />
                <h2>Microservice Analysis</h2>

                <Form onSubmit={this.handleSubmit}>
                    <Form.Group as={Row} controlId="codebase">
                        <Form.Label column sm={2}>
                            Codebase
                        </Form.Label>
                        <Col sm={5}>
                            <DropdownButton title={Object.keys(this.state.codebase).length === 0 ? "Select Codebase" : this.state.codebase.name}>
                                {this.state.codebases.map(codebase => 
                                    <Dropdown.Item 
                                        key={codebase.name}
                                        onClick={() => this.setCodebase(codebase)}>{codebase.name}</Dropdown.Item>)}
                            </DropdownButton>
                        </Col>
                    </Form.Group>

                    <Form.Group as={Row} controlId="sourceOfTruth">
                        <Form.Label column sm={2}>
                            Source of Truth
                        </Form.Label>
                        <Col sm={5}>
                            <DropdownButton title={Object.keys(this.state.graph1).length === 0 ? "Select Cut" : this.state.graph1.dendrogramName === undefined ? "Expert: " + this.state.graph1.name : this.state.graph1.name + " from " + this.state.graph1.dendrogramName}>
                                {this.state.experts.map(expert =>
                                    <Dropdown.Item
                                        key={expert.name}
                                        onClick={() => this.setGraph1(expert)}>{"Expert: " + expert.name}</Dropdown.Item>)}
                                <Dropdown.Divider />
                                {this.state.graphs.map(graph => 
                                    <Dropdown.Item
                                        key={graph.name}
                                        onClick={() => this.setGraph1(graph)}>{graph.name + " from " + graph.dendrogramName}</Dropdown.Item>)}
                            </DropdownButton>
                        </Col>
                    </Form.Group>

                    <Form.Group as={Row} controlId="compareToCut">
                        <Form.Label column sm={2}>
                            Compare to Cut
                        </Form.Label>
                        <Col sm={5}>
                            <DropdownButton title={Object.keys(this.state.graph2).length === 0 ? "Select Cut" : this.state.graph2.dendrogramName === undefined ? "Expert: " + this.state.graph2.name : this.state.graph2.name + " from " + this.state.graph2.dendrogramName}>
                                {this.state.experts.map(expert =>
                                    <Dropdown.Item
                                        key={expert.name}
                                        onClick={() => this.setGraph2(expert)}>{"Expert: " + expert.name}</Dropdown.Item>)}
                                <Dropdown.Divider />
                                {this.state.graphs.map(graph => 
                                    <Dropdown.Item
                                        key={graph.name}
                                        onClick={() => this.setGraph2(graph)}>{graph.name + " from " + graph.dendrogramName}</Dropdown.Item>)}
                            </DropdownButton>
                        </Col>
                    </Form.Group>

                    <Form.Group as={Row}>
                        <Col sm={{ span: 5, offset: 2 }}>
                            <Button type="submit"
                                    disabled={Object.keys(this.state.codebase).length === 0 || 
                                            Object.keys(this.state.graph1).length === 0 || 
                                            Object.keys(this.state.graph2).length === 0}>
                                Submit
                            </Button>
                            <Form.Text>
                                {this.state.isUploaded}
                            </Form.Text>
                        </Col>
                    </Form.Group>
                </Form>

                {Object.keys(this.state.result).length !== 0 &&
                    <div>
                        TP : {this.state.result.truePositive}< br/>
                        TN : {this.state.result.trueNegative}< br/>
                        FP : {this.state.result.falsePositive}< br/>
                        FN : {this.state.result.falseNegative}< br/>
                        Precision : {this.state.result.precision.toFixed(2)}< br/>
                        Recall : {this.state.result.recall.toFixed(2)}< br/>
                        F-Score : {this.state.result.fmeasure.toFixed(2)}< br/>
                    </div>
                }
            </div>
        )
    }
}