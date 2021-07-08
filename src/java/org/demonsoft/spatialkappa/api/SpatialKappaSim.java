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
import org.demonsoft.spatialkappa.model.AggregateSite;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.lang.IllegalArgumentException;
import java.lang.Exception;

public class SpatialKappaSim
{
    private IKappaModel kappaModel;
    private TransitionMatchingSimulation simulation;
    private File kappaFile;
    private boolean verbose;
    public double timeMult;

    // Constructors
    public SpatialKappaSim(String timeUnits, boolean verbose, Long seed) {
        report("SpatialKappaSim(%s, %d, %d)", timeUnits, verbose, seed);
        if (seed != null) {
            Utils.setSeed(seed.longValue());
        }
        this.verbose = verbose;
        Map<String, Double> allowedTimeUnits = new HashMap<String, Double>();
        allowedTimeUnits.put("s" , new Double(1E-3));
        allowedTimeUnits.put("ms", new Double(1.0));
        if (!allowedTimeUnits.containsKey(timeUnits)) {
            String error = "timeUnits must be one of " + allowedTimeUnits.keySet().toString();
            throw(new IllegalArgumentException(error));
        }
        this.timeMult = (double)allowedTimeUnits.get(timeUnits);
        this.kappaModel = new KappaModel();
        this.simulation = null;
    }

    public SpatialKappaSim(String timeUnits, boolean verbose) {
        this(timeUnits, verbose, null);
    }
    
    public SpatialKappaSim() {
        this("ms", false);
    }

    //////////////////////////////////////////////////////////////////////
    // Helpers
    //////////////////////////////////////////////////////////////////////

    private void report(String format, Object... args) {
        if (verbose) System.out.printf(format + "\n", args);
    }
    
    //////////////////////////////////////////////////////////////////////
    // Model specification
    //////////////////////////////////////////////////////////////////////
    
    // Agent creation
    // e.g. addAgentDeclaration('Ca', {'x': ['a', 'b', 'c'], 'y': {'g', 'f', 'e'}}) is equivalent to
    // Ca(x~a~b~c, y~e~f~g)

    public void addAgentDeclaration(String name, Map<String,List<String>> sites) {
        List<AggregateSite> sitesList = new ArrayList<AggregateSite>();
        for (Map.Entry<String,List<String>> site: sites.entrySet()) {
            sitesList.add(new AggregateSite(site.getKey(), site.getValue(), null));
        }
        this.kappaModel.addAgentDeclaration(new AgentDeclaration(name, sitesList));
    }

    // Test if an agent is declared
    public boolean isAgent(String name) {
        report("isAgent(%s)", name);
        return(this.kappaModel.getAgentDeclarationMap().containsKey(name));
    }
    
    // Get agent declaration
    public Map<String,List<String>> getAgentDeclaration(String name) {
        report("getAgentDeclaration(%s)", name);
        if (!isAgent(name)) {
            String error = "Agent " + name + " not defined in " + this.kappaFile + "\nMake sure there are both %agent: and %init: declarations.";
            throw(new IllegalArgumentException(error));
        }
        AgentDeclaration ad = this.kappaModel.getAgentDeclarationMap().get(name);
        Map<String,List<String>> sitesMap = new HashMap<String,List<String>>();
        List<AggregateSite> sites = ad.getSites();
        for (AggregateSite site: sites) {
            sitesMap.put(site.getName(), site.getStates());
        }
        return(sitesMap);
    }

    // Initialisation methods
    // Equivalent to %init: This version adds to the list of init lines
    public void addInitialValue(Map<String,Map<String,Map<String,String>>> agents, int value) {
        kappaModel.addInitialValue(agentList(agents), Integer.toString(value), NOT_LOCATED);
    };
    public void addInitialValue(Map<String,Map<String,Map<String,String>>> agents, double value) {
        addInitialValue(agents, (int)value);
    };
    
