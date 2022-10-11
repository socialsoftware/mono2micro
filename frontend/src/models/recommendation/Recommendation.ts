export default class Recommendation {
    type!: string;
    strategyName!: string;
    name: string;
    decompositionType: string;
    isCompleted: boolean;

    constructor(recommendation: any) {
        this.type               = recommendation.type;
        this.strategyName       = recommendation.strategyName;
        this.name               = recommendation.name;
        this.decompositionType  = recommendation.decompositionType;
        this.isCompleted        = Boolean(recommendation.completed) || false;
    }
}
