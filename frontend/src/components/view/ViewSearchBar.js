import Fuse from "fuse.js";
import {useEffect, useState} from "react";
import {Badge} from "react-bootstrap";
import {
    Box, Button, Chip,
    Dialog,
    DialogContent,
    List,
    ListItem,
    ListItemButton,
    ListItemText, Tab, TablePagination, Tabs,
    TextField, Typography
} from "@mui/material";

export const searchType = {
    CLUSTER: "Cluster",
    FUNCTIONALITY: "Functionality",
    ENTITY: "Entity",
}

const options = {
    threshold: 0.4,
    keys: [
        "name",
        "type",
    ]
};

const tagTypes = {
    TYPE: "type",
    ENTITIES: "entities",
    CLUSTER: "cluster",
    FUNC_TYPE: "funcType",
    CLUSTERS: "clusters",
}

export const ViewSearchBar = ({searchItems, openSearch, setOpenSearch, setSearchedItem}) => {
    const [fuse, setFuse] = useState(undefined);
    const [pattern, setPattern] = useState("");
    const [filteredItems, setFilteredItems] = useState([]);
    const [page, setPage] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(10);
    const [startRow, setStartRow] = useState(0);
    const [endRow, setEndRow] = useState(10);

    const [tabsContainer, setTabsContainer] = useState(true);
    const [tagTab, setTagTab] = useState(0);
    const [tags, setTags] = useState(undefined);

    // Setup search fuzzy finder Fuse and create tags
    useEffect(() => {
        if (searchItems) {
            setFuse(new Fuse(searchItems.items, options));
            setTags(searchItems.tags);
        }
    }, [searchItems]);

    function handleAddTag(type, tag) {
        let newTags;
        setTags(prevTags => {
            let availableType = prevTags.available[type];
            let selectedType = prevTags.selected[type];
            availableType = availableType.filter(value => value !== tag);
            selectedType.push(tag);
            prevTags.available[type] = availableType;
            prevTags.selected[type] = selectedType;
            newTags = prevTags;
            return {...prevTags};
        });
        setFilteredItems(filterItemsByTags(newTags, fuse.search(pattern)));
    }

    function handleDeleteTag(type, tag) {
        let newTags;
        setTags(prevTags => {
            let availableType = prevTags.available[type];
            let selectedType = prevTags.selected[type];
            selectedType = selectedType.filter(value => value !== tag);
            availableType.push(tag);
            prevTags.available[type] = availableType;
            prevTags.selected[type] = selectedType;
            newTags = prevTags;
            return {...prevTags};
        });
        setFilteredItems(filterItemsByTags(newTags, fuse.search(pattern)));
    }

    function filterItemsByTags(tags, availableItems) {
        // No item will be filtered if no tags are used
        if (Object.values(tags.selected).find(value => value.length !== 0) === undefined)
            return availableItems;

        // Checks if item contains any of the tags
        return availableItems.filter(item => Object.entries(tags.selected).find(([key, filterTags]) => filterTags.includes(item.item[key])));
    }

    function handleClose() {
        setPage(0);
        setStartRow(0);
        setEndRow(rowsPerPage);

        setPattern("");
        setFilteredItems([])
        setOpenSearch(false);
    }

    function handleChangePattern(pattern) {
        setPage(0);
        setStartRow(0);
        setEndRow(rowsPerPage);

        setPattern(pattern);
        setFilteredItems(filterItemsByTags(tags, fuse.search(pattern)));
    }

    function handleSubmit(item) {
        setPage(0);
        setStartRow(0);
        setEndRow(rowsPerPage);

        setPattern("");
        setFilteredItems([])
        setOpenSearch(false);
        setSearchedItem(item);
    }

    function handleChangePage(event, newPage) {
        setPage(newPage);
        setStartRow(newPage * rowsPerPage);
        setEndRow(newPage * rowsPerPage + rowsPerPage);
    }

    function handleRowsPerPage(event) {
        setRowsPerPage(event.target.value);
        setPage(0);
        setStartRow(0);
        setEndRow(event.target.value);
    }

    function handleOpenTags() {
        setTabsContainer(!tabsContainer);
    }

    function handleChangeTagTab(event, newTagTab) {
        setTagTab(newTagTab);
    }

    function renderSelectedTags() {
        return (
            <>
                {tags !== undefined && tags.selected.type.length !== 0 &&
                    <Box>
                        {tags.selected.type.map((tag, key) => {
                            if (tag === searchType.CLUSTER)
                                return <Chip key={key} sx={{margin: "0.4rem"}} color="success" label={tag} onDelete={() => handleDeleteTag(tagTypes.TYPE, tag)}/>
                            else if (tag === searchType.FUNCTIONALITY)
                                return <Chip key={key} sx={{margin: "0.4rem"}} color="warning" label={tag} onDelete={() => handleDeleteTag(tagTypes.TYPE, tag)}/>
                            else return <Chip key={key} sx={{margin: "0.4rem"}} color="error" label={tag} onDelete={() => handleDeleteTag(tagTypes.TYPE, tag)}/>
                        })}
                    </Box>
                }
                {tags !== undefined && tags.selected.entities.length !== 0 &&
                    <Box>
                        {tags.selected.entities.map((tag, key) => {
                            return <Chip key={key} sx={{margin: "0.4rem"}} color="info" label={"Entities: " + tag} onDelete={() => handleDeleteTag(tagTypes.ENTITIES, tag)}/>
                        })}
                    </Box>
                }
                {tags !== undefined && tags.selected.cluster.length !== 0 &&
                    <Box>
                        {tags.selected.cluster.map((tag, key) => {
                            return <Chip key={key} sx={{margin: "0.4rem"}} color="info" label={"Entities: " + tag} onDelete={() => handleDeleteTag(tagTypes.CLUSTER, tag)}/>
                        })}
                    </Box>
                }
                {tags !== undefined && tags.selected.clusters.length !== 0 &&
                    <Box>
                        {tags.selected.clusters.map((tag, key) => {
                            return <Chip key={key} sx={{margin: "0.4rem"}} label={"Accessed Clusters: " + tag} onDelete={() => handleDeleteTag(tagTypes.CLUSTERS, tag)}/>
                        })}
                    </Box>
                }
                {tags !== undefined && tags.selected.funcType.length !== 0 &&
                    <Box>
                        {tags.selected.funcType.map((tag, key) => {
                            if (tag === "QUERY")
                                return <Chip key={key} sx={{margin: "0.4rem"}} color="info" label={"QUERY"} onDelete={() => handleDeleteTag(tagTypes.FUNC_TYPE, tag)}/>
                            else return <Chip key={key} sx={{margin: "0.4rem"}} color="info" label={"SAGA"} onDelete={() => handleDeleteTag(tagTypes.FUNC_TYPE, tag)}/>
                        })}
                    </Box>
                }
            </>
        );
    }

    return (
        <>
            <Dialog
                open={openSearch}
                onClose={handleClose}
                fullWidth={true}
                scroll="paper"
                maxWidth="lg"
                PaperProps={{ sx: { position: "fixed", top: "0%" } }}
            >
                <DialogContent>
                    <Box className="d-flex align-items-center justify-content-center">
                        <TextField
                            autoFocus
                            autoComplete="off"
                            value={pattern}
                            margin="normal"
                            label="Search Element"
                            helperText="After searching, click in the desired element or use TAB and ENTER to select.
                            Type cluster/entity/functionality to search all of said type. Click in the tags to filter."
                            type="text"
                            variant="standard"
                            onChange={event => {handleChangePattern(event.target.value);}}
                        />
                        <Button className="ms-5" variant="outlined" onClick={handleOpenTags}>Add Tags</Button>
                    </Box>
                    {tabsContainer &&
                        <Box component="div" sx={{border: '1px solid gainsboro', borderRadius: "10px"}}>
                            <Tabs value={tagTab} onChange={handleChangeTagTab} centered>
                                <Tab label="Clusters"/>
                                <Tab label="Entities"/>
                                <Tab label="Functionalities"/>
                            </Tabs>
                            <Box className="d-flex" sx={{bgcolor: "gainsboro", borderRadius: "0px 0px 7px 7px"}}>
                                {tagTab === 0 &&
                                    <Box className="flex-grow-1 m-2" sx={{bgcolor: "white", borderRadius: "10px", maxWidth: "50%"}}>
                                        {tags !== undefined && tags.available.type.includes(searchType.CLUSTER) &&
                                            <Box sx={{margin: "0.8rem"}}>
                                                <Chip color="success" label="Cluster" onClick={() => handleAddTag(tagTypes.TYPE, searchType.CLUSTER)}/>
                                            </Box>
                                        }
                                        <Box sx={{margin: "0.4rem"}}>
                                            {tags !== undefined && tags.available.entities.map((number, key) => {
                                                return <Chip key={key} color="info" sx={{margin: "0.4rem"}} label={"Entities: " + number}
                                                             onClick={() => handleAddTag(tagTypes.ENTITIES, number)}/>
                                            })}
                                        </Box>
                                    </Box>
                                }
                                {tagTab === 1 &&
                                    <Box className="flex-grow-1 m-2" sx={{bgcolor: "white", borderRadius: "10px", maxWidth: "50%"}}>
                                        {tags !== undefined && tags.available.type.includes(searchType.ENTITY) &&
                                            <Box sx={{margin: "0.8rem"}}>
                                                <Chip color="error" label="Entity" onClick={() => handleAddTag(tagTypes.TYPE, searchType.ENTITY)}/>
                                            </Box>
                                        }
                                        <Box sx={{margin: "0.4rem"}}>
                                            {tags !== undefined && tags.available.cluster.map((name, key) => {
                                                return <Chip key={key} color="info" sx={{margin: "0.4rem"}} label={"Cluster: " + name}
                                                             onClick={() => handleAddTag(tagTypes.CLUSTER, name)}/>
                                            })}
                                        </Box>
                                    </Box>
                                }
                                {tagTab === 2 &&
                                    <Box className="flex-grow-1 m-2" sx={{bgcolor: "white", borderRadius: "10px", maxWidth: "50%"}}>
                                        {tags !== undefined && tags.available.type.includes(searchType.FUNCTIONALITY) &&
                                            <Box sx={{margin: "0.8rem"}}>
                                                <Chip color="warning" label="Functionality" onClick={() => handleAddTag(tagTypes.TYPE, searchType.FUNCTIONALITY)}/>
                                            </Box>
                                        }
                                        <Box sx={{margin: "0.4rem"}}>
                                            {tags !== undefined && tags.available.funcType.includes("QUERY") &&
                                                <Chip color="info" sx={{margin: "0.4rem"}} label={"QUERY"} onClick={() => handleAddTag(tagTypes.FUNC_TYPE, "QUERY")}/>
                                            }
                                            {tags !== undefined && tags.available.funcType.includes("SAGA") &&
                                                <Chip color="info" sx={{margin: "0.4rem"}} label={"SAGA"} onClick={() => handleAddTag(tagTypes.FUNC_TYPE, "SAGA")}/>
                                            }
                                        </Box>
                                        <Box sx={{margin: "0.4rem"}}>
                                            {tags !== undefined && tags.available.clusters.map((number, key) => {
                                                return <Chip key={key} sx={{margin: "0.4rem"}} label={"Accessed Clusters: " + number}
                                                             onClick={() => handleAddTag(tagTypes.CLUSTERS, number)}/>
                                            })}
                                        </Box>
                                    </Box>
                                }
                                <Box className="flex-grow-1 m-2" sx={{bgcolor: "white", borderRadius: "10px", maxWidth: "50%"}}>
                                    <Typography variant="h5" gutterBottom component="p" sx={{textAlign: "center", color: "dimgrey", borderBottom: 1, borderColor: 'divider', marginTop: "0.5rem"}}>
                                        Selected Tags
                                    </Typography>
                                    {renderSelectedTags()}
                                </Box>
                            </Box>
                        </Box>
                    }
                    <List dense={true}>
                        {
                            filteredItems.slice(startRow, endRow).map((item, index) =>
                                <ListItem key={index}>
                                    <ListItemButton
                                        style={{paddingTop: 0, paddingBottom: 0}}
                                        onClick={() => {handleSubmit(item.item)}}
                                    >
                                        <ListItemText>
                                            {item.item.name}
                                        </ListItemText>

                                        { item.item.type === searchType.FUNCTIONALITY &&
                                            <>
                                                <Badge className="me-1" bg="warning">Functionality</Badge>
                                                <Badge className="me-1" bg="info">Type: {item.item.funcType}</Badge>
                                                <Badge bg="secondary">Clusters: {item.item.clusters}</Badge>
                                            </>
                                        }
                                        { item.item.type === searchType.CLUSTER &&
                                            <>
                                                <Badge className="me-1" bg="success">Cluster</Badge>
                                                <Badge className="me-1" bg="info">Entities: {item.item.entities}</Badge>
                                                <Badge className="me-1" bg="secondary">Cohesion: {item.item.cohesion}</Badge>
                                                <Badge className="me-1" bg="secondary">Coupling: {item.item.coupling}</Badge>
                                                <Badge bg="secondary">Complexity: {item.item.complexity}</Badge>
                                            </>
                                        }
                                        { item.item.type === searchType.ENTITY &&
                                            <>
                                                <Badge className="me-1" bg="danger">Entity</Badge>
                                                <Badge bg="info">Cluster: {item.item.cluster}</Badge>
                                            </>
                                        }
                                    </ListItemButton>
                                </ListItem>
                            )
                        }
                    </List>
                    {searchItems !== undefined &&
                        <div className="d-flex justify-content-center">
                            <TablePagination
                                component="div"
                                count={filteredItems.length}
                                page={page}
                                rowsPerPage={rowsPerPage}
                                onPageChange={handleChangePage}
                                onRowsPerPageChange={handleRowsPerPage}
                            />
                        </div>
                    }
                </DialogContent>
            </Dialog>
        </>
    );
}