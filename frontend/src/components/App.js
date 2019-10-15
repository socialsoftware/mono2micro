import React from 'react';
import { Header} from './Header';
import { Main } from './Main';
import { Container, Jumbotron } from 'react-bootstrap';

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
