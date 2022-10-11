import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { Home } from './Home';
import { Decompositions } from './decomposition/Decompositions';
import { Views } from './view/accessesViews/Views';
import { Codebases } from './codebase/Codebases';
import { Strategies } from './strategy/Strategies';
import { Analysis } from './analysis/Analysis';
import { Profiles } from './profile/Profiles';
import {FunctionalityRefactorToolMenu} from "./view/accessesViews/FunctionalityRefactorToolMenu";
import {Similarities} from "./similarity/Similarities";
import {RepositoryViewGraph} from "./view/repositoryView/RepositoryViewGraph";
import {Recommendations} from "./recommendation/Recommendations";

export const Main = () => (
  <main>
    <Routes>
      <Route exact path='/' element={<Home/>}/>

      <Route exact path='/codebases' element={<Codebases/>} />
      <Route exact path='/codebases/:codebaseName' element={<Strategies/>} />
      <Route exact path='/codebases/:codebaseName/:strategyName/similarity' element={<Similarities/>} />
      <Route exact path='/codebases/:codebaseName/:strategyName/recommendation' element={<Recommendations/>} />
      <Route exact path='/codebases/:codebaseName/:strategyName/:similarityName/decomposition' element={<Decompositions/>} />
      <Route exact path='/codebases/:codebaseName/:strategyName/:decompositionName/accessesViews' element={<Views/>} />
      <Route exact path='/codebases/:codebaseName/:strategyName/:decompositionName/repositoryView' element={<RepositoryViewGraph/>} />
      <Route exact path='/codebases/:codebaseName/:strategyName/:decompositionName/functionalityRefactor' element={<FunctionalityRefactorToolMenu/>} />

      <Route exact path='/codebase/:codebaseName/:representationName/profiles' element={<Profiles/>} />

      <Route exact path='/analysis' element={<Analysis/>} />
    </Routes>
  </main>
)
