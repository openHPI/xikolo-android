package de.xikolo.model;

import android.content.Context;

import com.path.android.jobqueue.JobManager;

import java.util.List;

import de.xikolo.entities.Item;
import de.xikolo.jobs.OnJobResponseListener;
import de.xikolo.jobs.RetrieveItemDetailJob;
import de.xikolo.jobs.RetrieveItemsJob;
import de.xikolo.jobs.UpdateProgressionJob;

public class ItemModel extends BaseModel {

    public static final String TAG = ItemModel.class.getSimpleName();

    private OnModelResponseListener<List<Item>> mRetrieveItemsListener;

    private OnModelResponseListener<Item> mRetrieveItemDetailListener;

    public ItemModel(Context context, JobManager jobManager) {
        super(context, jobManager);
    }

    public void retrieveItems(String courseId, String moduleId, boolean cache) {
        OnJobResponseListener<List<Item>> callback = new OnJobResponseListener<List<Item>>() {
            @Override
            public void onResponse(List<Item> response) {
                if (mRetrieveItemsListener != null)
                    mRetrieveItemsListener.onResponse(response);
            }

            @Override
            public void onCancel() {
                if (mRetrieveItemsListener != null)
                    mRetrieveItemsListener.onResponse(null);
            }
        };
        mJobManager.addJobInBackground(new RetrieveItemsJob(callback, courseId, moduleId, cache, UserModel.readAccessToken(mContext)));
    }

    public void setRetrieveItemsListener(OnModelResponseListener<List<Item>> listener) {
        mRetrieveItemsListener = listener;
    }

    public void retrieveItemDetail(String courseId, String moduleId, String itemId, String itemType, boolean cache) {
        OnJobResponseListener<Item> callback = new OnJobResponseListener<Item>() {
            @Override
            public void onResponse(Item response) {
                if (mRetrieveItemDetailListener != null)
                    mRetrieveItemDetailListener.onResponse(response);
            }

            @Override
            public void onCancel() {
                if (mRetrieveItemDetailListener != null)
                    mRetrieveItemDetailListener.onResponse(null);
            }
        };
        mJobManager.addJobInBackground(new RetrieveItemDetailJob(callback, courseId, moduleId, itemId, itemType, cache, UserModel.readAccessToken(mContext)));
    }

    public void setRetrieveItemDetailListener(OnModelResponseListener<Item> listener) {
        mRetrieveItemDetailListener = listener;
    }

    public void updateProgression(String itemId) {
        OnJobResponseListener<Void> callback = new OnJobResponseListener<Void>() {
            @Override
            public void onResponse(Void response) {
            }

            @Override
            public void onCancel() {
            }
        };
        mJobManager.addJobInBackground(new UpdateProgressionJob(callback, itemId, UserModel.readAccessToken(mContext)));
    }

}
