package de.xikolo.data.net;

import android.content.Context;

public abstract class HttpConnectionRequest extends HttpRequest {

    public static final String TAG = HttpConnectionRequest.class.getSimpleName();

    public HttpConnectionRequest(String url, Context context) {
        super(url, context);
    }

    @Override
    protected Object doInBackground(Void... args) {
        return createConnection();
    }

}
