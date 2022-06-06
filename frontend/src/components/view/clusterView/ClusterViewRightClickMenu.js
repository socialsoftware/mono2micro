import {ListGroup, ListGroupItem} from "react-bootstrap";
import {types} from "../Views";
import {OPERATION} from "../../../constants/constants";
import {
    AllOut, BubbleChart,
    CallMerge,
    CallSplit,
    Cancel,
    DriveFileRenameOutline,
    Hub,
    MoveDown, Restore, Save,
    Visibility,
    VisibilityOff
} from "@mui/icons-material";
import React from "react";

export const ClusterViewRightClickMenu = ({ menuCoordinates, operations,
                                              handleExpandCluster,
                                              handleRename,
                                              handleOnlyNeighbours,
                                              handleCollapseCluster,
                                              handleShowAll,
                                              handleTogglePhysics,
                                              handleTransfer,
                                              handleTransferEntity,
                                              handleMerge,
                                              handleSplit,
                                              handleSave,
                                              handleCancel,
                                        }) => {
    return (
        <>
            {menuCoordinates !== undefined &&
                <ListGroup
                    style={{
                        zIndex: 1,
                        top: menuCoordinates.top,
                        left: menuCoordinates.left,
                        position: "absolute"
                    }}
                >
                    {menuCoordinates.type === types.CLUSTER &&
                        <>
                            {operations.includes(OPERATION.EXPAND) && <ListGroupItem action onClick={handleExpandCluster}><AllOut/>Expand Cluster</ListGroupItem>}
                            {operations.includes(OPERATION.RENAME) && <ListGroupItem action onClick={handleRename}><DriveFileRenameOutline/>Rename Cluster</ListGroupItem>}
                            {operations.includes(OPERATION.SPLIT) && <ListGroupItem action onClick={handleSplit}><CallSplit/>Split</ListGroupItem>}
                            {operations.includes(OPERATION.MERGE) && <ListGroupItem action onClick={handleMerge}><CallMerge/>Merge</ListGroupItem>}
                            {operations.includes(OPERATION.TRANSFER) && <ListGroupItem action onClick={handleTransfer}><MoveDown/>Transfer Entities</ListGroupItem>}
                            {operations.includes(OPERATION.ONLY_NEIGHBOURS) && <ListGroupItem action onClick={handleOnlyNeighbours}><VisibilityOff/>Only Show Neighbours</ListGroupItem>}
                            {operations.includes(OPERATION.SHOW_ALL) && <ListGroupItem action onClick={handleShowAll}><Visibility/>Show All</ListGroupItem>}
                        </>
                    }
                    {menuCoordinates.type === types.ENTITY &&
                        <>
                            {operations.includes(OPERATION.TRANSFER_ENTITY) && <ListGroupItem action onClick={handleTransferEntity}><MoveDown/>Transfer Entity</ListGroupItem>}
                            {operations.includes(OPERATION.COLLAPSE) && <ListGroupItem action onClick={handleCollapseCluster}><Hub/>Collapse Into Cluster</ListGroupItem>}
                            {operations.includes(OPERATION.ONLY_NEIGHBOURS) && <ListGroupItem action onClick={handleOnlyNeighbours}><VisibilityOff/>Only Show Neighbours</ListGroupItem>}
                            {operations.includes(OPERATION.SHOW_ALL) && <ListGroupItem action onClick={handleShowAll}><Visibility/>Show All</ListGroupItem>}
                        </>
                    }
                    {menuCoordinates.type === types.EDGE &&
                        <>
                            {operations.includes(OPERATION.SAVE) && <ListGroupItem action onClick={handleSave}><Save/>Save Graph Positions</ListGroupItem>}
                            {operations.includes(OPERATION.RESTORE) && <ListGroupItem action onClick={handleShowAll}><Restore/>Restore Graph Positions</ListGroupItem>}
                            {operations.includes(OPERATION.SHOW_ALL) && <ListGroupItem action onClick={handleShowAll}><Visibility/>Show All</ListGroupItem>}
                            {operations.includes(OPERATION.TOGGLE_PHYSICS) && <ListGroupItem action onClick={handleTogglePhysics}><BubbleChart/>Toggle Physics</ListGroupItem>}
                        </>
                    }
                    {menuCoordinates.type === types.NONE &&
                        <>
                            {operations.includes(OPERATION.SAVE) && <ListGroupItem action onClick={handleSave}><Save/>Save Graph Positions</ListGroupItem>}
                            {operations.includes(OPERATION.RESTORE) && <ListGroupItem action onClick={handleShowAll}><Restore/>Restore Graph Positions</ListGroupItem>}
                            {operations.includes(OPERATION.SHOW_ALL) && <ListGroupItem action onClick={handleShowAll}><Visibility/>Show All</ListGroupItem>}
                            {operations.includes(OPERATION.TOGGLE_PHYSICS) && <ListGroupItem action onClick={handleTogglePhysics}><BubbleChart/>Toggle Physics</ListGroupItem>}
                        </>
                    }
                    {operations.includes(OPERATION.CANCEL) && <ListGroupItem action onClick={handleCancel}><Cancel/>Cancel Operation</ListGroupItem>}
                </ListGroup>
            }
        </>
    );
}
