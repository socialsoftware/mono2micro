import Fuse from "fuse.js";
import React, {useContext, useEffect, useState} from "react";
import {
    Box,
    Dialog,
    DialogContent,
    TextField,
} from "@mui/material";
import BootstrapTable from "react-bootstrap-table-next";
import filterFactory, {numberFilter, selectFilter, textFilter} from "react-bootstrap-table2-filter";
import paginationFactory from "react-bootstrap-table2-paginator";
import ButtonGroup from "react-bootstrap/ButtonGroup";
import Button from "react-bootstrap/Button";
import {RepositoryService} from "../../services/RepositoryService";
import {useParams} from "react-router-dom";
import AppContext from "../AppContext";

export const searchType = {
    CLUSTER: "Cluster",
    FUNCTIONALITY: "Functionality",
    ENTITY: "Entity",
}

const fuseOptions = {
    threshold: 0.4,
    keys: [ "name" ]
};

const nFilter = numberFilter({placeholder: "Filter"});
const tFilter = textFilter({placeholder: "Filter"});
const sFilter1 = selectFilter({placeholder:"Select", options: {"Cluster": "Cluster", "Entity": "Entity", "Functionality": "Functionality"}});
const sFilter2 = selectFilter({placeholder:"Select", options: {"QUERY": "QUERY", "SAGA": "SAGA"}});
const sort = true;

const itemInfo = [
    {dataField: 'name',         text: 'Name'                                    , sort},
    {dataField: 'type',         text: 'Type',                   filter: sFilter1, sort},
    {dataField: 'funcType',     text: 'Functionality Type',     filter: sFilter2, sort},
    {dataField: 'cluster',      text: 'Belongs to Cluster',     filter: tFilter , sort},
    {dataField: 'entities',     text: 'Entities',               filter: nFilter , sort},
];

export const ViewSearchBar = ({openSearch, setOpenSearch, setSearchedItem}) => {
    let { decompositionName } = useParams();
    const context = useContext(AppContext);
    const { translateEntity } = context;
    const [fuse, setFuse] = useState(undefined);
    const [pattern, setPattern] = useState("");
    const [filteredItems, setFilteredItems] = useState([]);
    const [searchItems, setSearchItems] = useState(undefined);

    useEffect(() => {
        if (openSearch)
            updateSearchItems();
    }, [openSearch]);

    function updateSearchItems() {
        const service = new RepositoryService();
        service.getSearchItems(decompositionName).then(response => {
            const newSearchItems = response.data.map(item => {
                if (item.type === searchType.ENTITY) // Add translation to entities
                    item.name = translateEntity(Number(item.id));
                return item;
            })
            setSearchItems(newSearchItems);
        }).catch(error => {
            console.error("Error during search items fetch:", error);
        });
    }

    useEffect(() => {
        if (!openSearch) // remove cursor
            document.body.style.cursor = 'default';
    }, [openSearch]);

    const sizePerPageRenderer = ({options, currSizePerPage, onSizePerPageChange}) => (
        <ButtonGroup>
            {
                options.map((option) => {
                    const isSelect = currSizePerPage === `${option.page}`;
                    return (
                        <Button
                            key={ option.text }
                            onClick={ () => onSizePerPageChange(option.page) }
                            disabled={isSelect}
                        >
                            { option.text }
                        </Button>
                    );
                })
            }
        </ButtonGroup>
    );

    const pagination = paginationFactory({
        sizePerPageRenderer,
        page: 2,
        sizePerPageList: [
            {text: '10', value: 10},
            {text: '20', value: 20},
            {text: '50', value: 50},
            {text: '100', value: 100},
            {text: 'All', value: searchItems? searchItems.length : 200},
        ],
        sizePerPage: 10,
        lastPageText: '>>',
        firstPageText: '<<',
        nextPageText: '>',
        prePageText: '<',
        showTotal: true,
        alwaysShowAllBtns: true,
    });

    // Setup search fuzzy finder Fuse and create tags
    useEffect(() => {
        if (searchItems) {
            setFuse(new Fuse(searchItems, fuseOptions));
            setFilteredItems(searchItems);
        }
    }, [searchItems]);

    function filterItems(pattern) {
        if (pattern === "")
            return searchItems;

        return fuse.search(pattern).map(item => item.item);
    }

    function handleClose() {
        setPattern("");
        setFilteredItems(searchItems)
        setOpenSearch(false);
    }

    function handleChangePattern(pattern) {
        setPattern(pattern);
        setFilteredItems(filterItems(pattern));
    }

    const rowEvents = {
        onClick: (e, row, rowIndex) => {
            handleSubmit(row);
        },
        onMouseEnter: (e, row, rowIndex) => {
            document.body.style.cursor = 'pointer';
        },
        onMouseLeave: (e, row, rowIndex) => {
            document.body.style.cursor = 'default';
        }
    };

    function handleSubmit(item) {
        document.body.style.cursor = 'default';
        setPattern("");
        setFilteredItems(searchItems);
        setOpenSearch(false);
        setSearchedItem(item);
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
                            helperText="Click in the desired row to search the element."
                            type="text"
                            variant="standard"
                            fullWidth={true}
                            onChange={event => {handleChangePattern(event.target.value);}}
                        />
                    </Box>
                    <BootstrapTable
                        bootstrap4
                        keyField='id'
                        hover={true}
                        data={filteredItems}
                        columns={itemInfo}
                        pagination={pagination}
                        filter={filterFactory()}
                        rowEvents={rowEvents}
                    />
                </DialogContent>
            </Dialog>
        </>
    );
}