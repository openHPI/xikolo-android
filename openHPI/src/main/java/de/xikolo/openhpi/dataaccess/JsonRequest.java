package de.xikolo.openhpi.dataaccess;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

import de.xikolo.openhpi.util.Config;

public class JsonRequest extends NetworkRequest<Void, Void, Object> {

    public static final String TAG = JsonRequest.class.getSimpleName();

    private Class mClass;
    private String mUrl;
    private OnJsonReceivedListener mCallback;


    public <T> JsonRequest(String url, Class<T> c, OnJsonReceivedListener callback, Context context) {
        super(context);
        this.mClass = c;
        this.mUrl = url;
        this.mCallback = callback;
    }

    @Override
    protected Object doInBackground(Void... args) {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(mUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.addRequestProperty(Config.HEADER_ACCEPT, Config.HEADER_VALUE_ACCEPT_SAP);
            urlConnection.addRequestProperty(Config.HEADER_USER_PLATFORM, Config.HEADER_VALUE_USER_PLATFORM_ANDROID);

            final int statusCode = urlConnection.getResponseCode();

            if (statusCode != HttpURLConnection.HTTP_OK) {
                Log.w(TAG, "Error " + statusCode + " for URL " + mUrl);
                return null;
            }

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            Gson gson = new Gson();
            Reader reader = new InputStreamReader(in);

            return gson.fromJson(reader, mClass);

        } catch (IOException e) {
            Log.w(TAG, "Error for URL " + mUrl, e);
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        mCallback.onJsonReceived(o);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mCallback.onJsonRequestCancelled();
    }

    public interface OnJsonReceivedListener {

        public void onJsonReceived(Object o);

        public void onJsonRequestCancelled();

    }

}
