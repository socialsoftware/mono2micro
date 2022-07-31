import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { Home } from './Home';
import { Decompositions } from './decompositions/Decompositions';
import { Views } from './view/Views';
import { Dendrograms } from './dendrograms/Dendrograms'
import { Codebases } from './codebase/Codebases';
import { Strategies } from './strategies/Strategies';
import { Analysis } from './analysis/Analysis';
import { Profiles } from './profile/Profiles';
import {FunctionalityRefactorToolMenu} from "./view/FunctionalityRefactorToolMenu";

export const Main = () => (
  <main>
    <Routes>
      <Route exact path='/' element={<Home/>}/>

      <Route exact path='/codebases' element={<Codebases/>} />
      <Route exact path='/codebases/:codebaseName' element={<Strategies/>} />
      <Route exact path='/codebases/:codebaseName/strategies' element={<Strategies/>} />
      <Route exact path='/codebases/:codebaseName/strategies/:strategyName' element={<Decompositions/>} />
      <Route exact path='/codebases/:codebaseName/strategies/:strategyName/decompositions/:decompositionName/viewDecomposition' element={<Views/>} />
      <Route exact path='/codebases/:codebaseName/strategies/:strategyName/decompositions/:decompositionName/functionalityRefactor' element={<FunctionalityRefactorToolMenu/>} />

      <Route exact path='/codebase/:codebaseName/source/:sourceName/profiles' element={<Profiles/>} />

      <Route exact path='/analysis' element={<Analysis/>} />
    </Routes>
  </main>
)
