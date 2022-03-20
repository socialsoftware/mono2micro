import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { Home } from './Home';
import { Strategy } from './strategy/Strategy';
import { Views } from './view/Views';
import { Strategies } from './strategy/Strategies'
import { Codebases } from './codebase/Codebases';
import { Codebase } from './codebase/Codebase';
import { Analysis } from './analysis/Analysis';
import { Analyser } from './analysis/Analyser';
import { Profiles } from './profile/Profiles';

export const Main = () => (
  <main>
    <Routes>
      <Route exact path='/' element={<Home/>}/>

      <Route exact path='/codebases' element={<Codebases/>} />
      <Route exact path='/codebases/:codebaseName' element={<Codebase/>} />
      <Route exact path='/codebases/:codebaseName/strategies' element={<Strategies/>} />
      <Route exact path='/codebases/:codebaseName/strategies/:strategyName' element={<Strategy/>} />
      <Route exact path='/codebases/:codebaseName/strategies/:strategyName/decompositions/:decompositionName' element={<Views/>} />

      <Route exact path='/codebases/:codebaseName/profiles' element={<Profiles/>} />

      <Route exact path='/analysis' element={<Analysis/>} />
      <Route exact path='/analyser' element={<Analyser/>} />
    </Routes>
  </main>
)
