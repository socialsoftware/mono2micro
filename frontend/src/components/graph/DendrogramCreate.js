import React from 'react';
import { RepositoryService } from '../../services/RepositoryService';

var HttpStatus = require('http-status-codes');

export class DendrogramCreate extends React.Component {
    constructor(props) {
        super(props);
        this.state = { selectedFile: null, isUploaded: "" };

        this.handleSelectedFile = this.handleSelectedFile.bind(this);
        this.handleUpload= this.handleUpload.bind(this);
    }

    handleSelectedFile(event) {
        this.setState({
            selectedFile: event.target.files[0],
            isUploaded: ""
        });
    }

    handleUpload(event){
        event.preventDefault()
        const service = new RepositoryService();
        var data = new FormData();
        data.append('file', this.state.selectedFile);
        
        service.createDendrogram(data).then(response => {
            if (response.status == HttpStatus.CREATED) {
                this.setState({
                    isUploaded: "Upload completed successfully."
                });
            } else {
                this.setState({
                    isUploaded: "Upload failed."
                });
            }
        });
    }

    render() {
        return (
            <form onSubmit={this.handleUpload}>
                <h1>Create Dendrogram</h1>
                <h2>Upload data file from Callgraph Eclipse Plugin to create the dendrogram.</h2>
                <input type="file" onChange={this.handleSelectedFile} />
                <button type="submit">Upload</button>
                <div>{this.state.isUploaded}</div>
            </form>
        )
    }
}