import React, {Fragment, useEffect, useState} from "react";
import Form from "react-bootstrap/Form";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import DropdownButton from "react-bootstrap/DropdownButton";
import Dropdown from "react-bootstrap/Dropdown";
import FormControl from "react-bootstrap/FormControl";
import {RepositoryService} from "../../../services/RepositoryService";
import {SourceType} from "../../../models/sources/Source";


export const AccessesSciPyForm = ({strategy, setStrategy}) => {

    const [profiles, setProfiles] = useState([]);

    // Executes when it is informed that there is information to be updated
    useEffect(() => {
        loadProfiles();
    }, [])

    //function loadDecompositions() {
    //    const service = new RepositoryService();
    //    service.getCodebaseDecompositions(
    //        strategy.codebaseName,
    //        StrategyType.ACCESSES_SCIPY,
    //        [
    //            "name",
    //            "strategyName",
    //            "clusters",
    //            "silhouetteScore",
    //            "cohesion",
    //            "coupling",
    //            "complexity",
    //            "performance"
    //        ]
    //    ).then((response) => {
    //        if (response.data !== null) {
    //            setAllDecompositions(response.data);
    //        }
    //    });
    //}

    function loadProfiles() {
        const service = new RepositoryService();
        service.getSource(strategy.codebaseName, SourceType.ACCESSES)
            .then((response) => setProfiles(response.profiles));
    }


    //const decompositionColumns = [
    //    { dataField: 'strategy',          text: 'Strategy Name',                 sort: true },
    //    { dataField: 'decomposition',     text: 'Decomposition',                 sort: true },
    //    { dataField: 'clusters',          text: 'Number of Retrieved Clusters',  sort: true },
    //    { dataField: 'singleton',         text: 'Number of Singleton Clusters',  sort: true },
    //    { dataField: 'max_cluster_size',  text: 'Maximum Cluster Size',          sort: true },
    //    { dataField: 'ss',                text: 'Silhouette Score',              sort: true },
    //    { dataField: 'cohesion',          text: 'Cohesion',                      sort: true },
    //    { dataField: 'coupling',          text: 'Coupling',                      sort: true },
    //    { dataField: 'complexity',        text: 'Complexity',                    sort: true },
    //    { dataField: 'performance',       text: 'Performance',                   sort: true }
    //];

    //const decompositionRows = allDecompositions.map(decomposition => {
    //    let amountOfSingletonClusters = 0;
    //    let maxClusterSize = 0;

    //    Object.values(decomposition.clusters).forEach(c => {
    //        const numberOfEntities = c.entities.length;

    //        if (numberOfEntities === 1) amountOfSingletonClusters++;

    //        if (numberOfEntities > maxClusterSize) maxClusterSize = numberOfEntities;
    //    })

    //    return {
    //        id: decomposition.strategyName + decomposition.name,
    //        strategy: decomposition.strategyName,
    //        decomposition: decomposition.name,
    //        clusters: Object.keys(decomposition.clusters).length,
    //        singleton: amountOfSingletonClusters,
    //        max_cluster_size: maxClusterSize,
    //        ss: decomposition.silhouetteScore,
    //        cohesion: decomposition.cohesion,
    //        coupling: decomposition.coupling,
    //        complexity: decomposition.complexity,
    //        performance: decomposition.performance
    //    }
    //});

    function handleChangeAccessMetricWeight(event) {
        let newStrategy = strategy.copy();
        newStrategy.accessMetricWeight = Number(event.target.value);
        setStrategy(newStrategy);
    }

    function handleChangeWriteMetricWeight(event) {
        let newStrategy = strategy.copy();
        newStrategy.writeMetricWeight = Number(event.target.value);
        setStrategy(newStrategy);
    }

    function handleChangeReadMetricWeight(event) {
        let newStrategy = strategy.copy();
        newStrategy.readMetricWeight = Number(event.target.value);
        setStrategy(newStrategy);
    }

    function handleChangeSequenceMetricWeight(event) {
        let newStrategy = strategy.copy();
        newStrategy.sequenceMetricWeight = Number(event.target.value);
        setStrategy(newStrategy);
    }

    function handleChangeLinkageType(event) {
        let newStrategy = strategy.copy();
        newStrategy.linkageType = event.target.id;
        setStrategy(newStrategy);
    }

    function handleChangeProfile(profile) {
        let newStrategy = strategy.copy();
        newStrategy.profile = profile;
        setStrategy(newStrategy);
    }

    function handleChangeTracesMaxLimit(event) {
        let newStrategy = strategy.copy();
        newStrategy.tracesMaxLimit = Number(event.target.value);
        setStrategy(newStrategy);
    }

    function handleChangeTraceType(event) {
        let newStrategy = strategy.copy();
        newStrategy.traceType = event.target.value;
        setStrategy(newStrategy);
    }

    return (
        <Fragment>
            <h4 className="mb-3 mt-3" style={{ color: "#666666" }}>
                Create Strategy
            </h4>
            <Form.Group as={Row} controlId="selectControllerProfiles" className="align-items-center mb-3">
                <Form.Label column sm={2}>
                    Select Codebase Profile
                </Form.Label>
                <Col sm={2}>
                    <DropdownButton title={strategy.profile === ""? 'Controller Profiles': strategy.profile}>
                        {profiles !== [] && Object.keys(profiles).map(profile =>
                            <Dropdown.Item
                                key={profile}
                                onClick={() => handleChangeProfile(profile)}
                                active={strategy.profile === profile}
                            >
                                {profile}
                            </Dropdown.Item>
                        )}
                    </DropdownButton>
                </Col>
            </Form.Group>
            <Form.Group as={Row} controlId="amountOfTraces" className="mb-3">
                <Form.Label column sm={2}>
                    Amount of Traces per Controller
                </Form.Label>
                <Col sm={2}>
                    <FormControl
                        type="number"
                        placeholder="0 by default"
                        value={strategy.tracesMaxLimit === 0? '' : strategy.tracesMaxLimit}
                        onChange={handleChangeTracesMaxLimit}

                    />
                    <Form.Text className="text-muted">
                        If no number is inserted, 0 is assumed to be the default value meaning the maximum number of traces
                    </Form.Text>
                </Col>
            </Form.Group>
            <Form.Group as={Row} className="align-items-center mb-3">
                <Form.Label as="legend" column sm={2}>
                    Type of traces
                </Form.Label>
                <Col sm={3} style={{ paddingLeft: 0 }}>
                    <Col sm="auto">
                        <Form.Check
                            onClick={handleChangeTraceType}
                            name="traceType"
                            label="All"
                            type="radio"
                            id="allTraces"
                            value="ALL"
                            defaultChecked
                        />
                    </Col>
                    <Col sm="auto">
                        <Form.Check
                            onClick={handleChangeTraceType}
                            name="traceType"
                            label="Longest"
                            type="radio"
                            id="longest"
                            value="LONGEST"
                        />
                    </Col>
                    <Col sm="auto">
                        <Form.Check
                            onClick={handleChangeTraceType}
                            name="traceType"
                            label="With more different accesses"
                            type="radio"
                            id="withMoreDifferentTraces"
                            value="WITH_MORE_DIFFERENT_ACCESSES"
                        />

                    </Col>
                    <Col sm="auto">
                        <Form.Check
                            onClick={handleChangeTraceType}
                            name="traceType"
                            label="Representative (set of accesses)"
                            type="radio"
                            id="representativeSetOfAccesses"
                            value="REPRESENTATIVE"
                        />
                    </Col>
                    {/* WIP */}
                    <Col sm="auto">
                        <Form.Check
                            onClick={undefined}
                            name="traceType"
                            label="Representative (subsequence of accesses)"
                            type="radio"
                            id="complete"
                            value="?"
                            disabled
                        />
                    </Col>
                </Col>
            </Form.Group>
            <Form.Group as={Row} className="align-items-center mb-3">
                <Form.Label as="legend" column sm={2}>
                    Linkage Type
                </Form.Label>
                <Col sm="auto">
                    <Form.Check
                        onClick={handleChangeLinkageType}
                        name="linkageType"
                        label="Average"
                        type="radio"
                        id="average"
                        defaultChecked
                    />
                </Col>
                <Col sm="auto">
                    <Form.Check
                        onClick={handleChangeLinkageType}
                        name="linkageType"
                        label="Single"
                        type="radio"
                        id="single"
                    />

                </Col>
                <Col sm="auto">
                    <Form.Check
                        onClick={handleChangeLinkageType}
                        name="linkageType"
                        label="Complete"
                        type="radio"
                        id="complete"
                    />
                </Col>
            </Form.Group>

            <Form.Group as={Row} controlId="access" className="align-items-center mb-3">
                <Form.Label column sm={2}>
                    Access Metric Weight (%)
                </Form.Label>
                <Col sm={2}>
                    <FormControl
                        type="number"
                        placeholder="0-100"
                        value={strategy.accessMetricWeight === 0? '': strategy.accessMetricWeight}
                        onChange={handleChangeAccessMetricWeight} />
                </Col>
            </Form.Group>

            <Form.Group as={Row} controlId="write" className="align-items-center mb-3">
                <Form.Label column sm={2}>
                    Write Metric Weight (%)
                </Form.Label>
                <Col sm={2}>
                    <FormControl
                        type="number"
                        placeholder="0-100"
                        value={strategy.writeMetricWeight === 0? '': strategy.writeMetricWeight}
                        onChange={handleChangeWriteMetricWeight} />
                </Col>
            </Form.Group>

            <Form.Group as={Row} controlId="read" className="align-items-center mb-3">
                <Form.Label column sm={2}>
                    Read Metric Weight (%)
                </Form.Label>
                <Col sm={2}>
                    <FormControl
                        type="number"
                        placeholder="0-100"
                        value={strategy.readMetricWeight === 0? '': strategy.readMetricWeight}
                        onChange={handleChangeReadMetricWeight} />
                </Col>
            </Form.Group>

            <Form.Group as={Row} controlId="sequence" className="align-items-center mb-3">
                <Form.Label column sm={2}>
                    Sequence Metric Weight (%)
                </Form.Label>
                <Col sm={2}>
                    <FormControl
                        type="number"
                        placeholder="0-100"
                        value={strategy.sequenceMetricWeight === 0? '': strategy.sequenceMetricWeight}
                        onChange={handleChangeSequenceMetricWeight} />
                </Col>
            </Form.Group>
        </Fragment>
    );

        //function placeholder() {
        //    return <h4 style={{ color: "#666666" }}>
        //            Decompositions
        //        </h4>

        //        { //allDecompositions.length > 0 && <BootstrapTable bootstrap4 keyField='id' data={decompositionRows} columns={decompositionColumns} />
        //        }
        //    </Row>
        //}
}