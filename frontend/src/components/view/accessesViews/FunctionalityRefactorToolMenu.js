import React, {useContext, useEffect, useState} from 'react';
import {APIService} from '../../../services/APIService';
import BootstrapTable from 'react-bootstrap-table-next';
import Button from '@mui/material/Button';
import CircularProgress from '@mui/material/CircularProgress';
import Slider from '@mui/material/Slider';
import Divider from '@mui/material/Divider';
import GrainIcon from '@mui/icons-material/Grain';
import {useParams} from "react-router-dom";
import {ModalMessage} from "../utils/ModalMessage";
import Breadcrumb from "react-bootstrap/Breadcrumb";
import {toast, ToastContainer} from "react-toastify";
import AppContext from "../../AppContext";

export const refactorToolHelp = (
<div>
    A brute-force heuristic algorithm to estimate the best Saga redesign for each one of the functionalities of a given decomposition.

    <br></br>

    <b>Data dependence:</b> the number of previous entity accesses that will be taken into account when deciding if two invocations can be merged.
    If set to 0, every Read access before the invocation being analyzed until the previous invocation of the same cluster, will be considered as a data dependence of a Write in the first.
</div>
);

const refactorizationColumns = [
    {
        dataField: 'functionality',
        text: 'Functionality',
        sort: true,
    }, {
        dataField: 'status',
        text: 'Status',
        sort: true,
    }, {
        dataField: 'error',
        text: 'Error',
        sort: false,
        hidden: true,
    }, {
        dataField: 'functionalityComplexity',
        text: 'Functionality Complexity',
        sort: true,
        hidden: true
    }, {
        dataField: 'systemComplexity',
        text: 'System Complexity',
        sort: true,
        hidden: true,
    }, {
        dataField: 'invocations',
        text: 'Invocations',
        sort: true,
        hidden: true
    }, {
        dataField: 'accesses',
        text: 'Accesses',
        sort: true,
        hidden: true
    }, {
        dataField: 'sagaOrchestrator',
        text: 'Orch.',
        sort: true
    }, {
        dataField: 'sagaFunctionalityComplexity',
        text: 'FRC',
        sort: true
    },  {
        dataField: 'sagaSystemComplexity',
        text: 'SAC',
        sort: true
    },  {
        dataField: 'sagaInvocations',
        text: 'Invocations',
        sort: true
    },  {
        dataField: 'sagaAccesses',
        text: 'Accesses',
        sort: true
    },  {
        dataField: 'functionalityComplexityReduction',
        text: 'FRC Reduction',
        sort: true
    },  {
        dataField: 'systemComplexityReduction',
        text: 'SAC Reduction',
        sort: true
    },  {
        dataField: 'invocationMerges',
        text: 'Merges',
        sort: true
    }
];
const functionalityRedesignColumns = [
    {
        dataField: 'cluster',
        text: 'Cluster',
        sort: false,
    }, {
        dataField: 'entity_accesses',
        text: 'Entity Accesses',
        sort: false,
    },
];

