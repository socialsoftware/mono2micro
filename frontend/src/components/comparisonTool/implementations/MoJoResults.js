import {Grid, Typography} from "@mui/material";
import React, {useContext, useEffect, useState} from "react";
import ToolkitProvider, {Search} from "react-bootstrap-table2-toolkit/dist/react-bootstrap-table2-toolkit";
import BootstrapTable from "react-bootstrap-table-next";
import AppContext from "../../AppContext";
import {APIService} from "../../../services/APIService";
import {Accordion, AccordionDetails, AccordionSummary, Item, ItemCorner} from "../ComparisonTool";

export const MoJoResults = ({codebaseName, comparisonData}) => {
    const context = useContext(AppContext);
    const { translateEntity, updateEntityTranslationFile } = context;
    const [falsePairs, setFalsePairs] = useState([]);
    const [moJoResults, setMoJoResults] = useState({});

    const falsePairRows = falsePairs.map((falsePair, id) => {
        return {
            id: id,
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
        text: comparisonData.decomposition1.name,
        sort: true
    }, {
        dataField: 'e1g2',
        text: comparisonData.decomposition2.name,
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
        text: comparisonData.decomposition1.name,
        sort: true
    }, {
        dataField: 'e2g2',
        text: comparisonData.decomposition2.name,
        sort: true
    }];


    useEffect(() => {
        const service = new APIService();
        const moJoResults = comparisonData.analysisList.find(results => results.type === "MOJO");
        setMoJoResults(moJoResults);

        service.getIdToEntity(codebaseName).then(response => {
            updateEntityTranslationFile(response.data);
        }).catch(error => {
            console.log(error);
        });
        if (moJoResults.falsePairs !== undefined)
            setFalsePairs(moJoResults.falsePairs);
    }, [comparisonData]);

    const { SearchBar } = Search;

    return (
        <>
            <div>
                <Accordion>
                    <AccordionSummary>
                        <Typography>MojoFM</Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                        <Grid container spacing={1} sx={{marginBottom: "2rem"}}>
                            <Grid item xs={1}> </Grid>
                            <Grid item xs={2}> <ItemCorner>Accuracy</ItemCorner> </Grid>
                            <Grid item xs={2}> <ItemCorner>Precision</ItemCorner> </Grid>
                            <Grid item xs={2}> <ItemCorner>Recall</ItemCorner> </Grid>
                            <Grid item xs={2}> <ItemCorner>Specificity</ItemCorner> </Grid>
                            <Grid item xs={2}> <ItemCorner>F-score</ItemCorner> </Grid>
                            <Grid item xs={1}> </Grid>

                            <Grid item xs={1}> </Grid>
                            <Grid item xs={2}> <Item>{moJoResults.accuracy}</Item> </Grid>
                            <Grid item xs={2}> <Item>{moJoResults.precision}</Item> </Grid>
                            <Grid item xs={2}> <Item>{moJoResults.recall}</Item> </Grid>
                            <Grid item xs={2}> <Item>{moJoResults.specificity}</Item> </Grid>
                            <Grid item xs={2}> <Item>{moJoResults.fmeasure}</Item> </Grid>
                            <Grid item xs={1}> </Grid>
                        </Grid>
                        <Grid container spacing={1} sx={{marginBottom: "2rem"}}>
                            <Grid item xs={3}> <ItemCorner>Mojo Common Entities</ItemCorner> </Grid>
                            <Grid item xs={3}> <ItemCorner>Mojo Biggest Cluster</ItemCorner> </Grid>
                            <Grid item xs={3}> <ItemCorner>Mojo New Cluster</ItemCorner> </Grid>
                            <Grid item xs={3}> <ItemCorner>Mojo Singletons</ItemCorner> </Grid>

                            <Grid item xs={3}> <Item>{moJoResults.mojoCommon}</Item> </Grid>
                            <Grid item xs={3}> <Item>{moJoResults.mojoBiggest}</Item> </Grid>
                            <Grid item xs={3}> <Item>{moJoResults.mojoNew}</Item> </Grid>
                            <Grid item xs={3}> <Item>{moJoResults.mojoSingletons}</Item> </Grid>
                        </Grid>
                    </AccordionDetails>
                </Accordion>
                <Accordion>
                    <AccordionSummary>
                        <Typography>Entity Pairs</Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                        <Grid container spacing={1} sx={{marginBottom: "2rem"}}>
                            <Grid item xs={3}> <ItemCorner>True Positives</ItemCorner> </Grid>
                            <Grid item xs={3}> <ItemCorner>True Negatives</ItemCorner> </Grid>
                            <Grid item xs={3}> <ItemCorner>False Positives</ItemCorner> </Grid>
                            <Grid item xs={3}> <ItemCorner>False Negatives</ItemCorner> </Grid>

                            <Grid item xs={3}> <Item>{moJoResults.truePositive}</Item> </Grid>
                            <Grid item xs={3}> <Item>{moJoResults.trueNegative}</Item> </Grid>
                            <Grid item xs={3}> <Item>{moJoResults.falsePositive}</Item> </Grid>
                            <Grid item xs={3}> <Item>{moJoResults.falseNegative}</Item> </Grid>
                        </Grid>
                    </AccordionDetails>
                </Accordion>
                <Accordion>
                    <AccordionSummary>False Pairs</AccordionSummary>
                    <AccordionDetails>
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
                    </AccordionDetails>
                </Accordion>
            </div>
        </>
    )
}
