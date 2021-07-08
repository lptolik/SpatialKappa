import SpatialKappa
import os

sk = SpatialKappa.SpatialKappa()
sim = sk.kappa_sim("ms", True)
sim.loadFile("caPump.ka")

ca_agent = sim.getAgent("ca")
print(ca_agent)
tran = sim.addTransition("TEST", ca_agent, 0.0)

sim.runForTime(100.0, False)
print('Time: %4.3f. %5.0f molecules of P %5.0f molecules of Ca' % (sim.getTime(), sim.getObservation("P"), sim.getObservation("ca")))

sim.setTransitionRate("TEST", 100.0)
print(sim.getTransition("TEST"))

sim.runForTime(100.0, False)
print('Time: %4.3f. %5.0f molecules of P %5.0f molecules of Ca' % (sim.getTime(), sim.getObservation("P"), sim.getObservation("ca")))

