package de.xikolo.controller.navigation.adapter;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.R;
import de.xikolo.data.entities.User;
import de.xikolo.model.CourseModel;
import de.xikolo.model.UserModel;
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
    private Activity mActivity;

    private CourseModel courseModel;

    public NavigationAdapter(Activity activity, CourseModel courseModel) {
        this.mActivity = activity;
        this.courseModel = courseModel;
        elements = new ArrayList<Element>() {{
            add(new Element(mActivity.getString(R.string.icon_profile),
                    mActivity.getString(R.string.title_section_login)));
            add(new Element(mActivity.getString(R.string.icon_courses),
                    mActivity.getString(R.string.title_section_all_courses)));
            add(new Element(mActivity.getString(R.string.icon_course),
                    mActivity.getString(R.string.title_section_my_courses)));
            add(new Element(mActivity.getString(R.string.icon_news),
                    mActivity.getString(R.string.title_section_news)));
            add(new Element(mActivity.getString(R.string.icon_downloads),
                    mActivity.getString(R.string.title_section_downloads)));
            add(new Element(mActivity.getString(R.string.icon_settings),
                    mActivity.getString(R.string.title_section_settings)));
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

            LayoutInflater inflater = mActivity.getLayoutInflater();
            if (i == NAV_ID_PROFILE) {
                rowView = inflater.inflate(R.layout.item_navi_profile, null);

                viewHolder.containerLogin = rowView.findViewById(R.id.containerLogin);
                viewHolder.containerProfile = rowView.findViewById(R.id.containerProfile);
                viewHolder.name = (TextView) rowView.findViewById(R.id.textName);
                viewHolder.email = (TextView) rowView.findViewById(R.id.textEmail);
                viewHolder.img = (CircularImageView) rowView.findViewById(R.id.imgProfile);

                setStatusBarPadding(viewHolder.containerLogin);
                setStatusBarPadding(viewHolder.containerProfile);

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

        if (i == NAV_ID_PROFILE && UserModel.isLoggedIn(mActivity)) {
            holder.containerLogin.setVisibility(View.GONE);
            holder.containerProfile.setVisibility(View.VISIBLE);

            User user = UserModel.getSavedUser(mActivity);
            holder.name.setText(user.first_name + " " + user.last_name);
            holder.email.setText(user.email);

            if (user.user_visual != null) {
                Drawable lastImage;
                if (holder.img.getDrawable() != null) {
                    lastImage = holder.img.getDrawable();
                } else {
                    lastImage = mActivity.getResources().getDrawable(R.drawable.avatar);
                }
                DisplayImageOptions options = new DisplayImageOptions.Builder()
                        .showImageOnLoading(lastImage)
                        .showImageForEmptyUri(R.drawable.avatar)
                        .showImageOnFail(R.drawable.avatar)
                        .build();
                ImageLoader.getInstance().displayImage(user.user_visual, holder.img, options);
            } else {
                ImageLoader.getInstance().displayImage("drawable://" + R.drawable.avatar, holder.img);
            }

        } else if (i == NAV_ID_PROFILE && !UserModel.isLoggedIn(mActivity)) {
            holder.containerLogin.setVisibility(View.VISIBLE);
            holder.containerProfile.setVisibility(View.GONE);
            holder.label.setText(element.label);
        } else {
            holder.label.setText(element.label);
        }

        if (i == ((ListView) viewGroup).getCheckedItemPosition()) {
            if (i == NAV_ID_PROFILE && UserModel.isLoggedIn(mActivity)) {
                holder.img.setBorderColor(mActivity.getResources().getColor(R.color.apptheme_main));
            } else {
                holder.icon.setTextColor(mActivity.getResources().getColor(R.color.apptheme_main));
                holder.label.setTextColor(mActivity.getResources().getColor(R.color.apptheme_main));
            }
        } else {
            if (i == NAV_ID_PROFILE && UserModel.isLoggedIn(mActivity)) {
                holder.img.setBorderColor(mActivity.getResources().getColor(R.color.white));
            } else {
                holder.icon.setTextColor(mActivity.getResources().getColor(R.color.white));
                holder.label.setTextColor(mActivity.getResources().getColor(R.color.white));
            }
        }

        if (i == NAV_ID_MY_COURSES && UserModel.isLoggedIn(mActivity)) {
            int size = courseModel.getEnrollmentsCount();
            holder.counter.setText(String.valueOf(size));
            if (size > 0) {
                holder.counter.setVisibility(View.VISIBLE);
            } else {
                holder.counter.setVisibility(View.GONE);
            }
        } else if (i == NAV_ID_MY_COURSES && !UserModel.isLoggedIn(mActivity)) {
            holder.counter.setText(String.valueOf(0));
            holder.counter.setVisibility(View.GONE);
        }

        return rowView;
    }

    private void setStatusBarPadding(View view) {
       view.setPadding(view.getPaddingLeft(),
                getStatusBarHeight() + view.getPaddingTop(),
                view.getPaddingRight(),
                view.getPaddingBottom());
    }

    private int getStatusBarHeight() {
        int result = 0;

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return result;
        }

        int resourceId = mActivity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = mActivity.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
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
        View containerLogin;
        View containerProfile;
    }

}
