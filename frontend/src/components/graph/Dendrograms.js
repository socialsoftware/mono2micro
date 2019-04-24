import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';
import { Button, Card, CardDeck, Breadcrumb, BreadcrumbItem, Table } from 'react-bootstrap';
import { DENDROGRAM_URL } from '../../constants/constants';
import BootstrapTable from 'react-bootstrap-table-next';

const BreadCrumbs = () => {
    return (
      <div>
        <Breadcrumb style={{ backgroundColor: '#a32a2a' }}>
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
            dendrogramNames: [],
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
                dendrogramNames: response.data
            });

            var i;
            this.setState({
                dendrogramGraphs: []
            });
            for (i = 0; i < response.data.length; i++) { 
                service.getGraphs(response.data[i]).then(response2 => {
                    if (response2.data.length > 0)
                        this.state.dendrogramGraphs.push(response2.data);
                    this.setState({
                        dendrogramGraphs: this.state.dendrogramGraphs
                    });
                });
            }
        });
    }

    handleDeleteDendrogram(name) {
        const service = new RepositoryService();
        service.deleteDendrogram(name).then(response => {
            this.loadDendrograms();
        });
    }

    render() {
        const dendrograms = this.state.dendrogramNames.map( name =>
            <Card key={name} style={{ width: '18rem' }}>
                <Card.Img variant="top" src={DENDROGRAM_URL + "?dendrogramName=" + name + "&&" + new Date().getTime()} />
                <Card.Body>
                    <Card.Title>{name}</Card.Title>
                    <Button href={`/dendrogram/${name}`} className="mr-4" variant="primary">See Dendrogram</Button>
                    <Button onClick={() => this.handleDeleteDendrogram(name)} variant="danger">Delete</Button>
                </Card.Body>
            </Card>
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
                ss: graph.silhouetteScore
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
            sort: true
        }];

        return (
            <div>
                <BreadCrumbs />
                <h2 className="mb-3">Dendrograms</h2>
                <Button className="mb-3" href="/dendrogram/create">Create New Dendrogram</Button>
                <CardDeck>
                    {dendrograms}
                </CardDeck>

                {this.state.dendrogramGraphs.length > 0 &&
                    <BootstrapTable bootstrap4 keyField='id' data={ rows } columns={ columns } />
                }

                
            </div>
        )
    }
}