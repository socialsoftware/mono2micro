import React from 'react';
import { Switch, Route } from 'react-router-dom';
import { Home } from './Home';
import { DendrogramCreate } from './graph/DendrogramCreate';
import { DendrogramCut } from './graph/DendrogramCut';
import { Views } from './graph/Views';
import { Dendrograms } from './graph/Dendrograms'
import { Codebases } from './graph/Codebases';
import { Codebase } from './graph/Codebase';
import { Analysis } from './graph/Analysis';
import { Experts } from './graph/Experts';
import { Expert } from './graph/Expert';

export const Main = () => (
  <main>
    <Switch>
      <Route exact path='/' component={Home}/>
      <Route exact path='/dendrogram/create' component={DendrogramCreate} />
      <Route exact path='/dendrograms' component={Dendrograms} />
      <Route exact path='/dendrogram/:dendrogramName' component={DendrogramCut} />
      <Route exact path='/dendrogram/:dendrogramName/graph/:graphName' component={Views} />
      <Route exact path='/codebases' component={Codebases} />
      <Route exact path='/codebase/:codebaseName' component={Codebase} />
      <Route exact path='/analysis' component={Analysis} />
      <Route exact path='/experts' component={Experts} />
      <Route exact path='/expert/:expertName' component={Expert} />
    </Switch>
  </main>
)
