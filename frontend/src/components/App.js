import React from 'react';
import { Header} from './Header';
import { Main } from './Main';
import { BrowserRouter } from 'react-router-dom';


class App extends React.Component {
  render() {
    return (
      <BrowserRouter basename="">
        <Header />
        <div style={{ paddingLeft: "2rem", paddingTop: "2rem" }}>
            <Main />
        </div>
      </BrowserRouter>
    )
  }
}

export default App;
