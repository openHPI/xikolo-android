package de.xikolo.managers.jobs;

import android.util.Log;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;
import com.birbit.android.jobqueue.log.CustomLogger;

import de.xikolo.GlobalApplication;

public abstract class JobHelper {

    private static JobManager instance;

    private JobHelper() {
    }

    public synchronized static JobManager getJobManager() {
        if (instance == null) {
            int numThreads = Runtime.getRuntime().availableProcessors();

            Configuration configuration = new Configuration.Builder(GlobalApplication.getInstance())
                    .customLogger(new CustomLogger() {
                        private final String TAG = JobManager.class.getSimpleName();

                        @Override
                        public boolean isDebugEnabled() {
                            return false;
                        }

                        @Override
                        public void v(String text, Object... args) {
//                            if (Config.DEBUG) Log.v(TAG, String.format(text, args));
                        }

                        @Override
                        public void d(String text, Object... args) {
//                        if (Config.DEBUG) Log.d(TAG, String.format(text, args));
                        }

                        @Override
                        public void e(Throwable t, String text, Object... args) {
                            Log.e(TAG, String.format(text, args), t);
                        }

                        @Override
                        public void e(String text, Object... args) {
                            Log.e(TAG, String.format(text, args));
                        }
                    })
                    .minConsumerCount(numThreads > 3 ? 3 : numThreads) // always keep at least one consumer alive
                    .maxConsumerCount(numThreads) // consumers at a time
                    .loadFactor(2) // jobs per consumer
                    .consumerKeepAlive(120) // wait 2 minute
                    .build();

            instance = new JobManager(configuration);
        }
        return instance;
    }

}
