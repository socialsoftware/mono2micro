import React from 'react';
import ReactDOM from 'react-dom';
import { BrowserRouter } from 'react-router-dom';
import App from './components/App';
import 'bootstrap/dist/css/bootstrap.css';
import './styles/table.css';

ReactDOM.render(
     <BrowserRouter>
        <App />
    </BrowserRouter>,
    document.getElementById('root')
);
