package org.demonsoft.spatialkappa.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class Complex implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private static ComplexMatcher matcher = new ComplexMatcher();
    private static final List<AgentLink> NO_LINKS = new ArrayList<AgentLink>();
    
    public final List<Agent> agents = new ArrayList<Agent>();
    List<AgentLink> agentLinks = new ArrayList<AgentLink>();
    Map<Agent, List<AgentLink>> linksPerAgent = new HashMap<Agent, List<AgentLink>>();
    private String matchHash;

    public Complex(Agent... agents) {
        this(Arrays.asList(agents));
    }

    public Complex(Collection<Agent> agents) {
        if (agents == null) {
            throw new NullPointerException();
        }
        if (agents.size() == 0) {
            throw new IllegalArgumentException();
        }
        this.agents.addAll(agents);
        for (Agent agent : agents) {
            agent.setComplex(this);
        }
        canonicalSortAgents();
        createAgentLinks();
        updateMatchHash();
        verify();
    }

    void updateMatchHash() {
        StringBuilder builder = new StringBuilder();
        Agent previous = null;
        int count = 0;
        for (Agent current : agents) {
            if (previous == null) {
                count = 1;
            }
            else if (current.name.equals(previous.name)) {
                count++;
            }
            else {
                builder.append(previous.name).append(count);
                count = 1;
            }
            previous = current;
        }
        if (previous == null) {
            matchHash = "";
            return;
        }
        builder.append(previous.name).append(count);
        
        // Add links
        Map<String, Integer> linkHashMap = new HashMap<String, Integer>();
        for (AgentLink link : agentLinks) {
            if (link.targetSite.agent != null) {
                String sourceAgent = link.sourceSite.agent.name;
                String targetAgent = link.targetSite.agent.name;
            
                int order = sourceAgent.compareTo(targetAgent);
                String hash;
                if (order < 0) {
                    hash = sourceAgent + link.sourceSite.name + targetAgent + link.targetSite.name;
                }
                else if (order > 0) {
                    hash = targetAgent + link.targetSite.name + sourceAgent + link.sourceSite.name;
                }
                else {
                    String sourceSite = link.sourceSite.name;
                    String targetSite = link.targetSite.name;
                    if (sourceSite.compareTo(targetSite) < 0) {
                        hash = sourceAgent + sourceSite + targetAgent + targetSite;
                    }
                    else {
                        hash = targetAgent + targetSite + sourceAgent + sourceSite;
                    }
                }
                if (linkHashMap.containsKey(hash)) {
                    linkHashMap.put(hash, linkHashMap.get(hash) + 1);
                }
                else {
                    linkHashMap.put(hash, 1);
                }
            }
        }
        List<String> linkHashKeys = new ArrayList<String>(linkHashMap.keySet());
        Collections.sort(linkHashKeys);
        for (String key : linkHashKeys) {
            builder.append(key).append(linkHashMap.get(key));
        }
        matchHash = builder.toString();
    }
    
    public String getMatchHash() {
        return matchHash;
    }

    private Complex(Complex complex) {
        for (Agent agent : complex.agents) {
            agents.add(agent.clone());
        }
        for (Agent agent : agents) {
            agent.setComplex(this);
        }
        createAgentLinks();
        updateMatchHash();
        verify();
    }

    private void createAgentLinks() {
        Map<String, AgentSite[]> links = new HashMap<String, AgentSite[]>();
        for (Agent agent : agents) {
            for (AgentSite site : agent.getSites()) {
                if (site.isNamedLink()) {
                    AgentSite[] link = links.get(site.getLinkName());
                    if (link == null) {
                        links.put(site.getLinkName(), new AgentSite[] { site, null });
                    }
                    else {
                        if (link[1] != null) {
                            throw new IllegalArgumentException("Mismatched links: " + toString());
                        }
                        link[1] = site;
                    }
                }
                else if (site.getLinkName() == null) {
                    addAgentLink(AgentLink.getNoneLink(site));
                }
                else if (site.getLinkName().equals("?")) {
                    addAgentLink(AgentLink.getAnyLink(site));
                }
                else if (site.getLinkName().equals("_")) {
                    addAgentLink(AgentLink.getOccupiedLink(site));
                }
            }
        }

        for (AgentSite[] link : links.values()) {
            if (link[1] == null) {
                throw new IllegalArgumentException("Mismatched links: " + toString());
            }
            addAgentLink(new AgentLink(link[0], link[1]));
        }
    }


    private void addAgentLink(AgentLink link) {
        Agent sourceAgent = link.sourceSite.agent;
        List<AgentLink> links = linksPerAgent.get(sourceAgent);
        if (links == null) {
            links = new ArrayList<AgentLink>();
            linksPerAgent.put(sourceAgent, links);
        }
        links.add(link);
        
        Agent targetAgent = link.targetSite.agent;
        if (targetAgent != null) {
            links = linksPerAgent.get(targetAgent);
            if (links == null) {
                links = new ArrayList<AgentLink>();
                linksPerAgent.put(targetAgent, links);
            }
            links.add(link);
        }
        agentLinks.add(link);
    }

    private void canonicalSortAgents() {
        Collections.sort(agents, new Comparator<Agent>() {
            public int compare(Agent o1, Agent o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
    }

    @Override
    public String toString() {
        return agents.toString();
    }

    public List<AgentLink> getAgentLinks(Agent agent) {
        if (linksPerAgent.containsKey(agent)) {
            return linksPerAgent.get(agent);
        }
        return NO_LINKS;
    }

    @Override
    public Complex clone() {
        return new Complex(this);
    }

    public void addAgent(Agent agent) {
        if (agent.getComplex() != null) {
            agent.getComplex().deleteAgent(agent);
        }
        agents.add(agent);
        agent.setComplex(this);
        canonicalSortAgents();
        updateMatchHash();
        verify();
    }

    public void deleteLink(Agent agent, String name) {
        for (AgentLink link : getAgentLinks(agent)) {
            if (link.sourceSite.agent == agent && link.sourceSite.name.equals(name) || link.targetSite.agent == agent
                    && link.targetSite.name.equals(name)) {
                deleteLink(link, true);
                updateMatchHash();
                verify();
                return;
            }
        }
    }

    private void deleteLink(AgentLink link, boolean tidy) {
        linksPerAgent.get(link.sourceSite.agent).remove(link);
        link.sourceSite.setLinkName(null);
        if (link.targetSite.agent != null) {
            linksPerAgent.get(link.targetSite.agent).remove(link);
            link.targetSite.setLinkName(null);
        }//TODO test here
        agentLinks.remove(link);

        if (tidy) {
            addAgentLink(AgentLink.getNoneLink(link.sourceSite));
            if (link.targetSite.agent != null) {
                addAgentLink(AgentLink.getNoneLink(link.targetSite));
            }
        }
    }

    public Complex splitComplex() {
        Complex result = null;
        Agent firstAgent = agents.get(0);
        Queue<Agent> keepAgents = new LinkedList<Agent>();
        keepAgents.add(firstAgent);
        Set<Agent> splitAgents = new HashSet<Agent>(agents);
        splitAgents.remove(firstAgent);
        Set<AgentLink> splitLinks = new HashSet<AgentLink>(agentLinks);
        while (!keepAgents.isEmpty()) {
            Agent current = keepAgents.poll();
            List<AgentLink> links = getAgentLinks(current);
            splitLinks.removeAll(links);
            for (AgentLink link : links) {
                if (splitAgents.contains(link.sourceSite.agent)) {
                    keepAgents.add(link.sourceSite.agent);
                    splitAgents.remove(link.sourceSite.agent);
                }
                if (link.targetSite.agent != null && splitAgents.contains(link.targetSite.agent)) {
                    keepAgents.add(link.targetSite.agent);
                    splitAgents.remove(link.targetSite.agent);
                }
            }
        }
        agentLinks.removeAll(splitLinks);
        agents.removeAll(splitAgents);
        for (Agent agent : splitAgents) {
            linksPerAgent.remove(agent);
        }
        updateMatchHash();
        verify();

        if (!splitAgents.isEmpty()) {
            result = new Complex(splitAgents);
        }
        return result;
    }

    public void deleteAgent(Agent agent) {
        List<AgentLink> links = new ArrayList<AgentLink>(getAgentLinks(agent));
        for (AgentLink link : links) {
            if (link.targetSite != AgentLink.NONE && link.targetSite != AgentLink.ANY && link.targetSite != AgentLink.OCCUPIED) {
                deleteLink(link, true);
            }
        }
        agents.remove(agent);
        agent.setComplex(null);
        updateMatchHash();
        verify();
    }

    public void verify() {
        Map<String, AgentSite[]> links = new HashMap<String, AgentSite[]>();
        for (Agent agent : agents) {
            for (AgentSite site : agent.getSites()) {
                if (site.isNamedLink()) {
                    AgentSite[] link = links.get(site.getLinkName());
                    if (link == null) {
                        links.put(site.getLinkName(), new AgentSite[] { site, null });
                    }
                    else {
                        if (link[1] != null) {
                            throw new IllegalStateException("Mismatched links: " + toString());
                        }
                        link[1] = site;
                    }
                }
            }
        }

        for (AgentSite[] link : links.values()) {
            if (link[1] == null) {
                throw new IllegalStateException("Mismatched links: " + toString());
            }
        }
    }

    public AgentLink getAgentLink(Agent agent, String siteName) {
        for (AgentLink current : getAgentLinks(agent)) {
            if ((agent == current.sourceSite.agent && current.sourceSite.name.equals(siteName))
                    || (current.targetSite != null && agent == current.targetSite.agent && current.targetSite.name.equals(siteName))) {
                return current;
            }
        }
        return null;
    }

    public void mergeComplex(Complex complex) {
        agentLinks.addAll(complex.agentLinks);
        agents.addAll(complex.agents);
        linksPerAgent.putAll(complex.linksPerAgent);
        for (Agent agent : complex.agents) {
            agent.setComplex(this);
        }
        canonicalSortAgents();
        complex.agentLinks.clear();
        complex.agents.clear();
        complex.linksPerAgent.clear();
        updateMatchHash();
        verify();
    }

    public void incrementLinkNames(int increment) {
        for (AgentLink link : agentLinks) {
            if (link.targetSite != AgentLink.NONE && link.targetSite != AgentLink.ANY && link.targetSite != AgentLink.OCCUPIED) {
                link.sourceSite.setLinkName("" + (Integer.parseInt(link.sourceSite.getLinkName()) + increment));
                link.targetSite.setLinkName("" + (Integer.parseInt(link.targetSite.getLinkName()) + increment));
            }
        }
    }
    
    public void createAgentLink(AgentSite sourceSite, AgentSite targetSite, String linkID) {
        AgentLink currentLink = getAgentLink(sourceSite.agent, sourceSite.name);
        if (currentLink != null) {
            deleteLink(currentLink, false);
        }
        if (targetSite.agent != null) {
            currentLink = getAgentLink(targetSite.agent, targetSite.name);
            if (currentLink != null) {
                deleteLink(currentLink, false);
            }
            
        }
        addAgentLink(new AgentLink(sourceSite, targetSite));
        
        if (targetSite == AgentLink.ANY) {
            sourceSite.setLinkName("?");
        }
        else if (targetSite == AgentLink.NONE) {
            sourceSite.setLinkName(null);
        }
        else if (targetSite == AgentLink.OCCUPIED) {
            sourceSite.setLinkName("_");
        }
        else {
            sourceSite.setLinkName(linkID);
            targetSite.setLinkName(linkID);
        }

        updateMatchHash();
        verify();
    }

    public int renumberLinkNames(int startNumber) {
        int current = startNumber;
        for (AgentLink link : agentLinks) {
            if (link.targetSite != AgentLink.NONE && link.targetSite != AgentLink.ANY && link.targetSite != AgentLink.OCCUPIED) {
                String newLinkName = "" + (current++);
                link.sourceSite.setLinkName(newLinkName);
                link.targetSite.setLinkName(newLinkName);
            }
        }
        return current - 1;
    }
    
    public boolean isExactMatch(Complex other) {
        return matcher.isExactMatch(this, other);
    }


}
