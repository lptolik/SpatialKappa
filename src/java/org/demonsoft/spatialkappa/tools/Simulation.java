package org.demonsoft.spatialkappa.tools;

import org.demonsoft.spatialkappa.model.Observation;
import org.demonsoft.spatialkappa.model.ObservationListener;

public interface Simulation {

    Observation getCurrentObservation();

    void removeObservationListener(ObservationListener listener);

    void addObservationListener(ObservationListener listener);

    void runByEvent(int steps, int stepSize);

    void runByTime(double steps, double stepSize);

    void runByTime2(double stepEndTime, boolean progress) throws Exception;

    void stop();

    String getDebugOutput();
}
