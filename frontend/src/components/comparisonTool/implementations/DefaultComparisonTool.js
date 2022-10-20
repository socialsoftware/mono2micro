import BootstrapTable from "react-bootstrap-table-next";
import ToolkitProvider, {Search} from "react-bootstrap-table2-toolkit";
import React, {useEffect, useContext, useState} from "react";
import AppContext from "../../AppContext";
import {APIService} from "../../../services/APIService";
import MuiAccordion from '@mui/material/Accordion';
import MuiAccordionSummary from '@mui/material/AccordionSummary';
import MuiAccordionDetails from '@mui/material/AccordionDetails';
import ArrowForwardIosSharpIcon from '@mui/icons-material/ArrowForwardIosSharp';
import {Grid, List, ListItem, ListItemText, ListSubheader, Paper, styled, Typography} from "@mui/material";

export const DefaultComparisonTool = ({codebaseName, resultData}) => {
    const context = useContext(AppContext);
    const { translateEntity, updateEntityTranslationFile } = context;
    const [falsePairs, setFalsePairs] = useState([]);

    useEffect(() => {
        const service = new APIService();

        service.getIdToEntity(codebaseName).then(response => {
            updateEntityTranslationFile(response.data);
        }).catch(error => {
            console.log(error);
        });
        if (resultData.falsePairs !== undefined)
            setFalsePairs(resultData.falsePairs);
    }, [resultData]);


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
        text: resultData.decomposition1.name,
        sort: true
    }, {
        dataField: 'e1g2',
        text: resultData.decomposition2.name,
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
        text: resultData.decomposition1.name,
        sort: true
    }, {
        dataField: 'e2g2',
        text: resultData.decomposition2.name,
        sort: true
    }];

    const Accordion = styled((props) => (
        <MuiAccordion disableGutters {...props} />
    ))(({ theme }) => ({
        border: `1px solid ${theme.palette.error}`,
        '&:not(:last-child)': {
            borderBottom: 0,
        },
        '&:before': {
            display: 'none',
        },
    }));

    const AccordionSummary = styled((props) => (
        <MuiAccordionSummary
            expandIcon={<ArrowForwardIosSharpIcon sx={{ fontSize: '0.9rem' }} />}
            {...props}
        />
    ))(({ theme }) => ({
        backgroundColor:'#dee2e6',
        flexDirection: 'row-reverse',
        '& .MuiAccordionSummary-expandIconWrapper.Mui-expanded': {
            transform: 'rotate(90deg)',
        },
        '& .MuiAccordionSummary-content': {
            marginLeft: theme.spacing(1),
        },
    }));

    const AccordionDetails = styled(MuiAccordionDetails)(({ theme }) => ({
        backgroundColor: '#e9ecef',
        padding: theme.spacing(2),
        borderTop: '1px solid rgba(0, 0, 0, .125)',
    }));

    const ItemCorner = styled(Paper)(({ theme }) => ({backgroundColor: '#3498db', ...theme.typography.body2, padding: theme.spacing(1), textAlign: 'center', }));

    const ItemTop = styled(Paper)(({ theme }) => ({ ...theme.typography.body2, padding: theme.spacing(1), textAlign: 'center', }));

    const Item = styled(Paper)(({ theme }) => ({ backgroundColor: '#dee2e6', ...theme.typography.body2, padding: theme.spacing(1), textAlign: 'center', }));

    const { SearchBar } = Search;

    return (
        <div>
            {Object.keys(resultData).length !== 0 &&
                <>
                    <h4 style={{ color: "#666666" }}> Metrics </h4>

                    <div>
                        <Accordion>
                            <AccordionSummary>Statistics</AccordionSummary>
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
                                    <Grid item xs={2}> <Item>{resultData.accuracy}</Item> </Grid>
                                    <Grid item xs={2}> <Item>{resultData.precision}</Item> </Grid>
                                    <Grid item xs={2}> <Item>{resultData.recall}</Item> </Grid>
                                    <Grid item xs={2}> <Item>{resultData.specificity}</Item> </Grid>
                                    <Grid item xs={2}> <Item>{resultData.fmeasure}</Item> </Grid>
                                    <Grid item xs={1}> </Grid>
                                </Grid>
                                <Grid container spacing={1}>
                                    <Grid item xs={4}> <ItemCorner>Property</ItemCorner> </Grid>
                                    <Grid item xs={4}> <ItemTop>{resultData.decomposition1.name}</ItemTop> </Grid>
                                    <Grid item xs={4}> <ItemTop>{resultData.decomposition2.name}</ItemTop> </Grid>

                                    <Grid item xs={4}> <ItemCorner>Largest Cluster Size:</ItemCorner> </Grid>
                                    <Grid item xs={4}> <Item>{Object.values(resultData.decomposition1.clusters).reduce((p, c) => p > c.elements.length? p : c.elements.length, 0)}</Item> </Grid>
                                    <Grid item xs={4}> <Item>{Object.values(resultData.decomposition2.clusters).reduce((p, c) => p > c.elements.length? p : c.elements.length, 0)}</Item> </Grid>

                                    <Grid item xs={4}> <ItemCorner>Smallest Cluster Size:</ItemCorner> </Grid>
                                    <Grid item xs={4}> <Item>{Object.values(resultData.decomposition1.clusters).reduce((p, c) => p < c.elements.length? p : c.elements.length, Object.values(resultData.decomposition2.clusters)[0].elements.length)}</Item> </Grid>
                                    <Grid item xs={4}> <Item>{Object.values(resultData.decomposition2.clusters).reduce((p, c) => p < c.elements.length? p : c.elements.length, Object.values(resultData.decomposition2.clusters)[0].elements.length)}</Item> </Grid>

                                    <Grid item xs={4}> <ItemCorner>Number of Singleton Clusters:</ItemCorner> </Grid>
                                    <Grid item xs={4}> <Item>{Object.values(resultData.decomposition1.clusters).reduce((p, c) => c.elements.length === 1? p + 1: p, 0)}</Item> </Grid>
                                    <Grid item xs={4}> <Item>{Object.values(resultData.decomposition2.clusters).reduce((p, c) => c.elements.length === 1? p + 1: p, 0)}</Item> </Grid>

                                    {Object.entries(resultData.decomposition1.metrics).map(([metricName, metric]) => {
                                        const decompositionMetric = resultData.decomposition2.metrics[metricName];
                                        return (<React.Fragment key={metricName}>
                                            <Grid item xs={4}> <ItemCorner>{metricName}:</ItemCorner> </Grid>
                                            <Grid item xs={4}> <Item>{metric}</Item> </Grid>
                                            <Grid item xs={4}> <Item>{decompositionMetric !== undefined? decompositionMetric : ""} {decompositionMetric === undefined && <i>Not Present</i>}</Item> </Grid>
                                        </React.Fragment>);
                                    })}
                                    {Object.entries(resultData.decomposition2.metrics).map(([metricName, metric]) => {
                                        if (Object.keys(resultData.decomposition1.metrics).includes(metricName))
                                            return undefined;
                                        return (<React.Fragment key={metricName}>
                                            <Grid item xs={4}> <ItemCorner>{metricName}:</ItemCorner> </Grid>
                                            <Grid item xs={4}> <Item><i>Not Present</i></Item> </Grid>
                                            <Grid item xs={4}> <Item>{metric}</Item> </Grid>
                                        </React.Fragment>);
                                    })}
                                </Grid>
                            </AccordionDetails>
                        </Accordion>
                        <Accordion>
                            <AccordionSummary>
                                <Typography>MojoFM</Typography>
                            </AccordionSummary>
                            <AccordionDetails>
                                <Grid container spacing={1} sx={{marginBottom: "2rem"}}>
                                    <Grid item xs={3}> <ItemCorner>Mojo Common Entities</ItemCorner> </Grid>
                                    <Grid item xs={3}> <ItemCorner>Mojo Biggest Cluster</ItemCorner> </Grid>
                                    <Grid item xs={3}> <ItemCorner>Mojo New Cluster</ItemCorner> </Grid>
                                    <Grid item xs={3}> <ItemCorner>Mojo Singletons</ItemCorner> </Grid>

                                    <Grid item xs={3}> <Item>{resultData.mojoCommon}</Item> </Grid>
                                    <Grid item xs={3}> <Item>{resultData.mojoBiggest}</Item> </Grid>
                                    <Grid item xs={3}> <Item>{resultData.mojoNew}</Item> </Grid>
                                    <Grid item xs={3}> <Item>{resultData.mojoSingletons}</Item> </Grid>
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

                                    <Grid item xs={3}> <Item>{resultData.truePositive}</Item> </Grid>
                                    <Grid item xs={3}> <Item>{resultData.trueNegative}</Item> </Grid>
                                    <Grid item xs={3}> <Item>{resultData.falsePositive}</Item> </Grid>
                                    <Grid item xs={3}> <Item>{resultData.falseNegative}</Item> </Grid>
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
                        <Accordion>
                            <AccordionSummary>Decomposition Differences</AccordionSummary>
                            <AccordionDetails>
                                <div>
                                    {Object.entries(resultData.decomposition1.clusters).map(([clusterName, cluster1]) => {
                                        if (resultData.decomposition2.clusters[clusterName] === undefined)
                                            return  <Accordion disabled={true} key={clusterName}>
                                                <AccordionSummary>{clusterName} does not exist in {resultData.decomposition2.name}</AccordionSummary>
                                            </Accordion>
                                        else {
                                            let cluster2 = resultData.decomposition2.clusters[clusterName];
                                            let includedEntities = [], notIncludedEntities = [];
                                            {cluster1.elements.map(entity => {
                                                if (cluster2.elements.find(e => e.id === entity.id))
                                                    includedEntities.push(entity.name);
                                                else notIncludedEntities.push(entity.name + " (belong to " + Object.values(resultData.decomposition2.clusters).find(c => c.elements.find(e => e.id === entity.id)).name + ")");
                                            })}
                                            return  <Accordion key={clusterName}>
                                                <AccordionSummary>{clusterName}</AccordionSummary>
                                                <AccordionDetails className={"d-flex flex-grow-1 w-100"}>
                                                    <List
                                                        sx={{
                                                            width: '100%',
                                                            bgcolor: "background.grey",
                                                            position: 'relative',
                                                            overflow: 'auto',
                                                            '& ul': { padding: 0 },
                                                        }}
                                                    >
                                                        <ListSubheader sx={{fontSize: "25px", backgroundColor: "#07bc0c22"}}>Entities Belonging to the Same Cluster in Both Decompositions</ListSubheader>
                                                        {includedEntities.map(e => <ListItem key={e}><ListItemText>{e}</ListItemText></ListItem>)}
                                                    </List>
                                                    <List
                                                        sx={{
                                                            width: '100%',
                                                            bgcolor: "background.grey",
                                                            position: 'relative',
                                                            overflow: 'auto',
                                                            '& ul': { padding: 0 },
                                                        }}
                                                    >
                                                        <ListSubheader sx={{fontSize: "25px", backgroundColor: "#e74c3c22"}}>Entities Missing From the Second Decomposition's Clusters</ListSubheader>
                                                        {notIncludedEntities.map(e => <ListItem key={e}><ListItemText>{e}</ListItemText></ListItem>)}
                                                    </List>
                                                </AccordionDetails>
                                            </Accordion>
                                        }
                                    })}
                                    {Object.keys(resultData.decomposition2.clusters).map(clusterName => {
                                        if (resultData.decomposition1.clusters[clusterName] === undefined)
                                            return  <Accordion disabled={true} key={clusterName}>
                                                <AccordionSummary>{clusterName} does not exist in {resultData.decomposition1.name}</AccordionSummary>
                                            </Accordion>
                                        else return undefined;
                                    })}
                                </div>
                            </AccordionDetails>
                        </Accordion>
                    </div>
                </>
            }
        </div>
    )
}
