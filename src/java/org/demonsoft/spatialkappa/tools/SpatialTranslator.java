package org.demonsoft.spatialkappa.tools;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.apache.commons.io.FileUtils;
import org.demonsoft.spatialkappa.model.Agent;
import org.demonsoft.spatialkappa.model.AgentSite;
import org.demonsoft.spatialkappa.model.AggregateAgent;
import org.demonsoft.spatialkappa.model.AggregateSite;
import org.demonsoft.spatialkappa.model.Compartment;
import org.demonsoft.spatialkappa.model.CompartmentLink;
import org.demonsoft.spatialkappa.model.Complex;
import org.demonsoft.spatialkappa.model.Direction;
import org.demonsoft.spatialkappa.model.KappaModel;
import org.demonsoft.spatialkappa.model.LocatedObservable;
import org.demonsoft.spatialkappa.model.LocatedTransform;
import org.demonsoft.spatialkappa.model.Location;
import org.demonsoft.spatialkappa.model.Transform;
import org.demonsoft.spatialkappa.model.Transport;
import org.demonsoft.spatialkappa.model.KappaModel.InitialValue;
import org.demonsoft.spatialkappa.parser.SpatialKappaLexer;
import org.demonsoft.spatialkappa.parser.SpatialKappaParser;
import org.demonsoft.spatialkappa.parser.SpatialKappaWalker;


public class SpatialTranslator {

    private static final Map<String, Integer> NO_VARIABLES = new HashMap<String, Integer>();

    private final KappaModel kappaModel;

    public SpatialTranslator(File inputFile) throws Exception {
        if (inputFile == null) {
            throw new NullPointerException();
        }
        kappaModel = getKappaModel(new FileInputStream(inputFile));
    }

    public SpatialTranslator(String input) throws Exception {
        if (input == null) {
            throw new NullPointerException();
        }
        kappaModel = getKappaModel(new ByteArrayInputStream(input.getBytes()));
    }

    public SpatialTranslator(KappaModel kappaModel) {
        if (kappaModel == null) {
            throw new NullPointerException();
        }
        this.kappaModel = kappaModel;
    }

    private KappaModel getKappaModel(InputStream inputStream) throws Exception {
        ANTLRInputStream input = new ANTLRInputStream(inputStream);
        CommonTokenStream tokens = new CommonTokenStream(new SpatialKappaLexer(input));
        SpatialKappaParser.prog_return r = new SpatialKappaParser(tokens).prog();

        CommonTree t = (CommonTree) r.getTree();

        CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
        nodes.setTokenStream(tokens);
        SpatialKappaWalker walker = new SpatialKappaWalker(nodes);
        return walker.prog();
    }

    public String translateToKappa() {
        StringBuilder builder = new StringBuilder();
        // TODO - allow multiple diffusions same name
        // TODO - restrict diffusion agents to unlinked complexes
        for (Transport transport : kappaModel.getTransports()) {
            builder.append(getKappaString(transport));
        }
        builder.append("\n");

        for (LocatedTransform transform : kappaModel.getLocatedTransforms()) {
            builder.append(getKappaString(transform));
        }
        builder.append("\n");

        for (InitialValue initialValue : kappaModel.getInitialValues()) {
            builder.append(getKappaString(initialValue));
        }
        builder.append("\n");

        for (LocatedObservable observable : kappaModel.getLocatedObservables()) {
            builder.append(getKappaString(observable)).append("\n");
        }
        builder.append("\n");
        return builder.toString();
    }

    private List<Agent> getAggregateAgents(Map<String, AggregateAgent> aggregateAgentMap) {
        List<Agent> result = new ArrayList<Agent>();
        for (String agentName : aggregateAgentMap.keySet()) {
            result.add(new Agent(agentName));
        }
        Collections.sort(result, new Comparator<Agent>() {
            public int compare(Agent o1, Agent o2) {
                return o1.name.compareTo(o2.name);
            }
        });
        return result;
    }

    private CompartmentLink getCompartmentLink(List<CompartmentLink> compartmentLinks, String compartmentLinkName) {
        for (CompartmentLink link : compartmentLinks) {
            if (compartmentLinkName.equals(link.getName())) {
                return link;
            }
        }
        return null;
    }

    private String getComplexKappaString(List<Complex> complexes, Location location) {
        StringBuilder builder = new StringBuilder();
        builder.append(getKappaString(complexes.get(0), getKappaString(location)));
        for (int index = 1; index < complexes.size(); index++) {
            builder.append(",").append(getKappaString(complexes.get(index), getKappaString(location)));
        }
        return builder.toString();
    }

    private String getComplexKappaString(List<Complex> complexes, String stateSuffix) {
        StringBuilder builder = new StringBuilder();
        builder.append(getKappaString(complexes.get(0), stateSuffix));
        for (int index = 1; index < complexes.size(); index++) {
            builder.append(",").append(getKappaString(complexes.get(index), stateSuffix));
        }
        return builder.toString();
    }

    String getAgentKappaString(List<Agent> agents, Location location) {
        return getAgentKappaString(agents, getKappaString(location));
    }

