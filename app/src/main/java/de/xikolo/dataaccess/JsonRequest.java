package de.xikolo.dataaccess;

import android.content.Context;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;

public abstract class JsonRequest extends HttpRequest {

    public static final String TAG = JsonRequest.class.getSimpleName();

    private Type mType;

    public JsonRequest(String url, Type type, Context context) {
        super(url, context);
        this.mType = type;
    }

    @Override
    protected Object doInBackground(Void... args) {
        InputStream in = createConnection();

        Gson gson = new Gson();
        Reader reader = new InputStreamReader(in);

        Object o = gson.fromJson(reader, mType);
        closeConnection();

        return o;
    }

}
