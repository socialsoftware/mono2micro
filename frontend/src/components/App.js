import React from 'react';
import { Header} from './Header';
import { Main } from './Main';
import Container from 'react-bootstrap/Container';
import Jumbotron from 'react-bootstrap/Jumbotron';

class App extends React.Component {
  render() {
    return (
      <div>
          <Header />
          <Container fluid>
            <Jumbotron>
              <Main />
            </Jumbotron>
          </Container>
      </div>
    )
  }
}

export default App;
