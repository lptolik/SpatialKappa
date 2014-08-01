package org.demonsoft.spatialkappa.api;

import static org.demonsoft.spatialkappa.model.Location.NOT_LOCATED;
import org.demonsoft.spatialkappa.model.KappaModel;
import org.demonsoft.spatialkappa.model.IKappaModel;
import org.demonsoft.spatialkappa.tools.TransitionMatchingSimulation;
import org.demonsoft.spatialkappa.tools.Simulation;
import org.demonsoft.spatialkappa.model.SimulationState;
import org.demonsoft.spatialkappa.model.Agent;
import org.demonsoft.spatialkappa.model.AgentDeclaration;
import org.demonsoft.spatialkappa.model.Observation;
import org.demonsoft.spatialkappa.model.Complex;
import org.demonsoft.spatialkappa.model.Variable;
import org.demonsoft.spatialkappa.model.VariableExpression;

// import org.antlr.runtime.CharStream;
import org.demonsoft.spatialkappa.model.Utils;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.lang.IllegalArgumentException;

public class SpatialKappaSim
{
    private IKappaModel kappaModel;
    private TransitionMatchingSimulation simulation;
    private boolean verbose;
    public double timeMult;

    public SpatialKappaSim(String timeUnits, boolean verbose) {
        this.verbose = verbose;
        Map<String, Double> allowedTimeUnits = new <String, Double>HashMap();
        allowedTimeUnits.put("s" , new Double(1E-3));
        allowedTimeUnits.put("ms", new Double(1.0));
        if (!allowedTimeUnits.containsKey(timeUnits)) {
            String error = "timeUnits must be one of " + allowedTimeUnits.keySet().toString();
            throw(new IllegalArgumentException(error));
        }
        this.timeMult = (double)allowedTimeUnits.get(timeUnits);
    }

    public SpatialKappaSim() {
        this("ms", false);
    }

    public void loadFile(String kappaFile) throws Exception {
        File f = new File(kappaFile);
        kappaModel = Utils.createKappaModel(f);
        initialiseSim();
    }

    private void initialiseSim() {
        simulation = new TransitionMatchingSimulation(kappaModel);
    }

    public void runUntilTime(float stepEndTime, boolean progress) {
        simulation.runByTime2(stepEndTime*(float)timeMult, progress);
        if (verbose) {
            // This allows us to get the value of a particular observable
            Observation observation = simulation.getCurrentObservation();
            System.out.println(observation.toString());
        }
    }

    public void runForTime(float dt, boolean progress) {
        float stepEndTime = getTime() + dt;
        runUntilTime(stepEndTime, progress);
    }

    public Map<String, Variable> getVariables() {
        Map<String, Variable> variables = kappaModel.getVariables();
        for (Map.Entry<String, Variable> variable : variables.entrySet()) {
            System.out.println("Key = " + variable.getKey() + ", Value = " + variable.getValue());
        }
        return(variables);
    }

    public void setVariable(float input, String label) {
        kappaModel.addVariable(new VariableExpression(input), label);
        initialiseSim();
    }

    public float getTime() {
        return(simulation.getTime()/(float)timeMult);
    }

    public double getObservation(String key) {
        Observation observation = simulation.getCurrentObservation();
        return(observation.observables.get(key).value);
    }
    
    // value can be negative
    public void addAgent(String key, int value) {
        List<Agent> agents = new ArrayList<Agent>();
        SimulationState state = (SimulationState) simulation;                
        for (Complex complex : kappaModel.getFixedLocatedInitialValuesMap().keySet()) {
            for (Agent currentAgent : complex.agents) {
                // if (verbose) { System.out.println(currentAgent.name); }
                if (key.equals(currentAgent.name)) {
                    // if (verbose) { System.out.println("ADD STUFF"); }
                    agents.add(currentAgent);
                    state.addComplexInstances(agents, value);
                    agents.clear();
                }
            }
        }
    }

    public void addAgent(String key, double value) {
        int ivalue = (int)value;
        if (ivalue != value) {
            String error = "Trying to add non-integer number (" + value + ") of \'" + key +  "\' agents";
            throw(new IllegalArgumentException(error));
        }
        addAgent(key, (int)value);
    }

    public void setAgentInitialValue(String key, int value) {
        List<Agent> agents = new ArrayList<Agent>();
        for (Complex complex : kappaModel.getFixedLocatedInitialValuesMap().keySet()) {
            for (Agent currentAgent : complex.agents) {
                if (key.equals(currentAgent.name)) {
                    if (verbose) {
                        System.out.println("Set number of " + currentAgent.name + " to " + value);
                    }
                    agents.add(currentAgent);
                    kappaModel.overrideInitialValue(agents, Integer.toString(value), NOT_LOCATED);
                    agents.clear();
                }
            }
        }
        initialiseSim();
        if (verbose) { System.out.println("Number of " + key + " is " +  getObservation(key)); }
    }
    public void setAgentInitialValue(String key, double value) {
        setAgentInitialValue(key, (int)value);
    }
    
    private void getFixedLocatedInitialValuesMap() {
        for (Map.Entry<Complex, Integer> result : kappaModel.getFixedLocatedInitialValuesMap().entrySet()) {
            System.out.println("Key = " + result.getKey() + ", Value = " + result.getValue());
        }
    }

    @Override
    public String toString() {
        return(kappaModel.toString());
    }

    public void printAgentNames() {
        List<String> agentNames = new ArrayList<String>(kappaModel.getAgentDeclarationMap().keySet());
        for(String agentName : agentNames) {
            System.out.println(agentName + " ");
        }
    }
}
