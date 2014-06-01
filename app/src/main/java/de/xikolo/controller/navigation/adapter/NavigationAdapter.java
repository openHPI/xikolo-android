package de.xikolo.controller.navigation.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.R;
import de.xikolo.manager.AccessTokenManager;
import de.xikolo.manager.UserManager;

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
            LayoutInflater inflater = mContext.getLayoutInflater();
            if (i == NAV_ID_PROFILE) {
                rowView = inflater.inflate(R.layout.item_navi_profile, null);
            } else if (i == NAV_ID_SETTINGS || i == NAV_ID_DOWNLOADS) {
                rowView = inflater.inflate(R.layout.item_navi_sub, null);
            } else {
                rowView = inflater.inflate(R.layout.item_navi_main, null);
            }
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.icon = (TextView) rowView.findViewById(R.id.textIcon);
            viewHolder.label = (TextView) rowView.findViewById(R.id.textLabel);
            rowView.setTag(viewHolder);
        }
        ViewHolder holder = (ViewHolder) rowView.getTag();

        Element element = (Element) getItem(i);
        holder.icon.setText(element.icon);

        if (i == NAV_ID_PROFILE && AccessTokenManager.isLoggedIn(mContext)) {
            holder.label.setText(UserManager.getUser(mContext).name);
        } else {
            holder.label.setText(element.label);
        }

        if (i == ((ListView) viewGroup).getCheckedItemPosition()) {
            holder.icon.setTextColor(mContext.getResources().getColor(R.color.orange));
            holder.label.setTextColor(mContext.getResources().getColor(R.color.orange));
        } else {
            holder.icon.setTextColor(mContext.getResources().getColor(R.color.white));
            holder.label.setTextColor(mContext.getResources().getColor(R.color.white));
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
    }

}
