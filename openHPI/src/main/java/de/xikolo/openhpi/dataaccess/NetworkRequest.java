package de.xikolo.openhpi.dataaccess;

import android.content.Context;
import android.os.AsyncTask;

import de.xikolo.openhpi.util.Network;

public abstract class NetworkRequest<U, S, T> extends AsyncTask<U, S, T> {

    protected Context mContext;

    public NetworkRequest(Context context) {
        super();
        this.mContext = context;
    }

    @Override
    protected void onPreExecute() {
        if (!Network.isOnline(mContext)) {
            Network.showNoConnectionToast(mContext);
            cancel(true);
        }
    }

}
