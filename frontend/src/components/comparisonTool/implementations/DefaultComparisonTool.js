import React from "react";
import {Grid, List, ListItem, ListItemText, ListSubheader} from "@mui/material";
import {Accordion, AccordionDetails, AccordionSummary, Item, ItemCorner, ItemTop} from "../ComparisonTool";

export const DefaultComparisonTool = ({comparisonData}) => {

    return (
        <div>
            {Object.keys(comparisonData).length !== 0 &&
                <>
                    <div>
                        <Accordion>
                            <AccordionSummary>Statistics</AccordionSummary>
                            <AccordionDetails>
                                <Grid container spacing={1}>
                                    <Grid item xs={4}> <ItemCorner>Property</ItemCorner> </Grid>
                                    <Grid item xs={4}> <ItemTop>{comparisonData.decomposition1.name}</ItemTop> </Grid>
                                    <Grid item xs={4}> <ItemTop>{comparisonData.decomposition2.name}</ItemTop> </Grid>

                                    <Grid item xs={4}> <ItemCorner>Largest Cluster Size:</ItemCorner> </Grid>
                                    <Grid item xs={4}> <Item>{Object.values(comparisonData.decomposition1.clusters).reduce((p, c) => p > c.elements.length? p : c.elements.length, 0)}</Item> </Grid>
                                    <Grid item xs={4}> <Item>{Object.values(comparisonData.decomposition2.clusters).reduce((p, c) => p > c.elements.length? p : c.elements.length, 0)}</Item> </Grid>

                                    <Grid item xs={4}> <ItemCorner>Smallest Cluster Size:</ItemCorner> </Grid>
                                    <Grid item xs={4}> <Item>{Object.values(comparisonData.decomposition1.clusters).reduce((p, c) => p < c.elements.length? p : c.elements.length, Object.values(comparisonData.decomposition2.clusters)[0].elements.length)}</Item> </Grid>
                                    <Grid item xs={4}> <Item>{Object.values(comparisonData.decomposition2.clusters).reduce((p, c) => p < c.elements.length? p : c.elements.length, Object.values(comparisonData.decomposition2.clusters)[0].elements.length)}</Item> </Grid>

                                    <Grid item xs={4}> <ItemCorner>Number of Singleton Clusters:</ItemCorner> </Grid>
                                    <Grid item xs={4}> <Item>{Object.values(comparisonData.decomposition1.clusters).reduce((p, c) => c.elements.length === 1? p + 1: p, 0)}</Item> </Grid>
                                    <Grid item xs={4}> <Item>{Object.values(comparisonData.decomposition2.clusters).reduce((p, c) => c.elements.length === 1? p + 1: p, 0)}</Item> </Grid>

                                    {Object.entries(comparisonData.decomposition1.metrics).map(([metricName, metric]) => {
                                        const decompositionMetric = comparisonData.decomposition2.metrics[metricName];
                                        return (<React.Fragment key={metricName}>
                                            <Grid item xs={4}> <ItemCorner>{metricName}:</ItemCorner> </Grid>
                                            <Grid item xs={4}> <Item>{metric}</Item> </Grid>
                                            <Grid item xs={4}> <Item>{decompositionMetric !== undefined? decompositionMetric : ""} {decompositionMetric === undefined && <i>Not Present</i>}</Item> </Grid>
                                        </React.Fragment>);
                                    })}
                                    {Object.entries(comparisonData.decomposition2.metrics).map(([metricName, metric]) => {
                                        if (Object.keys(comparisonData.decomposition1.metrics).includes(metricName))
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
                            <AccordionSummary>Decomposition Differences</AccordionSummary>
                            <AccordionDetails>
                                <div>
                                    {Object.entries(comparisonData.decomposition1.clusters).map(([clusterName, cluster1]) => {
                                        if (comparisonData.decomposition2.clusters[clusterName] === undefined)
                                            return  <Accordion disabled={true} key={clusterName}>
                                                <AccordionSummary>{clusterName} does not exist in {comparisonData.decomposition2.name}</AccordionSummary>
                                            </Accordion>
                                        else {
                                            let cluster2 = comparisonData.decomposition2.clusters[clusterName];
                                            let includedEntities = [], notIncludedEntities = [];
                                            {cluster1.elements.map(entity => {
                                                if (cluster2.elements.find(e => e.id === entity.id))
                                                    includedEntities.push(entity.name);
                                                else notIncludedEntities.push(entity.name + " (belong to " + Object.values(comparisonData.decomposition2.clusters).find(c => c.elements.find(e => e.id === entity.id)).name + ")");
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
                                    {Object.keys(comparisonData.decomposition2.clusters).map(clusterName => {
                                        if (comparisonData.decomposition1.clusters[clusterName] === undefined)
                                            return  <Accordion disabled={true} key={clusterName}>
                                                <AccordionSummary>{clusterName} does not exist in {comparisonData.decomposition1.name}</AccordionSummary>
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
