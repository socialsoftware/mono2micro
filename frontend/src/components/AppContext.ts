import { createContext } from "react";

interface AppContext {
    translateEntity: (entityID: number) => string;
    updateEntityTranslationFile: (file: Record<number, string>) => void;
}

const defaultValue: AppContext = {
    translateEntity: () => "",
    updateEntityTranslationFile: () => undefined
};

const AppContext = createContext<AppContext>(defaultValue);

export const AppProvider = AppContext.Provider;
export const AppConsumer = AppContext.Consumer;
export default AppContext;
