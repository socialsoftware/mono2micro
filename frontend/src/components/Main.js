import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { Home } from './Home';
import { Dendrogram } from './dendrogram/Dendrogram';
import { Views } from './view/Views';
import { Dendrograms } from './dendrogram/Dendrograms'
import { Codebases } from './codebase/Codebases';
import { Codebase } from './codebase/Codebase';
import { Analysis } from './analysis/Analysis';
import { Analyser } from './analysis/Analyser';
import { CommitAnalyser } from './analysis/CommitAnalyser';
import { Profiles } from './profile/Profiles';

export const Main = () => (
  <main>
    <Routes>
      <Route exact path='/' element={<Home/>}/>

      <Route exact path='/codebases' element={<Codebases/>} />
      <Route exact path='/codebases/:codebaseName' element={<Codebase/>} />
      <Route exact path='/codebases/:codebaseName/dendrograms' element={<Dendrograms/>} />
      <Route exact path='/codebases/:codebaseName/dendrograms/:dendrogramName' element={<Dendrogram/>} />
      <Route exact path='/codebases/:codebaseName/dendrograms/:dendrogramName/decompositions/:decompositionName' element={<Views/>} />

      <Route exact path='/codebases/:codebaseName/profiles' element={<Profiles/>} />

      <Route exact path='/analysis' element={<Analysis/>} />
      <Route exact path='/analyser' element={<Analyser/>} />

      <Route exact path='/commit-analyser' element={<CommitAnalyser/>} />
    </Routes>
  </main>
)
