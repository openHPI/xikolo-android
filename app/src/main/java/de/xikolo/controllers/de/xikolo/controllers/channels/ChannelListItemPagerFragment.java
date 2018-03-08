package de.xikolo.controllers.de.xikolo.controllers.channels;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import de.xikolo.R;
import de.xikolo.config.GlideApp;
import de.xikolo.models.Course;

public class ChannelListItemPagerFragment extends Fragment{

    private View mView;

    public void setContent(Course course){
        ((TextView) mView.findViewById(R.id.textTitle)).setText(course.title);
        GlideApp.with(this).load(course.imageUrl).into(((ImageView) mView.findViewById(R.id.imageView)));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_channel_list_pager, container, false);
        return mView;
    }
}
