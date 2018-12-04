import SpatialKappa
import os
import unittest
from py4j.protocol import * 

class TestSpatialKappa(unittest.TestCase):
    
    def setUp(self):
        self.flog = open("/tmp/SpatialKappa-test.log", "w")
        self.sk = SpatialKappa.SpatialKappa(redirect_stdout=self.flog)
        self.sim = self.sk.kappa_sim("ms", True)
        self.sim.loadFile(os.path.dirname(SpatialKappa.__file__) + "/tests/caPump.ka")

    def test_setSeed(self):
        flog1 = open("/tmp/SpatialKappa-seed-1.log", "w")
        sk = SpatialKappa.SpatialKappa(redirect_stdout=flog1)
        sim1 = sk.kappa_sim("ms", True, 1)
        sim1.loadFile(os.path.dirname(SpatialKappa.__file__) + "/tests/caPump.ka")
        sim1.runForTime(5.0, True)

        flog2 = open("/tmp/SpatialKappa-seed-2.log", "w")
        sk2 = SpatialKappa.SpatialKappa(redirect_stdout=flog2)
        sim2 = sk2.kappa_sim("ms", True, 1)
        sim2.loadFile(os.path.dirname(SpatialKappa.__file__) + "/tests/caPump.ka")
        sim2.runForTime(5.0, True)

        self.assertEqual(sim1.getVariable('ca'), sim2.getVariable('ca'))
        self.assertEqual(sim1.getVariable('P-Ca'), sim2.getVariable('P-Ca'))
        self.assertEqual(sim1.getVariable('P'), sim2.getVariable('P'))        

        sim1 = []
        sk1 = []
        sim2 = []
        sk2 = []

        flog3 = open("/tmp/SpatialKappa-seed-3.log", "w")
        sk = SpatialKappa.SpatialKappa(redirect_stdout=flog3)
        sim = sk.kappa_sim("ms", True)
        sim.loadFile(os.path.dirname(SpatialKappa.__file__) + "/tests/caPump.ka")
        sim.runForTime(5.0, True)
        sim = []
        sk = []

        ## flog1.close()
        ## flog2.close()
        ## flog3.close()
        
    def test_createSpatialKappa(self):
        self.sk = SpatialKappa.SpatialKappa()

    def test_runForTime(self):
        self.sim.runForTime(100.0, False)
        self.assertEqual(self.sim.getTime(), 100.0)

    def test_addTransition(self):
        ca_agent = self.sim.getAgent("ca")
        tran = self.sim.addTransition("TEST", {}, {"ca": {"x": {}}}, 0.0)
        self.assertEqual(str(tran), "'TEST' : [] -> [[ca(x)]] @ 0.0")
        self.sim.setTransitionRateOrVariable("TEST", 100.0)
        self.assertEqual(str(tran), "'TEST' : [] -> [[ca(x)]] @ 100.0")
        tran2 = self.sim.addTransition("TEST2", {}, {"ca": {}}, 66.0)
        self.assertEqual(str(tran2), "'TEST2' : [] -> [[ca(x)]] @ 66.0")
        tran3 = self.sim.addTransition("TEST3", {"P": {}}, {}, 77.0)
        self.assertEqual(str(tran3), "'TEST3' : [[P(x)]] -> [] @ 77.0")


    def test_addAgent(self):
        obs = self.sim.getObservation("ca")
        self.assertTrue(isinstance(obs, float))
        ## Add an integer number of Ca ions specified as a float
        self.sim.addAgent('ca', 11.0) 
        obs2 = self.sim.getObservation("ca")
        self.assertEqual(obs2, obs + 11.0)
        ## Add an integer number of Ca ions specified as an integer
        self.sim.addAgent('ca', 10) 
        obs3 = self.sim.getObservation("ca")
        self.assertEqual(obs3, obs2 + 10)

        ## Somthing like this should work, but it doesn't.
        ## self.assertRaises(TypeError, random.shuffle, (1,2,3))
        ## Hence the hack below
        error = False
        try:
            self.sim.addAgent("ca", 11.5)
        except Py4JJavaError:
            error = True
        self.assertTrue(error)

    def test_addVariable(self):
        ## 1. Add agent expression
        ## Create an error due to nonexistent site name
        error = False
        try:
            self.sim.addVariable("test", {"ca": {"nonexistent_site_name": {"l": "?"}}})
        except Py4JJavaError:
            error = True
        self.assertTrue(error)
        
        ## Create an error due to nonexistent site attribute
        error = False
        try:
            self.sim.addVariable("test", {"ca": {"x": {"nonexistent_site_attribute": "?"}}})
        except Py4JJavaError:
            error = True
        self.assertTrue(error)

        self.sim.addVariable('TotCa2', {"ca": {"x": {"l": "?"}}})
        TotCa1 = self.sim.getVariable("TotCa")
        TotCa2 = self.sim.getVariable("TotCa2")
        self.assertEqual(TotCa1, TotCa2)
        self.sim.addVariable('P-Ca2', {"ca": {"x": {"l": "1"}}, "P": {"x": {"l": "1"}}})
        PCa1 = self.sim.getVariable("P-Ca")
        PCa2 = self.sim.getVariable("P-Ca2")
        self.assertEqual(PCa1, PCa2)

        ## print PCa1, PCa2

        ## 2. Add float variable
        self.sim.addVariable("V", 9.0)

    def test_getAgentMap(self):
        agent_map = self.sim.getAgentMap("ca")
        self.assertEqual(agent_map.keys(), [u'ca'])
        self.assertEqual(agent_map[u'ca'].keys(), [u'x'])
        self.assertEqual(len(agent_map[u'ca'][u'x']), 0)

    # Test exception thrown with faulty Kappa file
    def test_noInit(self):
        self.sk = SpatialKappa.SpatialKappa()
        self.sim = self.sk.kappa_sim("ms", True)
        self.sim.loadFile(os.path.dirname(SpatialKappa.__file__) + "/tests/no_init.ka")
        try:
            self.sim.getAgent('ca')
        except Py4JJavaError:
            error = True
        self.assertTrue(error)

    def tearDown(self):
        self.sim = []
        self.sk = []
        ## self.flog.close()
        
if __name__ == '__main__':
    unittest.main()