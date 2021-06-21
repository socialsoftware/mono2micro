import React from 'react';
import {RepositoryService} from '../../services/RepositoryService';
import BootstrapTable from 'react-bootstrap-table-next';
import Button from '@material-ui/core/Button';
import LinearProgress from '@material-ui/core/LinearProgress';
import Slider from '@material-ui/core/Slider';


export class FunctionalityRefactorToolMenu extends React.Component{

    constructor(props) {
        super(props);

        this.state = {
            waitingResponse: false,
            controllers: [],
            dataDependenceThreshold: 0,
            timeoutSecs: 10,
            minimizeSumOfComplexities: false,
            refactorization: null,
            refactorizationRows: [],
            refactorizationColumns: [
                {
                    dataField: 'controller',
                    text: 'Controller',
                    sortable: true
                }, {
                    dataField: 'status',
                    text: 'Status',
                    sortable: false,
                }, {
                    dataField: 'functionalityComplexity',
                    text: 'Functionality Complexity',
                    sortable: true
                }, {
                    dataField: 'systemComplexity',
                    text: 'System Complexity',
                    sortable: true
                }, {
                    dataField: 'invocations',
                    text: 'Invocations',
                    sortable: true
                }, {
                    dataField: 'accesses',
                    text: 'Accesses',
                    sortable: true
                }, {
                    dataField: 'sagaOrchestrator',
                    text: 'Orchestrator',
                    sortable: true
                }, {
                    dataField: 'sagaFunctionalityComplexity',
                    text: 'SAGA Functionality Complexity',
                    sortable: true
                },  {
                    dataField: 'sagaSystemComplexity',
                    text: 'SAGA System Complexity',
                    sortable: true
                },  {
                    dataField: 'sagaInvocations',
                    text: 'SAGA Invocations',
                    sortable: true
                },  {
                    dataField: 'sagaAccesses',
                    text: 'SAGA Accesses',
                    sortable: true
                },  {
                    dataField: 'functionalityComplexityReduction',
                    text: 'Functionality Complexity Reduction',
                    sortable: true
                },  {
                    dataField: 'systemComplexityReduction',
                    text: 'System Complexity Reduction',
                    sortable: true
                },  {
                    dataField: 'invocationMerges',
                    text: 'Invocation Merges',
                    sortable: true
                }
            ],
        };

        this.handleRefactorCodebase = this.handleRefactorCodebase.bind(this);
    }

    handleChangeThreshold(event, newValue) {
        this.setState({dataDependenceThreshold: newValue});
    }

    handleChangeTimeout(event, newValue) {
        this.setState({timeoutSecs: newValue});
    }

    handleDataParsing(data) {
        var refactorizationRows = []
        var id = 0

        try {
            for(const [key, controller] of Object.entries(data['controllers'])) {
                var refactorizationRows = refactorizationRows.concat({
                    controller: key,
                    functionalityComplexity: controller['monolith']['complexity_metrics']['functionality_complexity'],
                    systemComplexity: controller['monolith']['complexity_metrics']['system_complexity'],
                    invocations: controller['monolith']['complexity_metrics']['invocations_count'] === undefined ? 0 : controller['monolith']['complexity_metrics']['invocations_count'],
                    accesses: controller['monolith']['complexity_metrics']['accesses_count'] === undefined ? 0 : controller['monolith']['complexity_metrics']['accesses_count'],
                    sagaOrchestrator: controller['refactor'] === undefined ? 0 : controller['refactor']['orchestrator']['name'],
                    sagaFunctionalityComplexity: controller['refactor'] === undefined ? 0 : controller['refactor']['complexity_metrics']['functionality_complexity'],
                    sagaSystemComplexity: controller['refactor'] === undefined ? 0 : controller['refactor']['complexity_metrics']['system_complexity'],
                    sagaInvocations: controller['refactor'] === undefined ? 0 : controller['refactor']['complexity_metrics']['invocations_count'],
                    sagaAccesses: controller['refactor'] === undefined ? 0 : controller['refactor']['complexity_metrics']['accesses_count'],
                    functionalityComplexityReduction: controller['refactor'] === undefined ? 0 : controller['refactor']['execution_metrics']['functionality_complexity_reduction'],
                    systemComplexityReduction: controller['refactor'] === undefined ? 0 : controller['refactor']['execution_metrics']['system_complexity_reduction'],
                    invocationMerges: controller['refactor'] === undefined ? 0 : controller['refactor']['execution_metrics']['invocation_merges'],
                    status: controller['error'] === undefined ? 'refactored' : 'timed out',
                })
                var id = id + 1
            }

            this.setState({
                refactorizationRows: refactorizationRows,
            });
            console.log(this.state.refactorizationRows)
        }
        catch (error) {
            console.log(error)
        }
    }

    handleRefactorCodebase(){
        const service = new RepositoryService();

        this.setState({
            waitingResponse: true,
        });

        service.refactorCodebase(this.props.codebaseName, this.props.dendrogramName, this.props.decompositionName,
            this.state.controllers, Number(this.state.dataDependenceThreshold), this.state.minimizeSumOfComplexities, Number(this.state.timeoutSecs))
            .then(response => {
                this.setState({
                    waitingResponse: false,
                });
                this.handleDataParsing(response.data)
            }).catch(() => {
                this.setState({
                    error: true,
                    errorMessage: 'ERROR: Failed to request refactorization of the codebase.',
                    waitingResponse: false,
                });
            }
        );
    }

    render() {
        let waitingResponse = this.state.waitingResponse

        return (
            <div>
                <label>
                    Data dependence threshold:
                </label>

                <div style={{width: "300px"}}>
                    <Slider
                        onChange={this.handleChangeThreshold.bind(this)}
                        defaultValue={0}
                        aria-labelledby="discrete-slider"
                        valueLabelDisplay="auto"
                        step={1}
                        marks
                        min={0}
                        max={5}
                    />
                </div>

                <br></br>

                <label>
                    Controller refactor time out seconds:
                </label>
                
                <div style={{width: "300px"}}>
                    <Slider
                        onChange={this.handleChangeTimeout.bind(this)}
                        defaultValue={0}
                        aria-labelledby="continuous-slider"
                        valueLabelDisplay="auto"
                        step={5}
                        min={10}
                        max={1000}
                    />
                </div>
                
                <br></br>

                <Button 
                    onClick={ () => this.handleRefactorCodebase() } 
                    variant="contained" 
                    color="primary"
                >
                    Estimate Sagas
                </Button>

                <br></br>

                {waitingResponse && <LinearProgress />}

                <br></br>

                {<BootstrapTable
                    bootstrap4
                    keyField='controller'
                    data={this.state.refactorizationRows}
                    columns={this.state.refactorizationColumns}
                />}            
            </div>
        )
    }
}
