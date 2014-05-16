package de.xikolo.openhpi.dataaccess;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;

import de.xikolo.openhpi.util.Config;

public class JsonRequest extends NetworkRequest<Void, Void, Object> {

    public static final String TAG = JsonRequest.class.getSimpleName();

    private String mUrl;
    private Type mType;
    private OnJsonReceivedListener mCallback;


    public JsonRequest(String url, Type type, OnJsonReceivedListener callback, Context context) {
        super(context);
        this.mUrl = url;
        this.mType = type;
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

            return gson.fromJson(reader, mType);

        } catch (IOException e) {
            Log.w(TAG, "Error for URL " + mUrl, e);
        } catch (JsonSyntaxException e) {
            Log.w(TAG, "JSON Syntax Error for URL " + mUrl, e);
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
