import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { Home } from './Home';
import { Decompositions } from './decomposition/Decompositions';
import { Views } from './view/accessesSciPyViews/Views';
import { Codebases } from './codebase/Codebases';
import { Strategies } from './strategies/Strategies';
import { Analysis } from './analysis/Analysis';
import { Profiles } from './profile/Profiles';
import {FunctionalityRefactorToolMenu} from "./view/accessesSciPyViews/FunctionalityRefactorToolMenu";
import {Similarities} from "./similarity/Similarities";

export const Main = () => (
  <main>
    <Routes>
      <Route exact path='/' element={<Home/>}/>

      <Route exact path='/codebases' element={<Codebases/>} />
      <Route exact path='/codebases/:codebaseName' element={<Strategies/>} />
      <Route exact path='/codebases/:codebaseName/:strategyName' element={<Similarities/>} />
      <Route exact path='/codebases/:codebaseName/:strategyName/:similarityName/similarity' element={<Decompositions/>} />
      <Route exact path='/codebases/:codebaseName/:strategyName/:decompositionName/viewDecomposition' element={<Views/>} />
      <Route exact path='/codebases/:codebaseName/:strategyName/:decompositionName/functionalityRefactor' element={<FunctionalityRefactorToolMenu/>} />

      <Route exact path='/codebase/:codebaseName/:representationName/profiles' element={<Profiles/>} />

      <Route exact path='/analysis' element={<Analysis/>} />
    </Routes>
  </main>
)
