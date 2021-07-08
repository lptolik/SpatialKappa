name = "SpatialKappa"

import py4j.java_gateway as jg
import os

class SpatialKappa:
    """Runs Java gateway to SpatialKappa"""

    port = None
    gateway = None
    gateway_client = None

    count = 0
    "Count of SpatialKappa objects"
    
    def __init__(self, classpath='', redirect_stdout=None):
        """Create and return a gateway to a Java object with SpatialKappa in
        the classpath using py4j

        The distribution of SpatialKappa should contain the
        SpatialKappa jar file, so there should be no need to download
        it separately.
        
        Keyword arguments:
        classpath -- classpath to override the default values
        redirect_stdout -- Redirect stdout to this connection - see
          py4j.java_gateway.launch_gateway()

        """
        
        skjar_file = 'SpatialKappa-v2.1.5.jar'
        antlrjar_file = 'ant-antlr-3.2.jar'
        skjar_path = os.path.join(os.path.dirname(os.path.realpath(__file__)), 'share', 'SpatialKappa', skjar_file)
        antlrjar_path = os.path.join(os.path.dirname(os.path.realpath(__file__)), 'share', 'SpatialKappa', antlrjar_file)
        classpath = os.pathsep.join((classpath, skjar_path, antlrjar_path))
        self.id = SpatialKappa.count
        SpatialKappa.count = SpatialKappa.count + 1
        self.redirect_stdout = redirect_stdout
        self.port = jg.launch_gateway(classpath=classpath, die_on_exit=False, redirect_stdout=self.redirect_stdout)
        self.gateway_client = jg.GatewayClient(port=self.port)
        self.gateway = jg.JavaGateway(self.gateway_client, auto_convert=True)

    def kappa_sim(self, time_units, verbose, seed=None):
        """Create and return a SpatialKappa simulation. This is actually a Java object.

        Keyword arguments:
        time_units -- Time units of the simulation. Can be "s" (seconds)
          or "ms" (milliseconds)
        verbose -- If True, more output is printed to the connection
          specified by redirect_stdout of the parent SpatialKappa object
        seed -- Random seed
        
        The object created has the following methods:

        MODEL BUILDING METHODS

        * loadFile(String kappaFileName) -- Load a SpatialKappa file "kappaFileName" into the simulation

        * addAgentDeclaration(String name, dict sites) -- Declare the
          sites and states of an agent with name. Sites is in the format:
          {'site1': ['state1', 'state2', ...], 'site2': ['state1', 'state2', ...]}

        * isAgent(String name) -- Return True if "name" has been declared as an agent

        * getAgentDeclaration(String name) -- Return agent delcaration
          in same format as addAgentDeclaration()

        * addInitialValue(dict complex, int|double value) -- Set the
          intial value of a complex specified using the following dict
          structure in python:

            {agent_name1: {site_name1: {"l": link_name, "s": state_name}, 
                           site_name2: {"l": link_name, "s": state_name}, ...}, 
             agent_name2: {site_name1: {"l": link_name, "s": state_name}, ...}, ...}

          This is equivalent to 
             %init: complex value

          in SpatialKappa. This function preseves the behaviour of the
          effect of %init lines summing. For example 

            addInitialValue({'ca': {'x': {}}}, 20)
            addInitialValue({'ca': {'x': {}}}, 20)

          has the effect of adding 40 ca(x), just as would:

             %init: ca(x) 20
             %init: ca(x) 20

        * overrideInitialValue(dict complex, int|double value) --
          The same as addInitialValue(), but overrides any existing
          initial value for an complex.

        * addTransition(String label, dict leftSideAgents, dict rightSideAgents, float rate) -- 
          Create unidirectional transition labelled "label" with
          "leftSideAgents" and "rightSideAgents" specified using format
          described in setInitialValue() and propensity "rate"

        * addVariable(String name, dict complex) -- Create a variable
          "name" referencing "complex" using the syntax as described
          in addInitialValue()

        * addVariable(String name, float input) -- Create a
          variable "name" with value "input"

        * isVariable(String name) -- Return True if "name" has been
          declared as a variable

        RUNTIME METHODS
        
        * initialiseSim() -- Initialise the simulation
        
        * runUntilTime(float stepEndTime, boolean progress) -- Run
          simulation from current simulation time to "stepEndTime"
          (specified in "time_units" given when creating kappa_sim.
          Time progress shown if "progress" is True. Initialises if
          not already initialised.

        * runForTime(float dt, boolean progress) -- Run simulation
          from current simulation time for another "dt" (specified in
          "time_units" given when creating kappa_sim. Time progress
          shown if "progress" is True. Initialises if not already
          initialised.

        * getTime() -- Return time of SpatialKappa simulation

        * getVariable(String name) -- Get value of "name"

        * getVariables() -- Return variables. If "kappa_sim.verbose"
          is True, print values

        * getObservation(String name) -- get Observation "name"

        * setTransitionRateOrVariable(String label, float value) -- Set rate of
          Transition "label" or of Variable "label" to "value"

        * addAgent(String name, int value) -- Add "value" units of
          agent "name" in its default configuration
        
        * addAgent(String name, double value) -- Same as the addAgent
          with an int "value", but throws an error if non-int value
          given

        """
        ks = self.gateway.jvm.org.demonsoft.spatialkappa.api.SpatialKappaSim(time_units, verbose, seed)
        return ks

    def __del__(self):
        self.gateway_client.shutdown_gateway()
        SpatialKappa.count = SpatialKappa.count - 1

