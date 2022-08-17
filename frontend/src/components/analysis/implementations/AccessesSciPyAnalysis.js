import BootstrapTable from "react-bootstrap-table-next";
import ToolkitProvider, {Search} from "react-bootstrap-table2-toolkit";
import React, {useEffect, useContext, useState} from "react";
import AppContext from "../../AppContext";
import {RepositoryService} from "../../../services/RepositoryService";

const metricsColumns = [
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
];

export const AccessesSciPyAnalysis = ({codebaseName, resultData, decomposition1, decomposition2}) => {
    const context = useContext(AppContext);
    const { translateEntity, updateEntityTranslationFile } = context;
    const [falsePairs, setFalsePairs] = useState([]);

    useEffect(() => {
        const service = new RepositoryService();

        service.getIdToEntity(codebaseName).then(response => {
            updateEntityTranslationFile(response.data);
        }).catch(error => {
            console.log(error);
        });
        if (resultData.falsePairs !== undefined)
            setFalsePairs(resultData.falsePairs);
    }, [resultData]);


    const falsePairRows = falsePairs.map(falsePair => {
        return {
            id: falsePair[0] + falsePair[3],
            e1: translateEntity(falsePair[0]),
            e1g1: falsePair[1],
            e1g2: falsePair[2],
            e2: translateEntity(falsePair[3]),
            e2g1: falsePair[4],
            e2g2: falsePair[5]
        }
    });

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

    const { SearchBar } = Search;

    return (
        <>
            {Object.keys(resultData).length !== 0 &&
                <>
                    <h4 style={{ color: "#666666" }}> Metrics </h4>
                    <BootstrapTable
                        keyField='id'
                        bootstrap4
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
                        columns={metricsColumns}
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
