import BootstrapTable from "react-bootstrap-table-next";
import ToolkitProvider, {Search} from "react-bootstrap-table2-toolkit";
import React, {useEffect, useContext, useState} from "react";
import AppContext from "../../AppContext";
import {RepositoryService} from "../../../services/RepositoryService";
import MuiAccordion from '@mui/material/Accordion';
import MuiAccordionSummary from '@mui/material/AccordionSummary';
import MuiAccordionDetails from '@mui/material/AccordionDetails';
import ArrowForwardIosSharpIcon from '@mui/icons-material/ArrowForwardIosSharp';
import {Grid, List, ListItem, ListItemText, ListSubheader, Paper, styled, Typography} from "@mui/material";

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
                                <Typography>Accuracy: {resultData.accuracy}</Typography>
                                <Typography>Precision: {resultData.precision}</Typography>
                                <Typography>Recall: {resultData.recall}</Typography>
                                <Typography>Specificity: {resultData.specificity}</Typography>
                                <Typography>F-score: {resultData.fmeasure}</Typography>
                                <Grid container spacing={1}>
                                    <Grid item xs={4}> <ItemCorner>Property</ItemCorner> </Grid>
                                    <Grid item xs={4}> <ItemTop>{decomposition1.name}</ItemTop> </Grid>
                                    <Grid item xs={4}> <ItemTop>{decomposition2.name}</ItemTop> </Grid>

                                    <Grid item xs={4}> <ItemCorner>Silhouette Score:</ItemCorner> </Grid>
                                    <Grid item xs={4}> <Item>{decomposition1.silhouetteScore}</Item> </Grid>
                                    <Grid item xs={4}> <Item>{decomposition2.silhouetteScore}</Item> </Grid>

                                    <Grid item xs={4}> <ItemCorner>Largest Cluster Size:</ItemCorner> </Grid>
                                    <Grid item xs={4}> <Item>{Object.values(decomposition1.clusters).reduce((p, c) => p > c.entities.length? p : c.entities.length, 0)}</Item> </Grid>
                                    <Grid item xs={4}> <Item>{Object.values(decomposition2.clusters).reduce((p, c) => p > c.entities.length? p : c.entities.length, 0)}</Item> </Grid>

                                    <Grid item xs={4}> <ItemCorner>Smallest Cluster Size:</ItemCorner> </Grid>
                                    <Grid item xs={4}> <Item>{Object.values(decomposition1.clusters).reduce((p, c) => p < c.entities.length? p : c.entities.length, Object.values(decomposition2.clusters)[0].entities.length)}</Item> </Grid>
                                    <Grid item xs={4}> <Item>{Object.values(decomposition2.clusters).reduce((p, c) => p < c.entities.length? p : c.entities.length, Object.values(decomposition2.clusters)[0].entities.length)}</Item> </Grid>

                                    <Grid item xs={4}> <ItemCorner>Number of Singleton Clusters:</ItemCorner> </Grid>
                                    <Grid item xs={4}> <Item>{Object.values(decomposition1.clusters).reduce((p, c) => c.entities.length === 1? p + 1: p, 0)}</Item> </Grid>
                                    <Grid item xs={4}> <Item>{Object.values(decomposition2.clusters).reduce((p, c) => c.entities.length === 1? p + 1: p, 0)}</Item> </Grid>

                                    {Object.entries(decomposition1.metrics).map(([metricName, metric]) =>
                                        <React.Fragment key={metricName}>
                                            <Grid item xs={4}> <ItemCorner>{metricName}:</ItemCorner> </Grid>
                                            <Grid item xs={4}> <Item>{metric}</Item> </Grid>
                                            <Grid item xs={4}> <Item>{decomposition2.metrics[metricName]}</Item> </Grid>
                                        </React.Fragment>
                                    )}
                                </Grid>
                            </AccordionDetails>
                        </Accordion>
                        <Accordion>
                            <AccordionSummary>
                                <Typography>Mojo </Typography>
                            </AccordionSummary>
                            <AccordionDetails>
                                <Typography>Mojo Common Entities: {resultData.mojoCommon}</Typography>
                                <Typography>Mojo Biggest Cluster: {resultData.mojoBiggest}</Typography>
                                <Typography>Mojo New Cluster: {resultData.mojoNew}</Typography>
                                <Typography>Mojo Singletons: {resultData.mojoSingletons}</Typography>
                            </AccordionDetails>
                        </Accordion>
                        <Accordion>
                            <AccordionSummary>
                                <Typography>Entity Pairs</Typography>
                            </AccordionSummary>
                            <AccordionDetails>
                                <Typography>True Positives: {resultData.truePositive}</Typography>
                                <Typography>True Negatives: {resultData.trueNegative}</Typography>
                                <Typography>False Positives: {resultData.falsePositive}</Typography>
                                <Typography>False Negatives: {resultData.falseNegative}</Typography>
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
                                    {Object.entries(decomposition1.clusters).map(([clusterName, cluster1]) => {
                                        if (decomposition2.clusters[clusterName] === undefined)
                                            return  <Accordion disabled={true} key={clusterName}>
                                                <AccordionSummary>{clusterName} does not exist in {decomposition2.name}</AccordionSummary>
                                            </Accordion>
                                        else {
                                            let cluster2 = decomposition2.clusters[clusterName];
                                            let includedEntities = [], notIncludedEntities = [];
                                            {cluster1.entities.map(entity => {
                                                if (cluster2.entities.includes(entity))
                                                    includedEntities.push(translateEntity(entity));
                                                else notIncludedEntities.push(translateEntity(entity) + " (belong to " + Object.values(decomposition2.clusters).find(c => c.entities.includes(entity)).name + ")");
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
                                                        {includedEntities.map(e => <ListItem key={e}><ListItemText>{translateEntity(e)}</ListItemText></ListItem>)}
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
                                                        {notIncludedEntities.map(e => <ListItem key={e}><ListItemText>{translateEntity(e)}</ListItemText></ListItem>)}
                                                    </List>
                                                </AccordionDetails>
                                            </Accordion>
                                        }
                                    })}
                                    {Object.keys(decomposition2.clusters).map(clusterName => {
                                        if (decomposition1.clusters[clusterName] === undefined)
                                            return  <Accordion disabled={true} key={clusterName}>
                                                <AccordionSummary>{clusterName} does not exist in {decomposition1.name}</AccordionSummary>
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
