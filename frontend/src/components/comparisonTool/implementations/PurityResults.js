import React from "react";
import { Grid, Typography } from "@mui/material";
import { Accordion, AccordionDetails, AccordionSummary, Item } from "../ComparisonTool";

export const PurityResults = ({ clusterPurityMap = {}, clusterMapping = {}, decomposition1 = {}, decomposition2 = {}, purity = {} }) => {
    // Transform clusters data into the expected format
    const expertClusters = Object.keys(decomposition1.clusters || {}).reduce((acc, clusterName) => {
        const elements = decomposition1.clusters[clusterName].elements || [];
        acc[clusterName] = elements.map(element => element.name);
        return acc;
    }, {});

    const proposedClusters = Object.keys(decomposition2.clusters || {}).reduce((acc, clusterName) => {
        const elements = decomposition2.clusters[clusterName].elements || [];
        acc[clusterName] = elements.map(element => element.name);
        return acc;
    }, {});

    // Function to compute intersection
    const getCommonEntities = (proposedEntities, expertEntities) => {
        const proposedSet = new Set(proposedEntities);
        const expertSet = new Set(expertEntities);
        return [...proposedSet].filter(entity => expertSet.has(entity));
    };

    return (
        <div>
            <Accordion>
                <AccordionSummary>
                    <Typography>Purity Results</Typography>
                </AccordionSummary>
                <AccordionDetails>
                    <div style={{ marginBottom: '1rem' }}>
                        <Typography variant="h6">Overall Cluster Purity: {(purity || 0).toFixed(2)}</Typography>
                    </div>
                    <Grid container spacing={2} sx={{ marginBottom: "2rem" }}>
                        <Grid item xs={2}>
                            <Item>Cluster</Item>
                        </Grid>
                        <Grid item xs={2}>
                            <Item>Purity</Item>
                        </Grid>
                        <Grid item xs={2}>
                            <Item>Mapped to Expert Cluster</Item>
                        </Grid>
                        <Grid item xs={2}>
                            <Item>Entities in Proposed Cluster</Item>
                        </Grid>
                        <Grid item xs={2}>
                            <Item>Entities in Expert Cluster</Item>
                        </Grid>
                        <Grid item xs={2}>
                            <Item>Entities in Common</Item>
                        </Grid>
                        {Object.keys(clusterPurityMap).map(clusterName => {
                            const proposedEntities = proposedClusters[clusterName] || [];
                            const expertClusterName = clusterMapping[clusterName];
                            const expertEntities = expertClusters[expertClusterName] || [];
                            const commonEntities = getCommonEntities(proposedEntities, expertEntities);

                            return (
                                <React.Fragment key={clusterName}>
                                    <Grid item xs={2}>
                                        <Item>{clusterName}</Item>
                                    </Grid>
                                    <Grid item xs={2}>
                                        <Item>{(clusterPurityMap[clusterName] || 0).toFixed(2)}</Item>
                                    </Grid>
                                    <Grid item xs={2}>
                                        <Item>{expertClusterName}</Item>
                                    </Grid>
                                    <Grid item xs={2}>
                                        <Item>{proposedEntities.join(', ')}</Item>
                                    </Grid>
                                    <Grid item xs={2}>
                                        <Item>{expertEntities.join(', ')}</Item>
                                    </Grid>
                                    <Grid item xs={2}>
                                        <Item>{commonEntities.join(', ')}</Item>
                                    </Grid>
                                </React.Fragment>
                            );
                        })}
                    </Grid>
                </AccordionDetails>
            </Accordion>
        </div>
    );
};
