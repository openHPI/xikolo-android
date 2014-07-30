package de.xikolo.data.net;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import de.xikolo.util.Path;
import de.xikolo.util.Network;

public abstract class HttpRequest extends AsyncTask<Void, Void, Object> {

    public static final String TAG = HttpRequest.class.getSimpleName();

    protected Context mContext;

    protected String mUrl;
    protected String mToken;
    protected String mMethod;
    protected boolean mCache;
    protected boolean mCacheOnly;

    protected HttpsURLConnection urlConnection;

    public HttpRequest(String url, Context context) {
        super();
        this.mUrl = url;
        this.mContext = context;
        this.mToken = null;
        this.mMethod = Path.HTTP_GET;
        this.mCache = true;
        this.mCacheOnly = false;
    }

    public void setToken(String token) {
        this.mToken = token;
    }

    public void setMethod(String method) {
        this.mMethod = method;
    }

    public void setCache(boolean cache) {
        this.mCache = cache;
    }

    public void setCacheOnly(boolean cacheOnly) {
        this.mCacheOnly = cacheOnly;
    }

    @Override
    protected void onPreExecute() {
        if (!Network.isOnline(mContext) && !mCacheOnly) {
            Network.showNoConnectionToast(mContext);
            cancel(true);
        }
    }

    @Override
    protected Object doInBackground(Void... args) {
        try {
            InputStreamReader in = new InputStreamReader(new BufferedInputStream(createConnection().getInputStream()));
            BufferedReader buff = new BufferedReader(in);
            StringBuffer input = new StringBuffer();
            String line;
            try {
                while ((line = buff.readLine()) != null)
                    input.append(line);
            } catch (IOException e) {
                Log.e(TAG, "Error reading input stream for ", e);
            }
            closeConnection();
            return input;
        } catch (IOException e) {
            Log.w(TAG, "Error for URL " + mUrl, e);
        }
        return null;
    }

    protected void closeConnection() {
        if (urlConnection != null)
            urlConnection.disconnect();
    }

    protected HttpsURLConnection createConnection() {
        try {
            URL url = new URL(mUrl);
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod(mMethod);
            urlConnection.addRequestProperty(Path.HEADER_ACCEPT, Path.HEADER_VALUE_ACCEPT_SAP);
            urlConnection.addRequestProperty(Path.HEADER_USER_PLATFORM, Path.HEADER_VALUE_USER_PLATFORM_ANDROID);

            if (mCacheOnly) {
                urlConnection.addRequestProperty(Path.HEADER_CACHE_CONTROL, Path.HEADER_VALUE_ONLY_CACHE);
            } else if (!mCache) {
                urlConnection.addRequestProperty(Path.HEADER_CACHE_CONTROL, Path.HEADER_VALUE_NO_CACHE);
            }
            if (mToken != null) {
                urlConnection.addRequestProperty(Path.HEADER_AUTHORIZATION, "Token token=\"" + mToken + "\"");
            }

            final int statusCode = urlConnection.getResponseCode();

            if (statusCode != HttpURLConnection.HTTP_OK) {
                Log.w(TAG, "Error " + statusCode + " for URL " + mUrl);
                return null;
            }

            return urlConnection;
        } catch (IOException e) {
            Log.w(TAG, "Error for URL " + mUrl, e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        onRequestReceived(o);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        onRequestCancelled();
    }

    public abstract void onRequestReceived(Object o);

    public abstract void onRequestCancelled();

}
