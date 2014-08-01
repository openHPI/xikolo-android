package de.xikolo.util;

import android.app.Activity;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ProgressBarAnimator {

    public static void start(final Activity activity, final ProgressBar progressBar, final int state, final int max) {
        start(activity, progressBar, null, state, max);
    }

    public static void start(final Activity activity, final ProgressBar progressBar, final TextView textLabel, final int state, final int max) {
        (new Thread(new Runnable() {
            @Override
            public void run() {

                final int percentage;
                if (max > 0) {
                    percentage = (int) (state/ (max / 100.));
                } else {
                    percentage = 100;
                }

                if (textLabel != null) {
                    textLabel.setText("0%");
                }

                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(percentage);
                            if (textLabel != null) {
                                textLabel.setText(percentage + "%");
                            }
                        }
                    });
                }
                final int animTime = (int) (500. / percentage);
                for (int i = 0; i <= percentage; i++) {
                    final int p = i;
                    try {
                        Thread.sleep(animTime);
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setProgress(p);
                                if (textLabel != null) {
                                    textLabel.setText(p + "%");
                                }
                            }
                        });
                    } catch (InterruptedException e) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setProgress(percentage);
                                if (textLabel != null) {
                                    textLabel.setText(percentage + "%");
                                }
                            }
                        });
                    }
                }

            }
        })).start();
    }

}
