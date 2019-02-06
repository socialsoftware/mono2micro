import React from 'react';
import { Switch, Route } from 'react-router-dom';
import { Home } from './Home';
import { DendrogramCreate } from './graph/DendrogramCreate';
import { DendrogramCut } from './graph/DendrogramCut';
import { Decomposition } from './graph/Decomposition';

export const Main = () => (
  <main>
    <Switch>
      <Route exact path='/' component={Home}/>
      <Route exact path='/dendrogram/create' component={DendrogramCreate} />
      <Route exact path='/dendrogram/cut' component={DendrogramCut} />
      <Route path='/graphs/name/:name' component={Decomposition} />
    </Switch>
  </main>
)
