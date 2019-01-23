import React from 'react';
import { Header} from './Header';
import { Main } from './Main';
import { Grid, Jumbotron } from 'react-bootstrap';

class App extends React.Component {
  render() {
    return (
      <div>
          <Header />
          <Grid>
            <Jumbotron>
              <Main />
            </Jumbotron>
          </Grid>
      </div>
    )
  }
}

export default App;