export const FunctionalityRefactorToolMenu = () => {
    const context = useContext(AppContext);
    const { updateEntityTranslationFile, translateEntity } = context;
    let { codebaseName, strategyName, similarityName, decompositionName } = useParams();

    const [waitingResponse, setWaitingResponse] = useState(false);
    const [functionalities, setFunctionalities] = useState([]);
    const [dataDependenceThreshold, setDataDependenceThreshold] = useState(0);
    const [timeoutSecs, setTimeoutSecs] = useState(null);
    const [minimizeSumOfComplexities, setMinimizeSumOfComplexities] = useState(false);
    const [refactorizationRows, setRefactorizationRows] = useState([]);
    const [refactorizationExists, setRefactorizationExists] = useState(false);
    const [functionalitiesRedesignRows, setFunctionalitiesRedesignRows] = useState([]);
    const [error, setError] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');

    const [isLoading, setIsLoading] = useState(false);

    useEffect(() => {
        const service = new APIService();
        viewCodebaseRefactor();
        service.getIdToEntity(codebaseName).then(response => {
            updateEntityTranslationFile(response.data);
        });
    }, []);

    useEffect(() => {
        viewCodebaseRefactor();
    }, [translateEntity]);

    function handleChangeThreshold(event) {
        setDataDependenceThreshold(event.target.value);
    }

    function handleChangeTimeout(event, newValue) {
        setTimeoutSecs(newValue);
    }

    function closeErrorMessageModal() {
        setError(false);
        setErrorMessage('');
    }

    function handleDataParsing(data) {
        let refactorizationRows = [];

        try {
            setIsLoading(true);
            let allCompleted = true;
            for(const [key, functionality] of Object.entries(data['functionalities'])) {
                let functionalityData = {};
                if(functionality['status'] === "COMPLETED") {
                    functionalityData = {
                        name: key,
                        functionalityComplexity: functionality['monolith']['complexity_metrics']['functionality_complexity'],
                        systemComplexity: functionality['monolith']['complexity_metrics']['system_complexity'],
                        invocations: functionality['monolith']['complexity_metrics']['invocations_count'] === undefined ? 0 : functionality['monolith']['complexity_metrics']['invocations_count'],
                        accesses: functionality['monolith']['complexity_metrics']['accesses_count'] === undefined ? 0 : functionality['monolith']['complexity_metrics']['accesses_count'],
                        sagaOrchestrator: functionality['refactor'] === undefined ? 0 : functionality['refactor']['orchestrator']['name'],
                        sagaFunctionalityComplexity: functionality['refactor'] === undefined ? 0 : functionality['refactor']['complexity_metrics']['functionality_complexity'],
                        sagaSystemComplexity: functionality['refactor'] === undefined ? 0 : functionality['refactor']['complexity_metrics']['system_complexity'],
                        sagaInvocations: functionality['refactor'] === undefined ? 0 : functionality['refactor']['complexity_metrics']['invocations_count'],
                        sagaAccesses: functionality['refactor'] === undefined ? 0 : functionality['refactor']['complexity_metrics']['accesses_count'],
                        functionalityComplexityReduction: functionality['refactor'] === undefined ? 0 : functionality['refactor']['execution_metrics']['functionality_complexity_reduction'],
                        systemComplexityReduction: functionality['refactor'] === undefined ? 0 : functionality['refactor']['execution_metrics']['system_complexity_reduction'],
                        invocationMerges: functionality['refactor'] === undefined ? 0 : functionality['refactor']['execution_metrics']['invocation_merges'],
                        callGraph: functionality['refactor'] === undefined ? 0 : functionality['refactor']['call_graph'],
                    };

                    handleRedesignDataParsing(key, functionality['refactor']["call_graph"])
                }
                else if(functionality['status'] === "REFACTORING")
                    allCompleted = false;


                functionalityData["functionality"] = key;
                functionalityData["error"] = functionality['error'];
                functionalityData["status"] = functionality['status'];

                refactorizationRows = refactorizationRows.concat(functionalityData)
            }

            setIsLoading(!allCompleted);

            setRefactorizationRows(refactorizationRows);
        }
        catch (error) {
            console.log(error)
        }
    }

    function handleRedesignDataParsing(name, data) {
        const tempFunctionalitiesRedesignRows = functionalitiesRedesignRows;

        try {
            let invocations = [];

            for(let i=0; i < data.length; i++) {
                let invocationAccesses = "";

                for(let j=0; j<data[i]["accesses"].length; j++) {
                    console.log(translateEntity(data[i]["accesses"][j]["entity_id"]));
                    console.log(translateEntity(3));
                    invocationAccesses = invocationAccesses.concat(data[i]["accesses"][j]["type"], " -> ", translateEntity(data[i]["accesses"][j]["entity_id"]), "    |    ");
                }

                invocations = invocations.concat(
                    {
                        cluster: data[i]['cluster_name'],
                        entity_accesses: invocationAccesses,
                    }
                )
            }

            tempFunctionalitiesRedesignRows[name] = invocations
            setFunctionalitiesRedesignRows(tempFunctionalitiesRedesignRows);
        }
        catch (error) {
            console.log(error)
        }
    }

    function viewCodebaseRefactor(){
        const service = new APIService();

        setWaitingResponse(true);

        service.viewRefactor(decompositionName)
            .then(response => {
                setWaitingResponse(false);
                setRefactorizationExists(true);
                handleDataParsing(response.data);
            }).catch((error) => {
                if (error.response.status === 404) { // Not found
                    toast.info("No refactorization information was created previously.")
                }
                else {
                    setError(true);
                    setErrorMessage('ERROR: Failed to view refactorization of the codebase.');
                    setIsLoading(false);
                }
                setWaitingResponse(false);
                setRefactorizationExists(false);
            }
        );
    }

    function requestCodebaseRefactor(newRefactor){
        const service = new APIService();

        setWaitingResponse(true);

        service.refactorCodebase(decompositionName,
            functionalities, Number(dataDependenceThreshold), minimizeSumOfComplexities, Number(timeoutSecs), newRefactor)
            .then(response => {
                setWaitingResponse(false);
                setRefactorizationExists(true);
                handleDataParsing(response.data);
            }).catch(() => {
                setError(true);
                setErrorMessage('ERROR: Failed to request refactorization of the codebase.');
                setWaitingResponse(false);
            }
        );
    }

    const rowEvents = {
        onMouseEnter: (e, row, rowIndex) => {
            document.body.style.cursor = 'pointer';
        },
        onMouseLeave: (e, row, rowIndex) => {
            document.body.style.cursor = 'default';
        }
    };

    const expandRow = {
        renderer: row => (
            <div>
                {row["error"] !== "" ? <div><b>Error:</b> {row["error"]}</div> : ""}

                <div style={
                    {
                        margin: "auto",
                        width: "50%",
                        textAlign: "center",
                    }
                }>
                    {
                        row['status'] === "COMPLETED" ? <BootstrapTable
                                bootstrap4
                                keyField='functionality'
                                data={functionalitiesRedesignRows[row["name"]]}
                                columns={functionalityRedesignColumns}
                                caption={"Saga redesign proposed:"}
                                bordered={false}
                                hover={true}
                                rowEvents={rowEvents}
                        /> : ""
                    }
                </div>

                <br></br>

                <h6><b>Initial monolith decomposition metrics:</b></h6>
                <div>Functionality Redesign Complexity: <b>{row["functionalityComplexity"]}</b></div>
                <div>System Added Complexity: <b>{row["systemComplexity"]}</b></div>
                <div>Cluster Invocations: <b>{row["invocations"]}</b></div>
                <div>Entity Accesses: <b>{row["accesses"]}</b></div>
            </div>
        )
    };

    function renderBreadCrumbs() {
        return (
            <Breadcrumb>
                <Breadcrumb.Item href="/">
                    Home
                </Breadcrumb.Item>
                <Breadcrumb.Item href="/codebases">
                    Codebases
                </Breadcrumb.Item>
                <Breadcrumb.Item href={`/codebases/${codebaseName}`}>
                    {codebaseName}
                </Breadcrumb.Item>
                <Breadcrumb.Item href={`/codebases/${codebaseName}/${strategyName}/similarity`}>
                    {strategyName}
                </Breadcrumb.Item>
                <Breadcrumb.Item href={`/codebases/${codebaseName}/${strategyName}/${similarityName}/decomposition`}>
                    {similarityName}
                </Breadcrumb.Item>
                <Breadcrumb.Item active>
                    {decompositionName}
                </Breadcrumb.Item>
            </Breadcrumb>
        );
    }

    const defaultSorted = [{
        dataField: 'status',
        order: 'asc' // desc or asc
      }];

    return (
        <div>
            <ToastContainer
                position="top-center"
                theme="colored"
            />

            <ModalMessage
                show={error}
                setShow={setError}
                title='Error Message'
                message={errorMessage}
                onClose={closeErrorMessageModal}
            />

            <div style={{ zIndex: 1, left: "2rem", position: "absolute" }}>
                {renderBreadCrumbs()}
            </div>

            <div style={
                {
                    margin: "auto",
                    width: "50%",
                    fontSize: "30px",
                    fontWeight: "600",
                    padding: "10px",
                    paddingBottom: "5px",
                    textAlign: "center",
                }
            }>
                <div><GrainIcon style={{ color: "#007acc", fontSize: 70 }}></GrainIcon></div>
                Refactorization Tool
            </div>
            <div style={
                {
                    margin: "auto",
                    width: "50%",
                    fontSize: "20px",
                    padding: "5px",
                    paddingBottom: "20px",
                    textAlign: "center",
                }
            }>
                Automatically compute microservice Saga orchestrations
            </div>

            <div style={
                {
                    margin: "auto",
                    width: "60%",
                    fontSize: "16px",
                    padding: "5px",
                    paddingBottom: "40px",
                    textAlign: "center",
                }
            }>
                <div style={
                    {
                        margin: "auto",
                        marginBottom: "15px",
                        width: "70%",
                    }
                }><Divider></Divider></div>
                <p>The tool works by creating a Saga with each one of the clusters as orchestrator and then choosing the one that minimizes the migration cost.</p>
<p>During the redesign, the tool inserts one invocation of the orchestrator cluster between each invocation of two other clusters. 
After this, a recursive algorithm is executed, which iterates through each invocation in the callgraph and checks if it can be merged with the previous invocation of the same cluster.</p>
<p>Two invocations can be merged if there are no read accesses in the latest invocations of other clusters before the second one. 

The data dependance scope allows one to configure the maximum invocation distance for which a dependance will be considered. The value 0 considers all invocations 
between the one being analyzed and the last one from the same cluster.</p>
                <div style={
                    {
                        margin: "auto",
                        marginTop: "15px",
                        width: "70%",
                    }
                }><Divider></Divider></div>
            </div>
            <div style={
                {
                    margin: "auto",
                    width: "30%",
                    border: "1px solid grey",
                    borderRadius: "10px",
                    textAlign: "center",
                    padding: "20px"
                }
            }>
                <label>
                    Maximum Data Dependence Distance
                </label>


                <div style={{width: "50%", marginLeft: "25%"}}>
                    <Slider
                        onChange={handleChangeThreshold}
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

                {/* <label>
                    Refactorization timeout (seconds):
                </label>

                <div style={{width: "50%", marginLeft: "25%"}}>
                    <Slider
                        onChange={this.handleChangeTimeout.bind(this)}
                        defaultValue={0}
                        aria-labelledby="continuous-slider"
                        valueLabelDisplay="auto"
                        step={10}
                        min={60}
                        max={1000}
                    />
                </div> */}

                <Button
                    onClick={ () => requestCodebaseRefactor(true) }
                    variant="contained"
                    color="primary"
                    disabled={isLoading}
                >
                    Start
                </Button>
            </div>

            <br></br>

            {waitingResponse &&
                <div style={
                    {
                        margin: "auto",
                        textAlign: "center",
                    }
                }>
                    <CircularProgress />
                </div>
            }

            {refactorizationExists &&
                <div>
                    <Button
                        className="ms-3"
                        onClick={ () => viewCodebaseRefactor() }
                        variant="contained"
                        color="primary"
                    >
                        Update table
                    </Button>

                    <br></br>

                    <BootstrapTable
                        bootstrap4
                        keyField='functionality'
                        data={refactorizationRows}
                        columns={refactorizationColumns}
                        expandRow={ expandRow }
                        caption={"Results of last estimation for the valid Saga functionalities:"}
                        bordered={false}
                        hover={true}
                        defaultSorted={defaultSorted}
                        rowEvents={rowEvents}
                    />
                </div>
            }
        </div>
    )
}
