import {ListGroup, ListGroupItem} from "react-bootstrap";
import {types} from "../Views";
import {OPERATION} from "../../../../constants/constants";
import {
    AirlineStops,
    AllOut,
    CallMerge,
    CallSplit,
    Cancel, DeviceHub,
    DriveFileRenameOutline,
    Hub, LocalSee,
    MoveDown, Restore, Save,
    Visibility,
    VisibilityOff, Workspaces
} from "@mui/icons-material";
import React from "react";
import {toast} from "react-toastify";

export const ClusterViewRightClickMenu = ({ menuCoordinates, operations,
                                              handleExpandCluster,
                                              handleExpandAll,
                                              handleRenameRequest,
                                              handleOnlyNeighbours,
                                              handleCollapseCluster,
                                              handleCollapseAll,
                                              handleShowAll,
                                              handleTogglePhysics,
                                              handleTransferRequest,
                                              handleMergeRequest,
                                              handleSplitRequest,
                                              handleFormClusterRequest,
                                              handleSnapshot,
                                              handleSave,
                                              handleCancel,
                                        }) => {

    function addProgressMessage(message) {
        let toastId;
        switch (message) {
            case OPERATION.EXPAND:
                toastId = toast.loading("Processing entity nodes...", {type: toast.TYPE.INFO});
                setTimeout(() => { handleExpandCluster(); toast.dismiss(toastId); }, 50);
                break;
            case OPERATION.COLLAPSE:
                toastId = toast.loading("Processing cluster node...", {type: toast.TYPE.INFO});
                setTimeout(() => { handleCollapseCluster(); toast.dismiss(toastId); }, 50);
                break;
            case OPERATION.EXPAND_ALL:
                toastId = toast.loading("Processing entity nodes...", {type: toast.TYPE.INFO});
                setTimeout(() => { handleExpandAll(); toast.dismiss(toastId); }, 50);
                break;
            case OPERATION.COLLAPSE_ALL:
                toastId = toast.loading("Processing cluster nodes...", {type: toast.TYPE.INFO});
                setTimeout(() => { handleCollapseAll(); toast.dismiss(toastId); }, 50);
                break;
            case OPERATION.ONLY_NEIGHBOURS:
                toastId = toast.loading("Hiding nodes...", {type: toast.TYPE.INFO});
                setTimeout(() => { handleOnlyNeighbours(); toast.dismiss(toastId); }, 50);
                break;
            case OPERATION.SHOW_ALL:
                toastId = toast.loading("Showing all nodes...", {type: toast.TYPE.INFO});
                setTimeout(() => { handleShowAll("showAll"); toast.dismiss(toastId); }, 100);
                break;
            case OPERATION.RESTORE:
                toastId = toast.loading("Restoring all nodes...", {type: toast.TYPE.INFO});
                setTimeout(() => { handleShowAll("restore"); toast.dismiss(toastId); }, 50);
                break;
        }
    }

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
                            {operations.includes(OPERATION.EXPAND) && <ListGroupItem action onClick={() => addProgressMessage(OPERATION.EXPAND)}><AllOut/>Expand Cluster</ListGroupItem>}
                            {operations.includes(OPERATION.RENAME) && <ListGroupItem action onClick={handleRenameRequest}><DriveFileRenameOutline/>Rename Cluster</ListGroupItem>}
                            {operations.includes(OPERATION.SPLIT) && <ListGroupItem action onClick={handleSplitRequest}><CallSplit/>Split</ListGroupItem>}
                            {operations.includes(OPERATION.MERGE) && <ListGroupItem action onClick={handleMergeRequest}><CallMerge/>Merge</ListGroupItem>}
                            {operations.includes(OPERATION.TRANSFER) && <ListGroupItem action onClick={handleTransferRequest}><MoveDown/>Transfer Entities</ListGroupItem>}
                            {operations.includes(OPERATION.ONLY_NEIGHBOURS) && <ListGroupItem action onClick={() => addProgressMessage(OPERATION.ONLY_NEIGHBOURS)}><VisibilityOff/>Only Show Neighbours</ListGroupItem>}
                            {operations.includes(OPERATION.SHOW_ALL) && <ListGroupItem action onClick={() => addProgressMessage(OPERATION.SHOW_ALL)}><Visibility/>Show All</ListGroupItem>}
                        </>
                    }
                    {menuCoordinates.type === types.ENTITY &&
                        <>
                            {operations.includes(OPERATION.COLLAPSE) && <ListGroupItem action onClick={() => addProgressMessage(OPERATION.COLLAPSE)}><Hub/>Collapse Into Cluster</ListGroupItem>}
                            {operations.includes(OPERATION.FORM_CLUSTER) && <ListGroupItem action onClick={handleFormClusterRequest}><DeviceHub/>Form Cluster</ListGroupItem>}
                            {operations.includes(OPERATION.ONLY_NEIGHBOURS) && <ListGroupItem action onClick={() => addProgressMessage(OPERATION.ONLY_NEIGHBOURS)}><VisibilityOff/>Only Show Neighbours</ListGroupItem>}
                            {operations.includes(OPERATION.SHOW_ALL) && <ListGroupItem action onClick={() => addProgressMessage(OPERATION.SHOW_ALL)}><Visibility/>Show All</ListGroupItem>}
                        </>
                    }
                    {menuCoordinates.type === types.EDGE &&
                        <>
                            {operations.includes(OPERATION.SNAPSHOT) && <ListGroupItem action onClick={handleSnapshot}><LocalSee/>Snapshot Decomposition</ListGroupItem>}
                            {operations.includes(OPERATION.SAVE) && <ListGroupItem action onClick={handleSave}><Save/>Save Graph Positions</ListGroupItem>}
                            {operations.includes(OPERATION.RESTORE) && <ListGroupItem action onClick={() => addProgressMessage(OPERATION.RESTORE)}><Restore/>Restore Graph Positions</ListGroupItem>}
                            {operations.includes(OPERATION.SHOW_ALL) && <ListGroupItem action onClick={() => addProgressMessage(OPERATION.SHOW_ALL)}><Visibility/>Show All</ListGroupItem>}
                            {operations.includes(OPERATION.EXPAND_ALL) && <ListGroupItem action onClick={() => addProgressMessage(OPERATION.EXPAND_ALL)}><AllOut/>Expand All</ListGroupItem>}
                            {operations.includes(OPERATION.COLLAPSE_ALL) && <ListGroupItem action onClick={() => addProgressMessage(OPERATION.COLLAPSE_ALL)}><Workspaces/>Collapse All</ListGroupItem>}
                            {operations.includes(OPERATION.TOGGLE_PHYSICS) && <ListGroupItem action onClick={handleTogglePhysics}><AirlineStops/>Toggle Physics</ListGroupItem>}
                        </>
                    }
                    {menuCoordinates.type === types.NONE &&
                        <>
                            {operations.includes(OPERATION.SNAPSHOT) && <ListGroupItem action onClick={handleSnapshot}><LocalSee/>Snapshot Decomposition</ListGroupItem>}
                            {operations.includes(OPERATION.SAVE) && <ListGroupItem action onClick={handleSave}><Save/>Save Graph Positions</ListGroupItem>}
                            {operations.includes(OPERATION.RESTORE) && <ListGroupItem action onClick={() => addProgressMessage(OPERATION.RESTORE)}><Restore/>Restore Graph Positions</ListGroupItem>}
                            {operations.includes(OPERATION.SHOW_ALL) && <ListGroupItem action onClick={() => addProgressMessage(OPERATION.SHOW_ALL)}><Visibility/>Show All</ListGroupItem>}
                            {operations.includes(OPERATION.EXPAND_ALL) && <ListGroupItem action onClick={() => addProgressMessage(OPERATION.EXPAND_ALL)}><AllOut/>Expand All</ListGroupItem>}
                            {operations.includes(OPERATION.COLLAPSE_ALL) && <ListGroupItem action onClick={() => addProgressMessage(OPERATION.COLLAPSE_ALL)}><Workspaces/>Collapse All</ListGroupItem>}
                            {operations.includes(OPERATION.TOGGLE_PHYSICS) && <ListGroupItem action onClick={handleTogglePhysics}><AirlineStops/>Toggle Physics</ListGroupItem>}
                        </>
                    }
                    {menuCoordinates.type === types.MULTIPLE &&
                        <>
                            {operations.includes(OPERATION.FORM_CLUSTER) && <ListGroupItem action onClick={handleFormClusterRequest}><DeviceHub/>Form Cluster</ListGroupItem>}
                        </>
                    }
                    {operations.includes(OPERATION.CANCEL) && <ListGroupItem action onClick={handleCancel}><Cancel/>Cancel Operation</ListGroupItem>}
                </ListGroup>
            }
        </>
    );
}
