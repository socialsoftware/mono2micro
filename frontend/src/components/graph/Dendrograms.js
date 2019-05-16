import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { Nav, ButtonGroup, Button, Card, CardDeck, CardGroup, Breadcrumb, BreadcrumbItem, Table } from 'react-bootstrap';
import { DENDROGRAM_URL } from '../../constants/constants';
import BootstrapTable from 'react-bootstrap-table-next';

const BreadCrumbs = () => {
    return (
      <div>
        <Breadcrumb>
          <Breadcrumb.Item href="/">Home</Breadcrumb.Item>
          <BreadcrumbItem active>Dendrograms</BreadcrumbItem>
        </Breadcrumb>
      </div>
    );
};

export class Dendrograms extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            dendrogram: {},
            dendrograms: [],
            dendrogramGraphs: []
        };
    }

    componentDidMount() {
        this.loadDendrograms();
    }

    loadDendrograms() {
        const service = new RepositoryService();
        service.getDendrograms().then(response => {
            this.setState({
                dendrogram: response.data === [] ? {} : response.data[0],
                dendrograms: response.data,
                dendrogramGraphs: response.data.map(d => d.graphs).flat()
            });
        });
    }

    handleDeleteDendrogram(name) {
        const service = new RepositoryService();
        service.deleteDendrogram(name).then(response => {
            this.loadDendrograms();
        });
    }

    changeDendrogram(value) {
        this.setState({
            dendrogram: this.state.dendrograms.filter(d => d.name === value)[0]
        });
    }

    render() {
        const dendrograms = this.state.dendrograms.map(d =>
            <Button active={this.state.dendrogram.name === d.name} onClick={() => this.changeDendrogram(d.name)}>{d.name}</Button>
        );

        const rows = this.state.dendrogramGraphs.flat().map(graph => {
            return {
                id: graph.dendrogramName + graph.name,
                dendrogram: graph.dendrogramName,
                graph: graph.name,
                cut: graph.cutValue,
                clusters: graph.clusters.length,
                singleton: graph.clusters.filter(c => c.entities.length === 1).length,
                max_cluster_size: Math.max(...graph.clusters.map(c => c.entities.length)),
                ss: Number(graph.silhouetteScore.toFixed(2)).toString()
            } 
        });

        const columns = [{
            dataField: 'dendrogram',
            text: 'Dendrogram',
            sort: true
        }, {
            dataField: 'graph',
            text: 'Graph',
            sort: true
        }, {
            dataField: 'cut',
            text: 'Cut',
            sort: true
        }, {
            dataField: 'clusters',
            text: 'Number of Retrieved Clusters',
            sort: true
        }, {
            dataField: 'singleton',
            text: 'Number of Singleton Clusters',
            sort: true
        }, {
            dataField: 'max_cluster_size',
            text: 'Maximum Cluster Size',
            sort: true
        }, {
            dataField: 'ss',
            text: 'Silhouette Score',
            sort: true,
            headerFormatter: columnHelp
        }];

        function columnHelp(column, colIndex) {
            return (
                <div>
                    {column.text}
                    <img width="10px" height="10px" src="https://cdn4.iconfinder.com/data/icons/symbols-2-1/32/11_questionmark_ask_circle_round-512.png"/>
                </div>
            );
        }

        return (
            <div>
                <BreadCrumbs />
                <h2 className="mb-3">Dendrograms</h2>
                <Button className="mb-3" href="/dendrogram/create">Create New Dendrogram</Button>                

                {dendrograms.length !== 0 &&
                    <div>
                    <div style={{overflow: "auto"}} className="mb-3">
                    <ButtonGroup>
                        {dendrograms}
                    </ButtonGroup>
                    </div>

                    <Card className="mb-5" key={this.state.dendrogram.name} style={{ width: '20rem' }}>
                    <Card.Img variant="top" src={DENDROGRAM_URL + "?dendrogramName=" + this.state.dendrogram.name + "&&" + new Date().getTime()} />
                    <Card.Body>
                        <Card.Title>
                            {this.state.dendrogram.name}
                        </Card.Title>
                        <Card.Text>
                            Linkage Type: {this.state.dendrogram.linkageType}< br/>
                            Undistinct Access Metric Weight: {this.state.dendrogram.accessMetricWeight}< br/>
                            Read/Write Access Metric Weight: {this.state.dendrogram.readWriteMetricWeight}< br/>
                            Sequence Access Metric Weight: {this.state.dendrogram.sequenceMetricWeight}
                        </Card.Text>
                        <Button href={`/dendrogram/${this.state.dendrogram.name}`} className="mr-4" variant="primary">See Dendrogram</Button>
                        <Button onClick={() => this.handleDeleteDendrogram(this.state.dendrogram.name)} variant="danger">Delete</Button>
                    </Card.Body>
                    </Card>
                    </div>
                }

                {this.state.dendrogramGraphs.length > 0 &&
                    <span>
                    <h5>Metrics</h5>
                    <BootstrapTable bootstrap4 keyField='id' data={ rows } columns={ columns } />
                    </span>
                }
            </div>
        )
    }
}