    // Not equivalent to %init: This version overrides existing init lines with matching agents
    public void overrideInitialValue(List<Agent> agents, int value) {
        report("overrideInitialValue(<agents>, %d)", value);
        kappaModel.overrideInitialValue(agents, Integer.toString(value), NOT_LOCATED);
    };

    public void overrideInitialValue(List<Agent> agents, float value) {
        overrideInitialValue(agents, (int)value);
    };

    
    public void overrideInitialValue(Map<String,Map<String,Map<String,String>>> agents, int value) {
        overrideInitialValue(agentList(agents), value);
    };
    public void overrideInitialValue(Map<String,Map<String,Map<String,String>>> agents, double value) {
        overrideInitialValue(agents, (int)value);
    };

    // Variables interface
    public void addVariable(String label, float input) {
        report("addVariable(%s, %f)", label, input);
        kappaModel.addVariable(new VariableExpression(input), label);
    }

    // This allows a variable to be set using the syntax as described in agentList()
    public void addVariable(String label, Map<String,Map<String,Map<String,String>>> agentsMap) {
        report("addVariable(%s, <agentsMap>)", label);
        List<Agent> agents = agentList(agentsMap);
        kappaModel.addVariable(agents, label, NOT_LOCATED, false);
    }

    // Test if a variable exists
    public boolean isVariable(String name) {
        report("isVariable(%s)", name);
        return(kappaModel.getVariables().containsKey(name));
    }

    public Complex getVariableComplex(String name) throws Exception {
        report("getVariableComplex(%s)", name);
        if (!isVariable(name)) {
            throw(new Exception(name + " is not a variable"));
        }
        Variable var = kappaModel.getVariables().get(name);
        if (var.type != Variable.Type.KAPPA_EXPRESSION) {
            throw(new Exception("Variable " + name + " is not Kappa expression"));
        }
        return(var.complex);
    }
    
    // Limited version of addTransition(), just enough to make a creation rule
    public void addTransition(String label, 
                                    Map<String,Map<String,Map<String,String>>> leftSideAgents, 
                                    Map<String,Map<String,Map<String,String>>> rightSideAgents, 
                                    float rate) {
        report("addTransition(%s, <leftSideAgents>, <rightSideAgents>, %f)", label, rate);
        List<Transition> transitions = kappaModel.getTransitions();
        for (Transition transition: transitions) {
            if (label.equals(transition.label)) {
                String error = "Transition label \"" + label + "\" already exists";
                throw(new IllegalArgumentException(error));
            }
        }
        kappaModel.addTransition(label, 
                                 NOT_LOCATED, agentList(leftSideAgents), 
                                 null, 
                                 NOT_LOCATED, agentList(rightSideAgents), 
                                 new VariableExpression(rate));
    }

