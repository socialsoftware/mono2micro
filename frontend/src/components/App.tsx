import React, { FunctionComponent, useState } from 'react';
import Header from './Header';
import { Main } from './Main';
import { BrowserRouter } from 'react-router-dom';
import { AppProvider } from './AppContext';

const App: FunctionComponent = () => {

    const [entityTranslationFile, setEntityTranslationFile] = useState<Record<number, string>>({});

    const translateEntity = (entityID: number) => entityTranslationFile[entityID] ?? `${entityID}`;

    const updateEntityTranslationFile = (file: Record<number, string>) => {
        setEntityTranslationFile(file);
    }

    return (
        <AppProvider value={{
            translateEntity,
            updateEntityTranslationFile,
        }}
        >
            <BrowserRouter basename="">
                <Header />
                <div style={{ paddingTop: "1rem" }}>
                    <Main />
                </div>
            </BrowserRouter>
        </AppProvider>
    );
};

export default App;
