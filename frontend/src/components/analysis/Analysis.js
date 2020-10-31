import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Form from 'react-bootstrap/Form';
import DropdownButton from 'react-bootstrap/DropdownButton';
import Dropdown from 'react-bootstrap/Dropdown';
import Button from 'react-bootstrap/Button';
import Breadcrumb from 'react-bootstrap/Breadcrumb';
import BootstrapTable from 'react-bootstrap-table-next';
import ToolkitProvider, { Search } from 'react-bootstrap-table2-toolkit';

var HttpStatus = require('http-status-codes');

export class Analysis extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            codebases: [],
            codebase: {},
            decompositions: [],
            isUploaded: "",
            decomposition1: {},
            decomposition2: {},
            resultData: {},
            falsePairs: []
        };

        this.handleSubmit = this.handleSubmit.bind(this);
    }

    componentDidMount() {
        this.loadCodebases();
    }

    loadCodebases() {
        const service = new RepositoryService();
        service.getCodebases(
            [ "name" ]
        ).then(response => {
            this.setState({
                codebases: response.data
            });
        });
    }

    loadCodebaseDecompositions(codebaseName) {
        const service = new RepositoryService();
        
        service.getCodebaseDecompositions(
            codebaseName,
            [
                "name",
                "dendrogramName",
                "expert",
                "codebaseName",
                "clusters",
            ]
        ).then((response) => {
            if (response.data !== null) {
                this.setState({
                    decompositions: response.data,
                });
            }
        });
    }

    setCodebase(codebase) {
        this.setState({
            codebase: codebase,
        });

        this.loadCodebaseDecompositions(codebase.name);
    }

    setDecomposition1(decomposition) {
        this.setState({
            decomposition1: decomposition
        });
    }

    setDecomposition2(decomposition) {
        this.setState({
            decomposition2: decomposition
        });
    }

    handleSubmit(event) {
        event.preventDefault();

        this.setState({
            isUploaded: "Uploading..."
        });

        let requestData = {
            "decomposition1": this.state.decomposition1,
            "decomposition2": this.state.decomposition2
        };

        const service = new RepositoryService();
        service.analysis(requestData)
            .then(response => {
                if (response.status === HttpStatus.OK) {
                    this.setState({
                        resultData: response.data,
                        falsePairs: response.data.falsePairs,
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

    renderBreadCrumbs = () => {
        return (
            <Breadcrumb>
                <Breadcrumb.Item href="/">Home</Breadcrumb.Item>
                <Breadcrumb.Item active>Microservice Analysis</Breadcrumb.Item>
            </Breadcrumb>
        );
    }

    render() {

        const {
            codebase,
            codebases,
            falsePairs,
            decomposition1,
            decomposition2,
            decompositions,
            isUploaded,
            resultData,
        } = this.state;

        const falsePairRows = falsePairs.map(falsePair => {
            return {
                id: falsePair[0] + falsePair[3],
                e1: falsePair[0],
                e1g1: falsePair[1],
                e1g2: falsePair[2],
                e2: falsePair[3],
                e2g1: falsePair[4],
                e2g2: falsePair[5]
            }
        });

        const { SearchBar } = Search;

        const falsePairColumns = [{
            dataField: 'e1',
            text: 'Entity 1',
            sort: true
        }, {
            dataField: 'e1g1',
            text: decomposition1.name,
            sort: true
        }, {
            dataField: 'e1g2',
            text: decomposition2.name,
            sort: true
        }, {
            dataField: 'space',
            text: ''
        }, {
            dataField: 'e2',
            text: 'Entity 2',
            sort: true
        }, {
            dataField: 'e2g1',
            text: decomposition1.name,
            sort: true
        }, {
            dataField: 'e2g2',
            text: decomposition2.name,
            sort: true
        }];

        return (
            <>
                {this.renderBreadCrumbs()}
                <h4 style={{ color: "#666666" }}>Microservice Analysis</h4>

                <Form onSubmit={this.handleSubmit}>
                    <Form.Group as={Row} controlId="codebase">
                        <Form.Label column sm={2}>
                            Codebase
                        </Form.Label>
                        <Col sm={5}>
                            <DropdownButton
                                title={Object.keys(codebase).length === 0 ?
                                    "Select Codebase" :
                                    codebase.name
                                }
                            >
                                {codebases.map(codebase =>
                                    <Dropdown.Item
                                        key={codebase.name}
                                        onClick={() => this.setCodebase(codebase)}
                                    >
                                        {codebase.name}
                                    </Dropdown.Item>
                                )}
                            </DropdownButton>
                        </Col>
                    </Form.Group>

                    <Form.Group as={Row} controlId="sourceOfTruth">
                        <Form.Label column sm={2}>
                            Source of Truth
                        </Form.Label>
                        <Col sm={5}>
                            <DropdownButton
                                title={
                                    Object.keys(decomposition1).length === 0 ?
                                        "Select Cut" :
                                        decomposition1.name + " from " + decomposition1.dendrogramName
                                }
                            >
                                {
                                    decompositions.filter(decomposition => decomposition.expert === true).map(decomposition =>
                                        <Dropdown.Item
                                            key={decomposition.name}
                                            onClick={() => this.setDecomposition1(decomposition)}
                                        >
                                            {decomposition.name + " from " + decomposition.dendrogramName}
                                        </Dropdown.Item>
                                    )
                                }
                                <Dropdown.Divider />
                                {
                                    decompositions.filter(decomposition => decomposition.expert === false).map(decomposition =>
                                        <Dropdown.Item
                                            key={decomposition.name}
                                            onClick={() => this.setDecomposition1(decomposition)}
                                        >
                                            {decomposition.name + " from " + decomposition.dendrogramName}
                                        </Dropdown.Item>)
                                }
                            </DropdownButton>
                        </Col>
                    </Form.Group>

                    <Form.Group as={Row} controlId="compareToCut">
                        <Form.Label column sm={2}>
                            Compare to Cut
                        </Form.Label>
                        <Col sm={5}>
                            <DropdownButton
                                title={Object.keys(decomposition2).length === 0 ?
                                    "Select Cut" :
                                    decomposition2.name + " from " + decomposition2.dendrogramName
                                }
                            >
                                {
                                    decompositions.filter(decomposition => decomposition.expert === true).map(decomposition =>
                                        <Dropdown.Item
                                            key={decomposition.name}
                                            onClick={() => this.setDecomposition2(decomposition)}
                                        >
                                            {decomposition.name + " from " + decomposition.dendrogramName}
                                        </Dropdown.Item>
                                    )
                                }
                                <Dropdown.Divider />
                                {
                                    decompositions.filter(decomposition => decomposition.expert === false).map(decomposition =>
                                        <Dropdown.Item
                                            key={decomposition.name}
                                            onClick={() => this.setDecomposition2(decomposition)}
                                        >
                                            {decomposition.name + " from " + decomposition.dendrogramName}
                                        </Dropdown.Item>
                                    )
                                }
                            </DropdownButton>
                        </Col>
                    </Form.Group>

                    <Form.Group as={Row}>
                        <Col sm={{ span: 5, offset: 2 }}>
                            <Button
                                type="submit"
                                disabled={Object.keys(codebase).length === 0 ||
                                    Object.keys(decomposition1).length === 0 ||
                                    Object.keys(decomposition2).length === 0
                                }
                            >
                                Submit
                            </Button>
                            <Form.Text>
                                {isUploaded}
                            </Form.Text>
                        </Col>
                    </Form.Group>
                </Form>

                {Object.keys(resultData).length !== 0 &&
                    <>
                        <h4 style={{ color: "#666666" }}> Metrics </h4>
                        <BootstrapTable
                            keyField='id'
                            data={[{
                                id: "metrics",
                                tp: resultData.truePositive,
                                tn: resultData.trueNegative,
                                fp: resultData.falsePositive,
                                fn: resultData.falseNegative,
                                accuracy: resultData.accuracy,
                                precision: resultData.precision,
                                recall: resultData.recall,
                                specificity: resultData.specificity === -1 ? "--" : resultData.specificity,
                                fscore: resultData.fmeasure,
                                mojoCommon: resultData.mojoCommon,
                                mojoBiggest: resultData.mojoBiggest,
                                mojoNew: resultData.mojoNew,
                                mojoSingletons: resultData.mojoSingletons,
                            }
                            ]}
                            columns={[
                                {
                                    dataField: 'tp',
                                    text: 'TP',
                                },
                                {
                                    dataField: 'tn',
                                    text: 'TN',
                                },
                                {
                                    dataField: 'fp',
                                    text: 'FP',
                                },
                                {
                                    dataField: 'fn',
                                    text: 'FN',
                                },
                                {
                                    dataField: 'fscore',
                                    text: 'F-Score',
                                },
                                {
                                    dataField: 'accuracy',
                                    text: 'Accuracy',
                                },
                                {
                                    dataField: 'precision',
                                    text: 'Precision',
                                },
                                {
                                    dataField: 'recall',
                                    text: 'Recall',
                                },
                                {
                                    dataField: 'specificity',
                                    text: 'Specificity',
                                },
                                {
                                    dataField: 'mojoCommon',
                                    text: 'MoJo Common Entities',
                                },
                                {
                                    dataField: 'mojoBiggest',
                                    text: 'MoJo Biggest Cluster',
                                },
                                {
                                    dataField: 'mojoNew',
                                    text: 'MoJo New Cluster',
                                },
                                {
                                    dataField: 'mojoSingletons',
                                    text: 'MoJo Singletons',
                                },
                            ]}
                            bootstrap4
                        />
                        <hr />
                        <h4 style={{ color: "#666666" }}>False Pairs</h4>

                        <ToolkitProvider
                            bootstrap4
                            keyField="id"
                            data={falsePairRows}
                            columns={falsePairColumns}
                            search>
                            {
                                props => (
                                    <>
                                        <SearchBar {...props.searchProps} />
                                        <BootstrapTable {...props.baseProps} />
                                    </>
                                )
                            }
                        </ToolkitProvider>
                    </>
                }
            </>
        )
    }
}