package org.demonsoft.spatialkappa.model;

import java.util.List;
import java.util.Map;

public interface SimulationState {

    double getTime();
    double getElapsedTime();
    double getMaximumTime();
    int getEventCount();
    int getMaximumEventCount();
    
    
    Map<String, Variable> getVariables();
    Variable getVariable(String label);

    ObservationElement getComplexQuantity(Variable variable);
    ObservationElement getTransitionFiredCount(Variable variable);

    void addComplexInstances(List<Agent> agents, int amount);
    void setTransitionRateOrVariable(String name, VariableExpression rateExpression);
    void stop();
    void snapshot();
}