    // General function to allow an AgentList to be created programmtically.     
    // This allows a variable to be set using the following syntax in python:
    // {agent_name1: {site_name1: {"l": link_name, "s": state_name}, site_name2: {"l": link_name, "s": state_name}, ...}, agent_name2: {site_name1: {"l": link_name, "s": state_name}, ...}, ...}
    //
    // e.g.:
    // {"ca": {"x": {"l": "1"}}, "P": {"x": {"l": "1"}}}
    public List<Agent> agentList(Map<String,Map<String,Map<String,String>>> agentsMap) {
        List<Agent> agents = new ArrayList<Agent>();
        for (Map.Entry<String,Map<String,Map<String,String>>> entry: agentsMap.entrySet()) {
            String agentName = entry.getKey();
            Map<String,Map<String,String>> sites = entry.getValue();
            AgentDeclaration agentDeclaration = kappaModel.getAgentDeclarationMap().get(agentName);
            Map<String, AggregateSite> agentDeclarationSites = new HashMap<String, AggregateSite>();
            for (AggregateSite ads: agentDeclaration.getSites()) {
                agentDeclarationSites.put(ads.getName(), ads);
            }
            Agent agent = new Agent(agentDeclaration.getName());
            for (Map.Entry<String,Map<String,String>> site: sites.entrySet()) {
                String siteName = site.getKey();
                if (!agentDeclarationSites.containsKey(siteName)) {
                    String error = "Agent \"" +  agentName + "\" does not have a site \"" + siteName + "\"";
                    throw(new IllegalArgumentException(error));
                }
                Map<String,String> siteLinkState = site.getValue();
                for (String key: siteLinkState.keySet()) {
                    Set<String> validKeys = new HashSet<String>() {{add("l"); add("s");}};
                    if (!validKeys.contains(key)) {
                        String error = "Agent \"" +  agentName + "\" site name  \"" + siteName + "\" cannot have a \" + key + \" attribute; only \"l\" and \"s\" are valid";
                        throw(new IllegalArgumentException(error));
                    }
                }
                String linkName = null;
                if (siteLinkState.keySet().contains("l")) {
                    linkName = site.getValue().get("l");
                }
                String state = null;
                if (siteLinkState.keySet().contains("s")) {
                    state = siteLinkState.get("s");
                    if (!agentDeclarationSites.get(siteName).getStates().contains(state)) {
                        String error = "Agent \"" +  agentName + "\" site name  \"" + siteName + "\" does not have state " + state;
                        throw(new IllegalArgumentException(error));

                    }
                }
                AgentSite agentSite = new AgentSite(siteName, state, linkName);
                agent.addSite(agentSite);

            }
            agents.add(agent);
        }
        return(agents);
    }

    public List<Agent> agentList(Complex complex) {
        return(complex.agents);
    }
    
    public void loadFile(String kappaFileName) throws Exception {
        report("loadFile(%s)", kappaFileName);
        kappaFile = new File(kappaFileName);
        kappaModel = Utils.createKappaModel(kappaFile);
    }

    public void initialiseSim() {
        report("initialiseSim()");
        simulation = new TransitionMatchingSimulation(kappaModel);
    }

    public boolean isInitialised() {
        report("isIntialised()");
        return(simulation != null);
    }
    
    // General Accessor methods

    // Get the time in user units
    public double getTime() {
        report("getTime()");
        return(simulation.getTime()/timeMult);
    }

    
    //////////////////////////////////////////////////////////////////////
    // Model simulation
    //////////////////////////////////////////////////////////////////////
    
    // Run methods
    // stepEndTime is provided in user units
    public void runUntilTime(double stepEndTime, boolean progress) throws Exception {
        if (simulation == null) { initialiseSim(); };
        try {
            simulation.runByTime2(stepEndTime*timeMult, progress);
        }
        catch (Exception ex) {
            throw new Exception("Problem running simulation", ex);
        }
        if (verbose) {
            // This allows us to get the value of a particular observable
            Observation observation = simulation.getCurrentObservation();
            System.out.println(observation.toString());
        }
    }

    // test_runForTime
    // dt is provided in user units
    public void runForTime(double dt, boolean progress) throws Exception {
        report("runForTime(%f)", dt);
        if (simulation == null) { initialiseSim(); };
        double stepEndTime = getTime() + dt;
        try {
            runUntilTime(stepEndTime, progress);
        } catch (Exception ex) {
            throw(new Exception("Problem running simulation", ex));
        }
    }

    public double getVariable(String variableName) throws Exception {
        report("getVariable(%s)", variableName);
        if (simulation == null) {
            throw(new Exception("Simulation is not initialised, so not possble to get value of variable '" + variableName + "'"));
        }
        Variable variable = kappaModel.getVariables().get(variableName);
        ObservationElement observable = variable.evaluate(simulation);
        return(observable.value);
    }
    
    public Map<String, Variable> getVariables() {
        Map<String, Variable> variables = kappaModel.getVariables();
        return(variables);
    }
    
    // Observation interface
    public double getObservation(String key) {
        Observation observation = simulation.getCurrentObservation();
        return(observation.observables.get(key).value);
    }


