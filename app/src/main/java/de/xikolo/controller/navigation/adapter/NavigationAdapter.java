package de.xikolo.controller.navigation.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.R;
import de.xikolo.manager.EnrollmentsManager;
import de.xikolo.manager.TokenManager;
import de.xikolo.manager.UserManager;
import de.xikolo.view.CircularImageView;

public class NavigationAdapter extends BaseAdapter {

    public static final String TAG = NavigationAdapter.class.getSimpleName();
    public static final int NAV_ID_LOW_LEVEL_CONTENT = -1;
    public static final int NAV_ID_PROFILE = 0;
    public static final int NAV_ID_ALL_COURSES = 1;
    public static final int NAV_ID_MY_COURSES = 2;
    public static final int NAV_ID_NEWS = 3;
    public static final int NAV_ID_DOWNLOADS = 4;
    public static final int NAV_ID_SETTINGS = 5;
    private List<Element> elements;
    private Activity mContext;

    public NavigationAdapter(Activity context) {
        this.mContext = context;
        elements = new ArrayList<Element>() {{
            add(new Element(mContext.getString(R.string.icon_profile),
                    mContext.getString(R.string.title_section_login)));
            add(new Element(mContext.getString(R.string.icon_courses),
                    mContext.getString(R.string.title_section_all_courses)));
            add(new Element(mContext.getString(R.string.icon_course),
                    mContext.getString(R.string.title_section_my_courses)));
            add(new Element(mContext.getString(R.string.icon_news),
                    mContext.getString(R.string.title_section_news)));
            add(new Element(mContext.getString(R.string.icon_downloads),
                    mContext.getString(R.string.title_section_downloads)));
            add(new Element(mContext.getString(R.string.icon_settings),
                    mContext.getString(R.string.title_section_settings)));
        }};
    }

    @Override
    public int getCount() {
        return elements.size();
    }

    @Override
    public Object getItem(int i) {
        return elements.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View rowView = view;
        if (rowView == null) {
            ViewHolder viewHolder = new ViewHolder();

            LayoutInflater inflater = mContext.getLayoutInflater();
            if (i == NAV_ID_PROFILE) {
                rowView = inflater.inflate(R.layout.item_navi_profile, null);
                viewHolder.containerLogin = (RelativeLayout) rowView.findViewById(R.id.containerLogin);
                viewHolder.containerProfile = (RelativeLayout) rowView.findViewById(R.id.containerProfile);
                viewHolder.name = (TextView) rowView.findViewById(R.id.textName);
                viewHolder.email = (TextView) rowView.findViewById(R.id.textEmail);
                viewHolder.img = (CircularImageView) rowView.findViewById(R.id.imgProfile);
            } else if (i == NAV_ID_SETTINGS || i == NAV_ID_DOWNLOADS) {
                rowView = inflater.inflate(R.layout.item_navi_sub, null);
            } else {
                rowView = inflater.inflate(R.layout.item_navi_main, null);
                viewHolder.counter = (TextView) rowView.findViewById(R.id.textCounter);
            }

            viewHolder.icon = (TextView) rowView.findViewById(R.id.textIcon);
            viewHolder.label = (TextView) rowView.findViewById(R.id.textLabel);

            rowView.setTag(viewHolder);
        }
        ViewHolder holder = (ViewHolder) rowView.getTag();

        Element element = (Element) getItem(i);
        holder.icon.setText(element.icon);

        if (i == NAV_ID_PROFILE && TokenManager.isLoggedIn(mContext)) {
            holder.containerLogin.setVisibility(View.GONE);
            holder.containerProfile.setVisibility(View.VISIBLE);
            holder.name.setText(UserManager.getUser(mContext).name);
            holder.email.setText(UserManager.getUser(mContext).email);
            if (holder.img.getDrawable() == null) {
                holder.img.setImageDrawable(mContext.getResources().getDrawable(R.drawable.avatar));
            }
        } else if (i == NAV_ID_PROFILE && !TokenManager.isLoggedIn(mContext)) {
            holder.containerLogin.setVisibility(View.VISIBLE);
            holder.containerProfile.setVisibility(View.GONE);
            holder.label.setText(element.label);
        } else {
            holder.label.setText(element.label);
        }

        if (i == ((ListView) viewGroup).getCheckedItemPosition()) {
            if (i == NAV_ID_PROFILE && TokenManager.isLoggedIn(mContext)) {
                holder.name.setTextColor(mContext.getResources().getColor(R.color.orange));
                holder.email.setTextColor(mContext.getResources().getColor(R.color.orange));
            } else {
                holder.icon.setTextColor(mContext.getResources().getColor(R.color.orange));
                holder.label.setTextColor(mContext.getResources().getColor(R.color.orange));
            }
        } else {
            if (i == NAV_ID_PROFILE && TokenManager.isLoggedIn(mContext)) {
                holder.name.setTextColor(mContext.getResources().getColor(R.color.white));
                holder.email.setTextColor(mContext.getResources().getColor(R.color.white));
            } else {
                holder.icon.setTextColor(mContext.getResources().getColor(R.color.white));
                holder.label.setTextColor(mContext.getResources().getColor(R.color.white));
            }
        }

        if (i == NAV_ID_MY_COURSES && TokenManager.isLoggedIn(mContext)) {
            holder.counter.setVisibility(View.VISIBLE);
            int size = EnrollmentsManager.getEnrollmentsSize(mContext);
            if (size > 0) {
                holder.counter.setText(String.valueOf(size));
            }
        }

        return rowView;
    }

    static class Element {
        public String icon;
        public String label;

        public Element(String icon, String text) {
            this.icon = icon;
            this.label = text;
        }
    }

    static class ViewHolder {
        TextView icon;
        TextView label;
        TextView counter;

        CircularImageView img;
        TextView name;
        TextView email;
        RelativeLayout containerLogin;
        RelativeLayout containerProfile;
    }

}
