package de.xikolo.controller.navigation.adapter;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.controller.helper.ImageController;
import de.xikolo.data.entities.User;
import de.xikolo.model.CourseModel;
import de.xikolo.model.UserModel;

public class NavigationAdapter extends RecyclerView.Adapter<NavigationAdapter.BaseNavigationViewHolder> {

    public static final String TAG = NavigationAdapter.class.getSimpleName();

    public static final int NAV_ID_LOW_LEVEL_CONTENT = -1;
    public static final int NAV_ID_PROFILE = 0;
    public static final int NAV_ID_ALL_COURSES = 1;
    public static final int NAV_ID_MY_COURSES = 2;
    public static final int NAV_ID_NEWS = 3;
    public static final int NAV_ID_DOWNLOADS = 4;
    public static final int NAV_ID_SETTINGS = 5;

    private List<Element> elements;

    private CourseModel courseModel;

    private int checkedItem = -1;

    OnItemClickListener itemClickListener;

    public NavigationAdapter(CourseModel courseModel) {
        final Context context = GlobalApplication.getInstance();

        this.courseModel = courseModel;
        elements = new ArrayList<Element>() {{
            add(new Element(context.getString(R.string.icon_profile),
                    context.getString(R.string.title_section_login)));
            add(new Element(context.getString(R.string.icon_courses),
                    context.getString(R.string.title_section_all_courses)));
            add(new Element(context.getString(R.string.icon_course),
                    context.getString(R.string.title_section_my_courses)));
            add(new Element(context.getString(R.string.icon_news),
                    context.getString(R.string.title_section_news)));
            add(new Element(context.getString(R.string.icon_downloads),
                    context.getString(R.string.title_section_downloads)));
            add(new Element(context.getString(R.string.icon_settings),
                    context.getString(R.string.title_section_settings)));
        }};
    }

    @Override
    public int getItemCount() {
        return elements.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public BaseNavigationViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView;
        if (viewType == NAV_ID_PROFILE) {
            itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.item_navi_profile, viewGroup, false);
            return new ProfileNavigationViewHolder(itemView);
        } else if (viewType == NAV_ID_SETTINGS || viewType == NAV_ID_DOWNLOADS) {
            itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.item_navi_sub, viewGroup, false);
            return new BaseNavigationViewHolder(itemView);
        } else {
            itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.item_navi_main, viewGroup, false);
            return new CounterNavigationViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(BaseNavigationViewHolder viewHolder, int position) {
        Element element = elements.get(position);

        Context context = GlobalApplication.getInstance();

        viewHolder.textIcon.setText(element.icon);

        if (position == NAV_ID_PROFILE) {
            ProfileNavigationViewHolder profileViewHolder = (ProfileNavigationViewHolder) viewHolder;

            if (UserModel.isLoggedIn(context)) {
                profileViewHolder.viewLogin.setVisibility(View.GONE);
                profileViewHolder.viewProfile.setVisibility(View.VISIBLE);

                User user = UserModel.getSavedUser(context);
                profileViewHolder.textName.setText(String.format(context.getResources().getString(R.string.user_name),
                        user.first_name, user.last_name));
                profileViewHolder.textEmail.setText(user.email);

                if (user.user_visual != null) {
                    ImageController.loadRounded(user.user_visual, profileViewHolder.imageProfile);
                } else {
                    ImageController.loadRounded(R.drawable.avatar, profileViewHolder.imageProfile);
                }
            } else {
                profileViewHolder.viewLogin.setVisibility(View.VISIBLE);
                profileViewHolder.viewProfile.setVisibility(View.GONE);
                profileViewHolder.textLabel.setText(element.label);
            }
        } else if (position == NAV_ID_MY_COURSES) {
            CounterNavigationViewHolder counterViewHolder = (CounterNavigationViewHolder) viewHolder;

            viewHolder.textLabel.setText(element.label);

            if (UserModel.isLoggedIn(context)) {
                int size = courseModel.getEnrollmentsCount();
                counterViewHolder.textEnrollmentCounter.setText(String.valueOf(size));
                if (size > 0) {
                    counterViewHolder.textEnrollmentCounter.setVisibility(View.VISIBLE);
                } else {
                    counterViewHolder.textEnrollmentCounter.setVisibility(View.GONE);
                }
            } else {
                counterViewHolder.textEnrollmentCounter.setText(String.valueOf(0));
                counterViewHolder.textEnrollmentCounter.setVisibility(View.GONE);
            }
        } else {
            viewHolder.textLabel.setText(element.label);
        }

        if (position == getCheckedItemPosition()) {
            if (position != NAV_ID_PROFILE || !UserModel.isLoggedIn(context)) {
                viewHolder.textIcon.setTextColor(ContextCompat.getColor(context, R.color.apptheme_main));
                viewHolder.textLabel.setTextColor(ContextCompat.getColor(context, R.color.apptheme_main));
            }
        } else {
            if (position != NAV_ID_PROFILE || !UserModel.isLoggedIn(context)) {
                viewHolder.textIcon.setTextColor(ContextCompat.getColor(context, R.color.white));
                viewHolder.textLabel.setTextColor(ContextCompat.getColor(context, R.color.white));
            }
        }
    }

    public void setItemChecked(int position) {
        checkedItem = position;
    }

    public int getCheckedItemPosition() {
        return checkedItem;
    }

    static class Element {

        public String icon;
        public String label;

        public Element(String icon, String text) {
            this.icon = icon;
            this.label = text;
        }

    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.itemClickListener = mItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    class BaseNavigationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView textIcon;
        TextView textLabel;

        public BaseNavigationViewHolder(View view) {
            super(view);

            textIcon = (TextView) view.findViewById(R.id.textIcon);
            textLabel = (TextView) view.findViewById(R.id.textLabel);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(view, getAdapterPosition());
            }
        }

    }

    class ProfileNavigationViewHolder extends BaseNavigationViewHolder {

        ImageView imageProfile;
        TextView textName;
        TextView textEmail;
        View viewLogin;
        View viewProfile;

        public ProfileNavigationViewHolder(View view) {
            super(view);

            viewLogin = view.findViewById(R.id.containerLogin);
            viewProfile = view.findViewById(R.id.containerProfile);
            textName = (TextView) view.findViewById(R.id.textName);
            textEmail = (TextView) view.findViewById(R.id.textEmail);
            imageProfile = (ImageView) view.findViewById(R.id.imgProfile);

            setStatusBarPadding(viewLogin);
            setStatusBarPadding(viewProfile);
        }

        private void setStatusBarPadding(View view) {
            view.setPadding(view.getPaddingLeft(),
                    getStatusBarHeight() + view.getPaddingTop(),
                    view.getPaddingRight(),
                    view.getPaddingBottom());
        }

        private int getStatusBarHeight() {
            int result = 0;

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                return result;
            }

            int resourceId = GlobalApplication.getInstance().getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = GlobalApplication.getInstance().getResources().getDimensionPixelSize(resourceId);
            }
            return result;
        }

    }

    class CounterNavigationViewHolder extends BaseNavigationViewHolder {

        TextView textEnrollmentCounter;

        public CounterNavigationViewHolder(View view) {
            super(view);

            textEnrollmentCounter = (TextView) view.findViewById(R.id.textCounter);
        }

    }

}