    // Transition interface
    public Transition getTransition(String label) {
        if (simulation == null) {
            for (Transition transition: kappaModel.getTransitions()) {
                if (label.equals(transition.label)) {
                    return transition;
                }
            }
            return (Transition) null;
        }
        return simulation.getTransition(label);
    }
    
    public void setTransitionRateOrVariable(String label, float rate) throws Exception {
        report("setTransitionRateOrVariable(%s, %f)", label, rate);
        if (!isInitialised()) {
            throw(new Exception("Simulation not initalised. Initialise using initialSim()"));
        }
        simulation.setTransitionRateOrVariable(label, new VariableExpression(rate));
    }
    
    // Agent interface
    // value can be negative
    public void addAgent(String key, int value) {
        List<Agent> agents = new ArrayList<Agent>();
        SimulationState state = (SimulationState) simulation;               
        Agent agent = getAgent(key);
        agents.add(agent);
        state.addComplexInstances(agents, value);
        agents.clear();     
    }

    public void addAgent(String key, double value) {
        int ivalue = (int)value;
        if (ivalue != value) {
            String error = "Trying to add non-integer number (" + value + ") of \'" + key +  "\' agents";
            throw(new IllegalArgumentException(error));
        }
        addAgent(key, (int)value);
    }


    // Printing methods
    @Override
    public String toString() {
        return(kappaModel.toString());
    }

    public void printFixedLocatedInitialValuesMap() {
        for (Map.Entry<Complex, Integer> result : kappaModel.getFixedLocatedInitialValuesMap().entrySet()) {
            System.out.println("Key = " + result.getKey() + ", Value = " + result.getValue());
        }
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

    public String getDebugOutput() {
        return(simulation.getDebugOutput());
    }
    
    // FIXME: Pending removal
    // Get an agent
    private Agent getAgent(String name) {
        for (Complex complex : kappaModel.getFixedLocatedInitialValuesMap().keySet()) {
            for (Agent currentAgent : complex.agents) {
                if (name.equals(currentAgent.name)) {
                    return(currentAgent);
                }
            }
        }
        String error = "Agent " + name + " not defined in " + this.kappaFile + "\nMake sure there are both %agent: and %init: declarations.";
        throw(new IllegalArgumentException(error));
    }

    private Map<String,Map<String,Map<String,String>>> getAgentMap(Agent agent) {
        Map<String,Map<String,Map<String,String>>> agentMap  = new HashMap<String,Map<String,Map<String,String>>>();
        Map<String,Map<String,String>> siteMap  = new HashMap<String,Map<String,String>>();
        Collection<AgentSite> sites = agent.getSites();
        for (AgentSite site : sites) {
            Map<String,String> siteLinkState = new HashMap<String,String>();
            if (site.getLinkName() != null) {
                siteLinkState.put("l", site.getLinkName());
            }
            if (site.getState() != null) {
                siteLinkState.put("s", site.getState());
            }
            siteMap.put(site.name, siteLinkState);
        }
        agentMap.put(agent.name, siteMap);
        return(agentMap);
    }
    
    public void setAgentInitialValue(String key, int value) {
        List<Agent> agents = new ArrayList<Agent>();
        Agent agent = getAgent(key);
        if (agent != null) {

            agents.add(agent);
            overrideInitialValue(agents, value);
            agents.clear();
        }
    }

    public void setAgentInitialValue(String key, double value) {
        setAgentInitialValue(key, (int)value);
    }
    
    private Map<String,Map<String,Map<String,String>>> getAgentMap(String agentName) {
        return(getAgentMap(getAgent(agentName)));
    }

    public void setSeed(long seed) throws Exception {
        String error = "setSeed() is deprecated. Instead call SpatialKappaSim(String timeUnits, boolean verbose, Long seed) with the seed argument" ;
        throw(new Exception(error));
    }
}
