import React from "react";
import { Grid, Typography } from "@mui/material";
import { Accordion, AccordionDetails, AccordionSummary, Item } from "../ComparisonTool";

export const PurityResults = ({ purity }) => {
    return (
        <div>
            <Accordion>
                <AccordionSummary>
                    <Typography>Purity Results</Typography>
                </AccordionSummary>
                <AccordionDetails>
                    <Grid container spacing={1} sx={{ marginBottom: "2rem" }}>
                        <Grid item xs={4}>
                            <Item>Cluster Purity</Item>
                        </Grid>
                        <Grid item xs={8}>
                            <Item>{purity}</Item>
                        </Grid>
                    </Grid>
                </AccordionDetails>
            </Accordion>
        </div>
    );
};
