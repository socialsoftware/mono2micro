import Source from "../sources/Source";
import {SourceFactory} from "../sources/SourceFactory";
import Strategy from "../strategy/Strategy";
import {StrategyFactory} from "../strategy/StrategyFactory";

export default class Codebase {
    name!: string;
    strategies: Strategy[] | null;
    sources: Source[] | null;
    isEmpty: boolean | null;

    public constructor(codebase: any) {
        this.name = codebase.name;
        this.strategies = codebase.strategies.map((strategy: Strategy) => StrategyFactory.getStrategy(strategy));
        this.sources = SourceFactory.getSources(codebase.sources);
        this.isEmpty = codebase.empty;
    }
}
