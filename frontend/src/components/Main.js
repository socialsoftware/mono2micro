import React from 'react';
import { Switch, Route } from 'react-router-dom';
import { Home } from './Home';
import { Dendrogram } from './dendrogram/Dendrogram';
import { Views } from './view/Views';
import { Dendrograms } from './dendrogram/Dendrograms'
import { Codebases } from './codebase/Codebases';
import { Codebase } from './codebase/Codebase';
import { Analysis } from './analysis/Analysis';
import { Analyser } from './analysis/Analyser';
import { Profiles } from './profile/Profiles';

export const Main = () => (
  <main>
    <Switch>
      <Route exact path='/' component={Home}/>

      <Route exact path='/codebases' component={Codebases} />
      <Route exact path='/codebases/:codebaseName' component={Codebase} />
      <Route exact path='/codebases/:codebaseName/dendrograms' component={Dendrograms} />
      <Route exact path='/codebases/:codebaseName/dendrograms/:dendrogramName' component={Dendrogram} />
      <Route exact path='/codebases/:codebaseName/dendrograms/:dendrogramName/decompositions/:decompositionName' component={Views} />

      <Route exact path='/codebases/:codebaseName/profiles' component={Profiles} />

      <Route exact path='/analysis' component={Analysis} />
      <Route exact path='/analyser' component={Analyser} />
    </Switch>
  </main>
)
