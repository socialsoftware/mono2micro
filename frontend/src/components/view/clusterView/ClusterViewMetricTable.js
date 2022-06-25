import BootstrapTable from "react-bootstrap-table-next";
import React, {useState} from "react";
import ButtonGroup from "react-bootstrap/ButtonGroup";
import Button from "react-bootstrap/Button";

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

export const ClusterViewMetricTable = ({clusters, clustersFunctionalities}) => {

    const [selectedTable, setSelectedTable] = useState(1);

    const metricsRows = clusters.map(({ id, name, entities, cohesion, coupling, complexity }) => {
        return {
            id: id,
            cluster: name,
            entities: entities.length,
            functionalities: clustersFunctionalities[id] === undefined ? "fetching..." : clustersFunctionalities[id].length,
            cohesion: cohesion,
            coupling: coupling,
            complexity: complexity
        }
    });

    const couplingRows = clusters.map(c1 => {
        return Object.assign({id: c1.name}, ...clusters.map(c2 => {
            return {
                [c2.name]: c1.id === c2.id ? "---" :
                    c1.couplingDependencies[c2.id] === undefined ? 0 :
                        parseFloat(c1.couplingDependencies[c2.id].length / Object.keys(c2.entities).length).toFixed(2)
            }
        }))
    });

    const couplingColumns = [{ dataField: 'id', text: '', style: { fontWeight: 'bold' } }]
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

            {selectedTable === TABLE_TYPE.METRICS &&
                <BootstrapTable
                    bootstrap4
                    keyField='id'
                    data={metricsRows}
                    columns={metricsColumns}
                />
            }

            {selectedTable === TABLE_TYPE.COUPLING &&
                <BootstrapTable
                    bootstrap4
                    keyField='id'
                    data={couplingRows}
                    columns={couplingColumns}
                />
            }
        </>
);
}