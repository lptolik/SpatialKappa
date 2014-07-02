package org.demonsoft.spatialkappa.api;

import static org.demonsoft.spatialkappa.model.Location.NOT_LOCATED;
import org.demonsoft.spatialkappa.model.KappaModel;
import org.demonsoft.spatialkappa.model.IKappaModel;
import org.demonsoft.spatialkappa.tools.TransitionMatchingSimulation;
import org.demonsoft.spatialkappa.tools.Simulation;
import org.demonsoft.spatialkappa.model.SimulationState;
import org.demonsoft.spatialkappa.model.Agent;
import org.demonsoft.spatialkappa.model.AgentLink;
import org.demonsoft.spatialkappa.model.AgentSite;
import org.demonsoft.spatialkappa.model.AgentDeclaration;
import org.demonsoft.spatialkappa.model.Observation;
import org.demonsoft.spatialkappa.model.ObservationElement;
import org.demonsoft.spatialkappa.model.Complex;
import org.demonsoft.spatialkappa.model.Variable;
import org.demonsoft.spatialkappa.model.Transition;
import org.demonsoft.spatialkappa.model.VariableExpression;

// import org.antlr.runtime.CharStream;
import org.demonsoft.spatialkappa.model.Utils;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.lang.IllegalArgumentException;
import java.lang.Exception;

public class SpatialKappaSim
{
    private IKappaModel kappaModel;
    private TransitionMatchingSimulation simulation;
    private boolean verbose;
    public double timeMult;

    // Constructors
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

    // Get the time in user units
    public float getTime() {
        return(simulation.getTime()/(float)timeMult);
    }

    // Run methods

    // stepEndTime is provided in user units
    public void runUntilTime(float stepEndTime, boolean progress) {
        simulation.runByTime2(stepEndTime*(float)timeMult, progress);
        if (verbose) {
            // This allows us to get the value of a particular observable
            Observation observation = simulation.getCurrentObservation();
            System.out.println(observation.toString());
        }
    }

    // dt is provided in user units
    public void runForTime(float dt, boolean progress) {
        float stepEndTime = getTime() + dt;
        runUntilTime(stepEndTime, progress);
    }

    public Map<String, Variable> getVariables() {
        Map<String, Variable> variables = kappaModel.getVariables();
        if (verbose) {
            for (Map.Entry<String, Variable> variable : variables.entrySet()) {
                System.out.println("Key = " + variable.getKey() + ", Value = " + variable.getValue());
            }
        }
        return(variables);
    }

    // Variables interface
    public void setVariable(float input, String label) {
        kappaModel.addVariable(new VariableExpression(input), label);
        initialiseSim();
    }

    public double getVariable(String variableName) {
        Variable variable = kappaModel.getVariables().get(variableName);
        ObservationElement observable = variable.evaluate(simulation);
        return(observable.value);
    }

    public void addVariable(Agent agent, String siteName, String linkName) {
        List<Agent> agents = new ArrayList<Agent>();
        agents.add(agent.clone()); 
        AgentSite agentSite = agents.get(0).getSite(siteName);
        agentSite.setLinkName(linkName);
        // kappaModel.addVariable(new VariableExpression(agents, NOT_LOCATED), agent.toString() + "?");
        kappaModel.addVariable(agents, agent.toString() + "?", NOT_LOCATED, false);
        initialiseSim();
    }

    // Observation interface
    public double getObservation(String key) {
        Observation observation = simulation.getCurrentObservation();
        return(observation.observables.get(key).value);
    }

    public Agent getAgent(String name) {
        for (Complex complex : kappaModel.getFixedLocatedInitialValuesMap().keySet()) {
            for (Agent currentAgent : complex.agents) {
                if (name.equals(currentAgent.name)) {
                    return(currentAgent);
                }
            }
        }
        return (Agent) null;
    }

    // value can be negative
    public void addAgent(String key, int value) {
        List<Agent> agents = new ArrayList<Agent>();
        SimulationState state = (SimulationState) simulation;                
        Agent agent = getAgent(key);
        if (agent != null) {
            agents.add(agent);
            state.addComplexInstances(agents, value);
            agents.clear();
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
        Agent agent = getAgent(key);
        if (agent != null) {
            agents.add(agent);
            kappaModel.overrideInitialValue(agents, Integer.toString(value), NOT_LOCATED);
            agents.clear();
        }
        initialiseSim();
        if (verbose) { System.out.println("Number of " + key + " is " +  getObservation(key)); }
    }

    public void setAgentInitialValue(String key, double value) {
        setAgentInitialValue(key, (int)value);
    }
    
    private void printFixedLocatedInitialValuesMap() {
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

    public String printAgentAgentInteractions() {
        Map<String, Set<String>> interactions = new HashMap<String, Set<String>>();
        for (Transition transition : kappaModel.getTransitions()) {
            List<Complex> allComplexes = new ArrayList<Complex>();
            allComplexes.addAll(transition.sourceComplexes);
            allComplexes.addAll(transition.targetComplexes);
            for (Complex complex: allComplexes) {
                for (Agent agent: complex.agents) {
                    if (!interactions.containsKey(agent.name)) {
                        interactions.put(agent.name, new HashSet<String>());
                    }
                    for (Agent coagent: complex.agents) {
                        if (agent.name != coagent.name) {
                            interactions.get(agent.name).add(coagent.name);
                        }
                    }
                }
            }
        }
        return interactions.toString();
    }

    public Transition getTransition(String label) {
        return simulation.getTransition(label);
    }

    // Limited version of addTransition(), just enough to make a creation rule
    public Transition addTransition(String label, Agent rightSideAgent, float rate) {
        List<Transition> transitions = kappaModel.getTransitions();
        for (Transition transition: transitions) {
            if (label.equals(transition.label)) {
                String error = "Transition label \"" + label + "\" already exists";
                throw(new IllegalArgumentException(error));
            }
        }
        List<Agent> rightSideAgents = new ArrayList();
        rightSideAgents.add(rightSideAgent);
        kappaModel.addTransition(label, null, null, null, null, rightSideAgents, new VariableExpression(rate));
        initialiseSim();

        // Returning the transition may not be strictly necessary
        transitions = kappaModel.getTransitions();
        for (Transition transition: transitions) {
            if (label.equals(transition.label)) {
                return transition;
            }
        }
        return (Transition) null; 
    }

    public void setTransitionRate(String name, float rate) {
        simulation.setTransitionRateOrVariable(name, new VariableExpression(rate));
    }

}