    String getAgentKappaString(List<Agent> agents, String stateSuffix) {
        StringBuilder builder = new StringBuilder();
        builder.append(agents.get(0).toString(stateSuffix));
        for (int index = 1; index < agents.size(); index++) {
            builder.append(",").append(agents.get(index).toString(stateSuffix));
        }
        return builder.toString();
    }

    String getKappaString(Complex complex, String agentStateSuffix) {
        return getAgentKappaString(complex.agents, agentStateSuffix);
    }

    String getKappaString(Location location) {
        int dimensionCount = 0;
        if (location != null && location.getIndices() != null && location.getIndices().length > 0) {
            dimensionCount = location.getIndices().length;
        }
        return getKappaString(location, NO_VARIABLES, dimensionCount);
    }

    String getKappaString(Location location, int dimensionCount) {
        return getKappaString(location, NO_VARIABLES, dimensionCount);
    }

    String getKappaString(Location location, Map<String, Integer> variables, int dimensionCount) {
        if (variables == null) {
            throw new NullPointerException();
        }
        if (location == null) {
            return "";
        }
        int usedDimensions = 0;
        if (location.getIndices() != null && location.getIndices().length > 0) {
            usedDimensions = location.getIndices().length;
        }
        if (usedDimensions > dimensionCount) {
            throw new IllegalArgumentException();
        }
        
        StringBuilder builder = new StringBuilder();
        builder.append("loc~").append(location.getName());
        if (dimensionCount == 1) {
            if (usedDimensions == 1) {
                builder.append(",loc_index~").append(location.getIndices()[0].evaluate(variables));
            }
            else {
                builder.append(",loc_index~0");
            }
        }
        else if (dimensionCount > 1) {
            for (int index = 0; index < usedDimensions; index++) {
                builder.append(",loc_index_").append(index + 1).append("~").append(location.getIndices()[index].evaluate(variables));
            }
            for (int index = usedDimensions; index < dimensionCount; index++) {
                builder.append(",loc_index_").append(index + 1).append("~0");
            }
        }
        return builder.toString();
    }

    String getKappaString(LocatedObservable observable) {
        StringBuilder builder = new StringBuilder();
        if (observable.observable.isGeneratedLabel()) {
            builder.append("%obs:");
        }
        else {
            builder.append("%obs: '").append(observable.observable.label).append("'");
        }
        if (observable.observable.complex != null) {
            builder.append(" ").append(getKappaString(observable.observable.complex, getKappaString(observable.location)));
        }
        return builder.toString();
    }
    
    String getKappaString(Transport transport) {
        StringBuilder builder = new StringBuilder();
        CompartmentLink compartmentLink = getCompartmentLink(kappaModel.getCompartmentLinks(), transport.getCompartmentLinkName());
        String[][] stateSuffixPairs = getLinkStateSuffixPairs(compartmentLink, kappaModel.getCompartments());
        List<Agent> agents = transport.getAgents();
        if (agents != null) {
            writeAgents(builder, stateSuffixPairs, transport, compartmentLink, agents, 1, kappaModel.getAggregateAgentMap());
        }
        else {
            int labelSuffix = 1;
            agents = getAggregateAgents(kappaModel.getAggregateAgentMap());
            List<Agent> currentAgents = new ArrayList<Agent>();
            for (Agent agent : agents) {
                currentAgents.clear();
                currentAgents.add(agent);
                writeAgents(builder, stateSuffixPairs, transport, compartmentLink, currentAgents, labelSuffix, kappaModel.getAggregateAgentMap());
                labelSuffix += stateSuffixPairs.length;
            }
        }
        return builder.toString();
    }
    
    private void writeAgents(StringBuilder builder, String[][] stateSuffixPairs, Transport transport, CompartmentLink compartmentLink, List<Agent> agents,
            int startLabelSuffix, Map<String, AggregateAgent> aggregateAgentMap) {

        int labelSuffix = startLabelSuffix;
        List<Agent> isolatedAgents = getIsolatedAgents(agents, aggregateAgentMap);

        for (int index = 0; index < stateSuffixPairs.length; index++) {
            if (transport.label != null) {
                builder.append("'").append(transport.label);
                if (stateSuffixPairs.length > 1) {
                    builder.append("-").append(labelSuffix++);
                }
                builder.append("' ");
            }
            String leftSuffix = (compartmentLink.getDirection() != Direction.BACKWARD) ? stateSuffixPairs[index][0] : stateSuffixPairs[index][1];
            String rightSuffix = (compartmentLink.getDirection() != Direction.BACKWARD) ? stateSuffixPairs[index][1] : stateSuffixPairs[index][0];
            Direction direction = (compartmentLink.getDirection() == Direction.BIDIRECTIONAL) ? Direction.BIDIRECTIONAL : Direction.FORWARD;
            builder.append(getAgentKappaString(isolatedAgents, leftSuffix));
            builder.append(" ").append(direction).append(" ");
            builder.append(getAgentKappaString(isolatedAgents, rightSuffix)).append(" @ ");
            builder.append(transport.getRateText());
            if (compartmentLink.getDirection() == Direction.BIDIRECTIONAL) {
                builder.append(",").append(transport.getRateText());
            }
            builder.append("\n");
        }
    }

