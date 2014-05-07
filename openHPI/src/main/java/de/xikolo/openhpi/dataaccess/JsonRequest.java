package de.xikolo.openhpi.dataaccess;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

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
        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet getRequest = new HttpGet(mUrl);

        try {

            HttpResponse getResponse = client.execute(getRequest);
            final int statusCode = getResponse.getStatusLine().getStatusCode();

            if (statusCode != HttpStatus.SC_OK) {
                Log.w(TAG, "Error " + statusCode + " for URL " + mUrl);
                return null;
            }

            HttpEntity getResponseEntity = getResponse.getEntity();
            InputStream source = getResponseEntity.getContent() ;
            Gson gson = new Gson();
            Reader reader = new InputStreamReader(source);

            return gson.fromJson(reader, mClass);
        }
        catch (IOException e) {
            getRequest.abort();
            Log.w(TAG, "Error for URL " + mUrl, e);
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
