import React from 'react';
import { Switch, Route } from 'react-router-dom';
import { Home } from './Home';
import { DendrogramCreate } from './graph/DendrogramCreate';
import { DendrogramCut } from './graph/DendrogramCut';
import { Views } from './graph/Views';
import { Dendrograms } from './graph/Dendrograms'
import { Profiles } from './graph/Profiles'
import { ProfileGroup } from './graph/ProfileGroup'

export const Main = () => (
  <main>
    <Switch>
      <Route exact path='/' component={Home}/>
      <Route exact path='/dendrogram/create' component={DendrogramCreate} />
      <Route exact path='/dendrograms' component={Dendrograms} />
      <Route exact path='/dendrogram/:dendrogramName' component={DendrogramCut} />
      <Route exact path='/dendrogram/:dendrogramName/graph/:graphName' component={Views} />
      <Route exact path='/profiles' component={Profiles} />
      <Route exact path='/profile/:profileGroupName' component={ProfileGroup} />
    </Switch>
  </main>
)
