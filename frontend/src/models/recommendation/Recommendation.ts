export default class Recommendation {
    codebaseName: string;
    strategyName: string;
    name: string;
    type: string;
    isCompleted: boolean;

    constructor(recommendation: any) {
        this.codebaseName = recommendation.codebaseName;
        this.strategyName = recommendation.strategyName;
        this.name = recommendation.name;
        this.type         = recommendation.type;
        this.isCompleted  = Boolean(recommendation.completed) || false;
    }
}
