package de.xikolo.lanalytics;

import java.util.List;

import de.xikolo.lanalytics.database.DatabaseHelper;
import de.xikolo.lanalytics.database.access.EventDataAccess;

public class Tracker {

    private EventDataAccess eventDataAccess;

    private Thread networker;

    Tracker(DatabaseHelper databaseHelper) {
        this.eventDataAccess = (EventDataAccess) databaseHelper.getDataAccess(DatabaseHelper.DataAccessType.EVENT);
    }

    public void track(final Lanalytics.Event event) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                eventDataAccess.add(event);
            }
        }).start();
    }


    private class Networker implements Runnable {

        @Override
        public void run() {
            List<Lanalytics.Event> events = eventDataAccess.getTop(50);
        }

    }

}
