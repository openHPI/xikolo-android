package de.xikolo.controller;

import android.app.Application;
import android.net.http.HttpResponseCache;
import android.util.Log;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.io.IOException;

import de.xikolo.R;
import de.xikolo.util.Config;
import de.xikolo.util.FontsOverride;

public class GlobalApplication extends Application {

    public static final String TAG = GlobalApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        // set global typefaces
        // use xml attr. android:typeface="serif" for bold font style
        FontsOverride.setDefaultFont(this, "SANS_SERIF", Config.FONT_SANS);
        FontsOverride.setDefaultFont(this, "SERIF", Config.FONT_SANS_BOLD);

        // Create global configuration and initialize ImageLoader with this configuration
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .showImageOnLoading(R.color.gray_text)
                .showImageForEmptyUri(R.color.gray_text)
                .showImageOnFail(R.color.gray_text)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .discCacheSize(30 * 1024 * 1024) // 30 MiB
                .build();
        ImageLoader.getInstance().init(config);

        // Create HTTP Response Cache
        try {
            File httpCacheDir = new File(this.getCacheDir(), "http");
            long httpCacheSize = 20 * 1024 * 1024; // 20 MiB
            HttpResponseCache.install(httpCacheDir, httpCacheSize);
        } catch (IOException e) {
            Log.i(TAG, "HTTP response cache installation failed:" + e);
        }
    }

    public void flushCache() {
        HttpResponseCache cache = HttpResponseCache.getInstalled();
        if (cache != null) {
            cache.flush();
        }
    }

}
