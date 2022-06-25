import {Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, TextField} from "@mui/material";
import Button from "@mui/material/Button";
import BootstrapTable from "react-bootstrap-table-next";
import {useEffect, useState} from "react";
import {ArrowRightAlt} from "@mui/icons-material";

export const DIALOG_TYPE = {
    RENAME: 0,
    TRANSFER: 1,
    TRANSFER_ENTITY: 2,
    MERGE: 3,
    SPLIT: 4,
    FORM_CLUSTER: 5,
}

export const ClusterViewDialogs = ({requestDialog, setDialogResponse, handleCancel}) => {

    const [name, setName] = useState('');
    const [selectedEntities, setSelectedEntities] = useState([]);
    const [textError, setTextError] = useState(false);

    useEffect(() => {
        setTextError(false);
    }, [requestDialog]);

    useEffect(() => {
        if (name !== '')
            setTextError(false);
    }, [name]);

    function handleChangeName(newName) {
        if (name.length !== 0 && newName.length === 0)
            setTextError(true);
        setName(newName);
    }

    function handleRename() {
        if (name !== '')
            setDialogResponse({type: DIALOG_TYPE.RENAME, newName: name});
        else setTextError(true);
    }

    function handleTransfer() {
        if (selectedEntities.selectionContext.selected.length !== 0) {
            if (requestDialog.entities.length === selectedEntities.selectionContext.selected.length)
                setDialogResponse({type: DIALOG_TYPE.MERGE, newName: requestDialog.toCluster});
            else setDialogResponse({type: DIALOG_TYPE.TRANSFER, entities: selectedEntities.selectionContext.selected});
        }
    }

    function handleTransferEntity() {
        setDialogResponse({type: DIALOG_TYPE.TRANSFER_ENTITY});
    }

    function handleMerge() {
        if (name !== '')
            setDialogResponse({type: DIALOG_TYPE.MERGE, newName: name});
        else setTextError(true);
    }

    function handleSplit() {
        if (name !== '') {
            if (selectedEntities.selectionContext.selected.length !== 0) {
                if (requestDialog.entities.length === selectedEntities.selectionContext.selected.length)
                    setDialogResponse({ type: DIALOG_TYPE.RENAME, newName: name });
                setDialogResponse({ type: DIALOG_TYPE.SPLIT, newName: name, entities: selectedEntities.selectionContext.selected });
            } else handleCancel();
        }
        else setTextError(true);
    }

    function handleFormCluster() {
        if (name !== '') {
            if (selectedEntities.selectionContext.selected.length !== 0) {
                let entities = {};
                selectedEntities.selectionContext.selected.forEach(selectedEntity => {
                    const entity = requestDialog.entities.find(entity => entity.id === selectedEntity);
                    let clusterEntities = entities[entity.cid];
                    if (clusterEntities)
                        clusterEntities.push(entity.id);
                    else entities[entity.cid] = [entity.id];
                });
                setDialogResponse({type: DIALOG_TYPE.FORM_CLUSTER, newName: name, entities});
            }
        }
        else setTextError(true);
    }

    return (
        <>
            <Dialog open={requestDialog.type === DIALOG_TYPE.RENAME} onClose={handleCancel}>
                <DialogContent>
                    <DialogTitle>Rename Cluster {requestDialog.clusterName}</DialogTitle>
                    <DialogContentText>
                        This operation will change the name of cluster {requestDialog.clusterName} to the
                        submitted name.
                        Keep in mind that an unused name should be inserted.
                    </DialogContentText>
                    <TextField
                        autoFocus
                        error={textError}
                        margin="normal"
                        id="name"
                        label="New name"
                        type="text"
                        fullWidth
                        variant="standard"
                        onChange={event => handleChangeName(event.target.value)}
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCancel}>Cancel</Button>
                    <Button onClick={handleRename}>Rename</Button>
                </DialogActions>
            </Dialog>

            <Dialog open={requestDialog.type === DIALOG_TYPE.TRANSFER} onClose={handleCancel}>
                <DialogContent>
                    <DialogTitle>Transfer Entities From Cluster {requestDialog.fromCluster} <ArrowRightAlt/> {requestDialog.toCluster}</DialogTitle>
                    <DialogContentText className={"mb-2"}>
                        This operation will transfer the entities from cluster {requestDialog.fromCluster} to
                        cluster {requestDialog.toCluster}.
                        Select the checkboxes of the entities to be transferred.
                    </DialogContentText>
                    <BootstrapTable
                        keyField={'id'}
                        hover={true}
                        data={requestDialog.entities}
                        columns={[{
                            dataField: 'name',
                            text: 'Entity Name',
                            sort: true
                        }]}
                        selectRow={{mode: 'checkbox', clickToSelect: true, hideSelectAll: true}}
                        ref={n => setSelectedEntities(n)}
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCancel}>Cancel</Button>
                    <Button onClick={handleTransfer}>Transfer</Button>
                </DialogActions>
            </Dialog>

            <Dialog open={requestDialog.type === DIALOG_TYPE.TRANSFER_ENTITY} onClose={handleCancel}>
                <DialogContent>
                    <DialogTitle>Transfer entity {requestDialog.entityName} <ArrowRightAlt/> {requestDialog.toCluster}</DialogTitle>
                    <DialogContentText className={"mb-2"}>
                        This operation will transfer entity {requestDialog.entityName} to
                        cluster {requestDialog.toCluster}.
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCancel}>Cancel</Button>
                    <Button onClick={handleTransferEntity}>Transfer Entity</Button>
                </DialogActions>
            </Dialog>

            <Dialog open={requestDialog.type === DIALOG_TYPE.MERGE} onClose={handleCancel}>
                <DialogContent>
                    <DialogTitle>Merge Cluster {requestDialog.fromCluster} + {requestDialog.toCluster}</DialogTitle>
                    <DialogContentText className={"mb-2"}>
                        This operation will merge cluster {requestDialog.fromCluster} with
                        cluster {requestDialog.toCluster}.
                        Attribute a name to the merged cluster.
                    </DialogContentText>
                    <TextField
                        autoFocus
                        error={textError}
                        margin="normal"
                        id="name"
                        label="Merged cluster's name"
                        type="text"
                        fullWidth
                        variant="standard"
                        onChange={event => handleChangeName(event.target.value)}
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCancel}>Cancel</Button>
                    <Button onClick={handleMerge}>Merge</Button>
                </DialogActions>
            </Dialog>

            <Dialog open={requestDialog.type === DIALOG_TYPE.SPLIT} onClose={handleCancel}>
                <DialogContent>
                    <DialogTitle>Split cluster {requestDialog.cluster}</DialogTitle>
                    <DialogContentText className={"mb-2"}>
                        This operation will split cluster {requestDialog.cluster} into a new cluster.
                        Attribute a name to the new cluster and select the entities to be placed in the new cluster.
                    </DialogContentText>
                    <TextField
                        autoFocus
                        error={textError}
                        margin="normal"
                        id="name"
                        label="New cluster"
                        type="text"
                        fullWidth
                        variant="standard"
                        onChange={event => handleChangeName(event.target.value)}
                    />
                    <BootstrapTable
                        keyField={'id'}
                        hover={true}
                        data={requestDialog.entities}
                        columns={[{
                            dataField: 'name',
                            text: 'Entity Name',
                            sort: true
                        }]}
                        selectRow={{mode: 'checkbox', clickToSelect: true, hideSelectAll: true}}
                        ref={n => setSelectedEntities(n)}
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCancel}>Cancel</Button>
                    <Button onClick={handleSplit}>Split</Button>
                </DialogActions>
            </Dialog>

            <Dialog open={requestDialog.type === DIALOG_TYPE.FORM_CLUSTER} onClose={handleCancel}>
                <DialogContent>
                    <DialogTitle>Form new cluster</DialogTitle>
                    <DialogContentText className={"mb-2"}>
                        This operation will form a new cluster.
                        Attribute a name to the new cluster and select the entities to be placed in the new cluster.
                    </DialogContentText>
                    <TextField
                        autoFocus
                        error={textError}
                        margin="normal"
                        id="name"
                        label="New cluster"
                        type="text"
                        fullWidth
                        variant="standard"
                        onChange={event => handleChangeName(event.target.value)}
                    />
                    <BootstrapTable
                        keyField={'id'}
                        hover={true}
                        data={requestDialog.entities}
                        columns={[
                            { dataField: 'name', text: 'Entity Name', sort: true },
                            { dataField: 'cluster', text: 'Cluster Name', sort: true }
                        ]}
                        selectRow={{mode: 'checkbox', clickToSelect: true}}
                        ref={n => setSelectedEntities(n)}
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCancel}>Cancel</Button>
                    <Button onClick={handleFormCluster}>Create</Button>
                </DialogActions>
            </Dialog>
        </>
    );
}