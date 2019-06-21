import React from 'react';
import { Switch, Route } from 'react-router-dom';
import { Home } from './Home';
import { DendrogramCreate } from './dendrogram/DendrogramCreate';
import { DendrogramCut } from './dendrogram/DendrogramCut';
import { Views } from './view/Views';
import { Dendrograms } from './dendrogram/Dendrograms'
import { Codebases } from './codebase/Codebases';
import { Codebase } from './codebase/Codebase';
import { Analysis } from './analysis/Analysis';
import { Experts } from './expert/Experts';
import { Expert } from './expert/Expert';

export const Main = () => (
  <main>
    <Switch>
      <Route exact path='/' component={Home}/>
      <Route exact path='/dendrogram/create' component={DendrogramCreate} />

      <Route exact path='/codebases' component={Codebases} />
      <Route exact path='/codebase/:codebaseName' component={Codebase} />
      <Route exact path='/codebase/:codebaseName/dendrograms' component={Dendrograms} />
      <Route exact path='/codebase/:codebaseName/dendrogram/:dendrogramName' component={DendrogramCut} />
      <Route exact path='/codebase/:codebaseName/dendrogram/:dendrogramName/graph/:graphName' component={Views} />
      
      <Route exact path='/codebase/:codebaseName/experts' component={Experts} />
      <Route exact path='/codebase/:codebaseName/expert/:expertName' component={Expert} />

      <Route exact path='/analysis' component={Analysis} />
    </Switch>
  </main>
)
