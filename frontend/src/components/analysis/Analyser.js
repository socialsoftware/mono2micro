import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { Row, Col, Form, DropdownButton, Dropdown, Button, Breadcrumb } from 'react-bootstrap';
import BootstrapTable from 'react-bootstrap-table-next';

var HttpStatus = require('http-status-codes');

var count = 0;
var interval = 10;
var multiplier = 10;
var minClusters = 2;
var maxClusters = 15;

var activeRequests = 0;
var maxActiveRequests = 10;

var total = 0;

var maxRequests = 50;

export class Analyser extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            codebases: [],
            codebase: {},
            experts: [],
            expert: {},
            resultData: []
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
            experts: codebase.dendrograms.map(dendrogram => dendrogram.graphs)
                                        .flat()
                                        .filter(graph => graph.expert === true)
        });
    }

    setExpert(expert) {
        this.setState({
            expert: expert
        });
    }

    async sendRequest(a, w, r, s1, s2) {
        total += maxClusters - minClusters;
        const service = new RepositoryService();

        a *= multiplier;
        w *= multiplier;
        r *= multiplier;
        s1 *= multiplier;
        s2 *= multiplier;

        for (var n = minClusters; n < maxClusters; n++) {
            let requestData = {
                "codebaseName": this.state.codebase.name,
                "expert": this.state.expert,
                "accessWeight": a,
                "writeWeight": w,
                "readWeight": r,
                "sequence1Weight": s1,
                "sequence2Weight": s2,
                "numberClusters": n
            };

            while (activeRequests >= maxActiveRequests) {
                await this.sleep(1000);
            }

            if (count === maxRequests)
                return;

            activeRequests = activeRequests + 1;
            count = count + 1;
            
            service.analyser(requestData).then(response => {
                activeRequests = activeRequests - 1;
                if (response.status === HttpStatus.OK) {
                    this.setState({
                        resultData: [...this.state.resultData, response.data]
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

    handleSubmit(event) {
        event.preventDefault();

        for (var a = interval; a >= 0; a--) { 
            let remainder = interval - a;
            if (remainder === 0) {
                this.sendRequest(a, 0, 0, 0, 0);
            } else {
                for (var w = remainder; w >= 0; w--) { 
                    let remainder2 = remainder - w;
                    if (remainder2 === 0) {
                        this.sendRequest(a, w, 0, 0, 0);
                    } else {
                        for (var r = remainder2; r >= 0; r--) { 
                            let remainder3 = remainder2 - r;
                            if (remainder3 === 0) {
                                this.sendRequest(a, w, r, 0, 0);
                            } else {
                                for (var s1 = remainder3; s1 >= 0; s1--) {
                                    let remainder4 = remainder3 - s1;
                                    if (remainder4 === 0) {
                                        this.sendRequest(a, w, r, s1, 0);
                                    } else {
                                        this.sendRequest(a, w, r, s1, remainder4);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    renderBreadCrumbs = () => {
        return (
            <Breadcrumb>
                <Breadcrumb.Item href="/">Home</Breadcrumb.Item>
                <Breadcrumb.Item active>Analyser</Breadcrumb.Item>
            </Breadcrumb>
        );
    }

    render() {

        const metricRows = this.state.resultData.map((data, index) => {
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
                fmeasure: data.fmeasure,
                complexity: data.complexity
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
        }, {
            dataField: 'complexity',
            text: 'Complexity',
            sort: true,
            /*sortFunc: (a, b, order, dataField, rowA, rowB) => {
                if (order === 'asc')
                    return a - b;
                else
                    return b - a;
            }*/
        }];

        return (
            <div>
                {this.renderBreadCrumbs()}
                <h4 style={{color: "#666666"}}>Analyser</h4>

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
                            <DropdownButton title={Object.keys(this.state.expert).length === 0 ? "Select Expert Cut" : this.state.expert.name}>
                                {this.state.experts.map(expert => 
                                    <Dropdown.Item 
                                        key={expert.name}
                                        onClick={() => this.setExpert(expert)}>{expert.name}</Dropdown.Item>)}
                            </DropdownButton>
                        </Col>
                    </Form.Group>

                    <Form.Group as={Row}>
                        <Col sm={{ span: 5, offset: 2 }}>
                            <Button type="submit"
                                    disabled={Object.keys(this.state.codebase).length === 0 || 
                                            Object.keys(this.state.expert).length === 0}>
                                Submit
                            </Button>
                            <Form.Text>
                                Loading: {this.state.resultData.length}/{count}/{total}
                            </Form.Text>
                        </Col>
                    </Form.Group>
                </Form>

                <BootstrapTable bootstrap4 keyField='id' data={ metricRows } columns={ metricColumns } />
            </div>
        )
    }
}