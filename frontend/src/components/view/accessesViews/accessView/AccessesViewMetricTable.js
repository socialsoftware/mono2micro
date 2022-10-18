import BootstrapTable from "react-bootstrap-table-next";
import React, {useState} from "react";
import ButtonGroup from "react-bootstrap/ButtonGroup";
import Button from "react-bootstrap/Button";
import CircularProgress from "@mui/material/CircularProgress";
import {MetricType} from "../../../../type-declarations/types";

const metricsColumns = [{
    dataField: 'cluster',
    text: 'Cluster',
    sort: true
}, {
    dataField: 'entities',
    text: 'Entities',
    sort: true
}, {
    dataField: 'functionalities',
    text: 'Functionalities',
    sort: true
}, {
    dataField: 'cohesion',
    text: 'Cohesion',
    sort: true
}, {
    dataField: 'coupling',
    text: 'Coupling',
    sort: true
}, {
    dataField: 'complexity',
    text: 'Complexity',
    sort: true
}];

const TABLE_TYPE = {
    METRICS: 1,
    COUPLING: 2
}

export const AccessesViewMetricTable = ({clusters, clustersFunctionalities, outdated}) => {

    const [selectedTable, setSelectedTable] = useState(1);

    const metricsRows = clusters.map(({ name, elements, metrics }) => {
        return {
            name: name,
            cluster: name,
            entities: elements.length,
            functionalities: clustersFunctionalities[name] === undefined ? "fetching..." : clustersFunctionalities[name].length,
            cohesion: metrics[MetricType.COHESION],
            coupling: metrics[MetricType.COUPLING],
            complexity: metrics[MetricType.COMPLEXITY]
        }
    });

    const couplingRows = clusters.map(c1 => {
        return Object.assign({name: c1.name}, ...clusters.map(c2 => {
            return {
                [c2.name]: c1.name === c2.name ? "---" :
                    c1.couplingDependencies[c2.name] === undefined ? 0 :
                        parseFloat(c1.couplingDependencies[c2.name].length / Object.keys(c2.elements).length).toFixed(2)
            }
        }))
    });

    const couplingColumns = [{ dataField: 'name', text: '', style: { fontWeight: 'bold' } }]
        .concat(clusters.map(c => {
            return {
                dataField: c.name,
                text: c.name
            }
        }));

    return (
        <>
            <ButtonGroup className="mb-3">
                <Button disabled={selectedTable === TABLE_TYPE.METRICS} onClick={() => setSelectedTable(1)}>Metrics</Button>
                <Button disabled={selectedTable === TABLE_TYPE.COUPLING} onClick={() => setSelectedTable(2)}>Inter Coupling Cluster</Button>
            </ButtonGroup>

            {(outdated || Object.keys(clustersFunctionalities).length === 0) &&
                <div style={{ margin: "auto", textAlign: "center"}}>
                    {outdated && <h2>Waiting for metrics and functionalities to be updated...</h2>}
                    <CircularProgress/>
                </div>
            }
            {!outdated && clusters !== 0 && Object.keys(clustersFunctionalities).length !== 0 &&
                <>
                    {selectedTable === TABLE_TYPE.METRICS &&
                        <BootstrapTable
                            bootstrap4
                            keyField='name'
                            data={metricsRows}
                            columns={metricsColumns}
                        />
                    }

                    {selectedTable === TABLE_TYPE.COUPLING &&
                        <BootstrapTable
                        bootstrap4
                        keyField='name'
                        data={couplingRows}
                        columns={couplingColumns}
                        />
                    }
                </>
            }
        </>
    );
}