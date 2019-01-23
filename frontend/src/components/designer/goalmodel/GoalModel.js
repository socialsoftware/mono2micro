import React from 'react';
import { connect } from 'react-redux';
import { GoalModelDiagram } from './GoalModelDiagram';

const mapStateToProps = state => {
    return { spec: state.spec };
};  

class ConnectedGoalModel extends React.Component {
    render() {
        return (
            <GoalModelDiagram spec={this.props.spec} />
        );
    }
}

const GoalModel = connect(mapStateToProps)(ConnectedGoalModel);

export default GoalModel;