package de.xikolo.data.net;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;

import javax.net.ssl.HttpsURLConnection;

public abstract class JsonRequest extends HttpRequest {

    public static final String TAG = JsonRequest.class.getSimpleName();

    private Type mType;

    public JsonRequest(String url, Type type, Context context) {
        super(url, context);
        this.mType = type;
    }

    @Override
    protected Object doInBackground(Void... args) {
        try {
            HttpsURLConnection conn = createConnection();
            InputStream in = new BufferedInputStream(conn.getInputStream());

            Gson gson = new Gson();
            Reader reader = new InputStreamReader(in);

            Object o = gson.fromJson(reader, mType);
            closeConnection();

            return o;
        } catch (Exception e) {
            Log.w(TAG, "Error for URL " + mUrl, e);
            onRequestCancelled();
        }
        return null;
    }

}
