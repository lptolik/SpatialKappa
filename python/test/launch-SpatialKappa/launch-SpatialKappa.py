import SpatialKappa
import os

sk = SpatialKappa.SpatialKappa()
sim = sk.kappa_sim("ms", True)
sim.loadFile("caPump.ka")

sim.runForTime(0.01)
print('Time: %4.3f. %5.0f molecules of P %5.0f molecules of Ca' % (sim.getTime(), sim.getObservation("P"), sim.getObservation("ca")))

## Add an integer number of Ca ions (but given as a float
sim.addAgent('ca', 11.0) 
print('Time: %4.3f. %5.0f molecules of P %5.0f molecules of Ca, 11 more than before' % (sim.getTime(), sim.getObservation("P"), sim.getObservation("ca")))

## Adding a non-integer number of ions should throw an error:
try:
    sim.addAgent('ca', 11.5) 
except:
    print "Error was raised correctly"

sim.runUntilTime(0.1)
print('Time: %4.3f. %5.0f molecules of P %5.0f molecules of Ca' % (sim.getTime(), sim.getObservation("P"), sim.getObservation("ca")))

## Add an integer number of Ca ions specified as an integer
sim.addAgent('ca', 10)

sim.runForTime(0.01)
print('Time: %4.3f. %5.0f molecules of P %5.0f molecules of Ca' % (sim.getTime(), sim.getObservation("P"), sim.getObservation("ca")))

