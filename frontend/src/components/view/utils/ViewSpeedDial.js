import {SpeedDial, SpeedDialAction, SpeedDialIcon} from "@mui/material";
import React from "react";

export const ViewSpeedDial = ({actions}) => {
    return (
        <SpeedDial
            ariaLabel="Search Dial"
            sx={{ position: 'fixed', bottom: "2rem", right: "2rem" }}
            icon={<SpeedDialIcon/>}
            direction="up"
        >
            {actions.map((action) => (
                <SpeedDialAction
                    onClick={action.handler}
                    key={action.name}
                    icon={action.icon}
                    tooltipTitle={action.name}
                />
            ))}
        </SpeedDial>
    );
}