    private List<Agent> getIsolatedAgents(List<Agent> agents, Map<String, AggregateAgent> aggregateAgentMap) {
        List<Agent> result = new ArrayList<Agent>();
        for (Agent agent : agents) {
            List<AgentSite> isolatedSites = new ArrayList<AgentSite>();
            isolatedSites.addAll(agent.getSites()); // Sites are cloned - no
                                                    // problem here

            AggregateAgent aggregateAgent = aggregateAgentMap.get(agent.name);
            for (AggregateSite site : aggregateAgent.getSites()) {
                if (site.getLinks().size() > 0 && agent.getSite(site.getName()) == null) {
                    isolatedSites.add(new AgentSite(site.getName(), null, null));
                }
            }
            result.add(new Agent(agent.name, isolatedSites));
        }
        return result;
    }


    // Transforms
    String getKappaString(LocatedTransform transition) {
        StringBuilder builder = new StringBuilder();
        Transform transform = (Transform) transition.transition;
        boolean partition = false;
        Compartment compartment = null;
        Location reference = transition.sourceLocation;

        if (reference != null) {
            compartment = reference.getReferencedCompartment(kappaModel.getCompartments());
            if (compartment.getDimensions().length != reference.getIndices().length) {
                partition = true;
            }
        }

        if (partition && compartment != null) {
            String[] stateSuffixes = compartment.getCellStateSuffixes();
            int labelSuffix = 1;
            for (int cellIndex = 0; cellIndex < stateSuffixes.length; cellIndex++) {
                if (transform.label != null) {
                    builder.append("'").append(transform.label).append("-").append(labelSuffix++).append("' ");
                }
                if (transform.leftAgents.size() > 0) {
                    builder.append(getAgentKappaString(transform.leftAgents, stateSuffixes[cellIndex])).append(" ");
                }
                builder.append("-> ");
                if (transform.rightAgents.size() > 0) {
                    builder.append(getAgentKappaString(transform.rightAgents, stateSuffixes[cellIndex])).append(" ");
                }
                builder.append("@ ").append(transform.getRateText()).append("\n");

            }
        }
        else {
            if (transform.label != null) {
                builder.append("'").append(transform.label).append("' ");
            }
            if (transform.leftAgents.size() > 0) {
                builder.append(getAgentKappaString(transform.leftAgents, transition.sourceLocation)).append(" ");
            }
            builder.append("-> ");
            if (transform.rightAgents.size() > 0) {
                builder.append(getAgentKappaString(transform.rightAgents, transition.sourceLocation)).append(" ");
            }
            builder.append("@ ").append(transform.getRateText()).append("\n");
        }
        return builder.toString();

    }
    
    String getKappaString(InitialValue initialValue) {
        StringBuilder builder = new StringBuilder();
        boolean partition = false;
        Compartment compartment = null;
        Location location = initialValue.location;
        if (location != null) {
            compartment = location.getReferencedCompartment(kappaModel.getCompartments());
            if (compartment.getDimensions().length != location.getIndices().length) {
                partition = true;
            }
        }

        if (partition && compartment != null) {
            int[] cellCounts = compartment.getDistributedCellCounts(initialValue.quantity);
            String[] stateSuffixes = compartment.getCellStateSuffixes();

            for (int cellIndex = 0; cellIndex < cellCounts.length; cellIndex++) {
                builder.append("%init: ").append(cellCounts[cellIndex]).append(" * (");
                builder.append(getComplexKappaString(initialValue.complexes, stateSuffixes[cellIndex]));
                builder.append(")\n");
            }
        }
        else {
            builder.append("%init: ").append(initialValue.quantity).append(" * (");
            builder.append(getComplexKappaString(initialValue.complexes, getKappaString(location)));
            builder.append(")\n");
        }
        return builder.toString();
    }

    String[][] getLinkStateSuffixPairs(CompartmentLink link, List<Compartment> compartments) {
        Location[][] references = link.getCellReferencePairs(compartments);
        String[][] result = new String[references.length][2];
        int maxDimensions = 0;
        for (Compartment compartment : compartments) {
            if (compartment.getDimensions() != null && compartment.getDimensions().length > maxDimensions) {
                maxDimensions = compartment.getDimensions().length;
            }
        }
        for (int index = 0; index < references.length; index++) {
            result[index][0] = getKappaString(references[index][0], maxDimensions);
            result[index][1] = getKappaString(references[index][1], maxDimensions);
        }
        return result;
    }

    
    public static void main(String[] args) throws Exception {
        if (args.length < 1 && args.length > 2) {
            System.err.println("SpatialTranslator version " + Version.VERSION);
            System.err.println("Usage: SpatialTranslator <input file path> [<output file path>]");
            return;
        }

        String result = new SpatialTranslator(new File(args[0])).translateToKappa();
        if (args.length == 1) {
            System.out.println(result);
        }
        else {
            FileUtils.writeStringToFile(new File(args[1]), result);
            System.out.println("Result written to " + args[1]);
        }
    }


}