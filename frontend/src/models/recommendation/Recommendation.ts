export default class Recommendation {
    type!: string;
    strategyName!: string;
    name: string;
    isCompleted: boolean;

    constructor(recommendation: any) {
        this.type               = recommendation.type;
        this.strategyName       = recommendation.strategyName;
        this.name               = recommendation.name;
        this.isCompleted        = Boolean(recommendation.completed) || false;
    }
}
