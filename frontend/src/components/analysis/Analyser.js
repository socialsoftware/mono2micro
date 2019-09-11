import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { Row, Col, Form, DropdownButton, Dropdown, Button, Breadcrumb } from 'react-bootstrap';
import BootstrapTable from 'react-bootstrap-table-next';

var HttpStatus = require('http-status-codes');

var count = 0;
var interval = 100;
var multiplier = 1;
var minClusters = 2;
var maxClusters = 10;


export class Analyser extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            codebases: [],
            codebase: {},
            experts: [],
            expert: "",
            allData: []
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
            experts: codebase.experts
        });
    }

    setExpert(expert) {
        this.setState({
            expert: expert
        });
    }

    sendRequest(a, w, r, s1, s2) {
        const service = new RepositoryService();

        a *= multiplier;
        w *= multiplier;
        r *= multiplier;
        s1 *= multiplier;
        s2 *= multiplier;

        for (var n = minClusters; n < maxClusters; n++) {
            let requestData = {};
            requestData["codebaseName"] = this.state.codebase.name;
            requestData["expertName"] = this.state.expert;
            requestData["accessWeight"] = a;
            requestData["writeWeight"] = w;
            requestData["readWeight"] = r;
            requestData["sequence1Weight"] = s1;
            requestData["sequence2Weight"] = s2;
            requestData["numberClusters"] = n;

            count = count + 1;
            service.analyser(requestData).then(response => {
                if (response.status === HttpStatus.OK) {
                    this.setState({
                        allData: [...this.state.allData, response.data]
                    });
                } else {
                    console.log(response);
                }
            })
            .catch(error => {
                console.log(error);
            });
        }
    }

    sleep(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    async handleSubmit(event) {
        event.preventDefault();

        for (var a = interval; a >= 0; a--) { 
            let remainder = interval - a;
            if (remainder === 0) {
                this.sendRequest(a, 0, 0, 0, 0);
                await this.sleep(4000);
            } else {
                for (var w = remainder; w >= 0; w--) { 
                    let remainder2 = remainder - w;
                    if (remainder2 === 0) {
                        this.sendRequest(a, w, 0, 0, 0);
                        await this.sleep(4000);
                    } else {
                        for (var r = remainder2; r >= 0; r--) { 
                            let remainder3 = remainder2 - r;
                            if (remainder3 === 0) {
                                this.sendRequest(a, w, r, 0, 0);
                                await this.sleep(4000);
                            } else {
                                for (var s1 = remainder3; s1 >= 0; s1--) {
                                    let remainder4 = remainder3 - s1;
                                    if (remainder4 === 0) {
                                        this.sendRequest(a, w, r, s1, 0);
                                        await this.sleep(4000);
                                    } else {
                                        this.sendRequest(a, w, r, s1, remainder4);
                                        await this.sleep(4000);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    render() {
        const BreadCrumbs = () => {
            return (
                <div>
                    <Breadcrumb>
                        <Breadcrumb.Item href="/">Home</Breadcrumb.Item>
                        <Breadcrumb.Item active>Analyser</Breadcrumb.Item>
                    </Breadcrumb>
                </div>
            );
        };

        const metricRows = this.state.allData.map((data, index) => {
            return {
                id: index,
                access: data.accessWeight,
                write: data.writeWeight,
                read: data.readWeight,
                sequence1: data.sequence1Weight,
                sequence2: data.sequence2Weight,
                numberClusters: data.numberClusters,
                accuracy: data.accuracy,
                precision: data.precision,
                recall: data.recall,
                specificity: data.specificity,
                fmeasure: data.fmeasure
            } 
        });

        const metricColumns = [{
            dataField: 'access',
            text: 'Access',
            sort: true
        }, {
            dataField: 'write',
            text: 'Write',
            sort: true
        }, {
            dataField: 'read',
            text: 'Read',
            sort: true
        }, {
            dataField: 'sequence1',
            text: 'Sequence1',
            sort: true
        }, {
            dataField: 'sequence2',
            text: 'Sequence2',
            sort: true
        }, {
            dataField: 'numberClusters',
            text: 'Number Clusters',
            sort: true
        }, {
            dataField: 'accuracy',
            text: 'Accuracy',
            sort: true
        }, {
            dataField: 'precision',
            text: 'Precision',
            sort: true
        }, {
            dataField: 'recall',
            text: 'Recall',
            sort: true
        }, {
            dataField: 'specificity',
            text: 'Specificity',
            sort: true
        }, {
            dataField: 'fmeasure',
            text: 'F-Score',
            sort: true
        }];

        return (
            <div>
                <BreadCrumbs />
                <h2>Analyser</h2>

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

                    <Form.Group as={Row} controlId="expert">
                        <Form.Label column sm={2}>
                            Expert
                        </Form.Label>
                        <Col sm={5}>
                            <DropdownButton title={this.state.expert === "" ? "Select Expert Cut" : this.state.expert}>
                                {this.state.experts.map(expert => 
                                    <Dropdown.Item 
                                        key={expert.name}
                                        onClick={() => this.setExpert(expert.name)}>{expert.name}</Dropdown.Item>)}
                            </DropdownButton>
                        </Col>
                    </Form.Group>

                    <Form.Group as={Row}>
                        <Col sm={{ span: 5, offset: 2 }}>
                            <Button type="submit"
                                    disabled={Object.keys(this.state.codebase).length === 0 || 
                                            this.state.expert === ""}>
                                Submit
                            </Button>
                            <Form.Text>
                                Loading: {this.state.allData.length}/{count}
                            </Form.Text>
                        </Col>
                    </Form.Group>
                </Form>

                <BootstrapTable bootstrap4 keyField='id' data={ metricRows } columns={ metricColumns } />
            </div>
        )
    }
}