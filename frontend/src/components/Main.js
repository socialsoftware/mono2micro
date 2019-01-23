import React from 'react';
import { Switch, Route } from 'react-router-dom';
import { Home } from './Home';
import { DendrogramLoad } from './graph/DendrogramLoad';
import { DendrogramCut } from './graph/DendrogramCut';
import { Graph } from './graph/Graph';

export const Main = () => (
  <main>
    <Switch>
      <Route exact path='/' component={Home}/>
      <Route exact path='/dendrogram/load' component={DendrogramLoad} />
      <Route exact path='/dendrogram/cut' component={DendrogramCut} />
      <Route path='/graphs/name/:name' component={Graph} />
    </Switch>
  </main>
)
