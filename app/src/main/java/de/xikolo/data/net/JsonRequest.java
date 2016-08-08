package de.xikolo.data.net;

import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;

import de.xikolo.data.parser.GsonHelper;
import de.xikolo.util.Config;

public class JsonRequest extends HttpRequest {

    public static final String TAG = JsonRequest.class.getSimpleName();

    private Type mType;

    public JsonRequest(String url, Type type) {
        super(url);
        this.mType = type;
    }

    @Override
    public Object getResponse() {
        try {
            HttpsURLConnection conn = createConnection();
            InputStream in = new BufferedInputStream(conn.getInputStream());

            Gson gson = GsonHelper.create();
            Reader reader = new InputStreamReader(in);

            Object o = gson.fromJson(reader, mType);
            closeConnection();

            return o;
        } catch (Exception e) {
            if (Config.DEBUG) Log.w(TAG, "Error for URL " + mUrl, e);
        }
        return null;
    }

}
