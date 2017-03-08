package de.xikolo.controllers.navigation.adapter;

import android.content.Context;
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
import de.xikolo.controllers.helper.ImageHelper;
import de.xikolo.managers.CourseManager;
import de.xikolo.managers.UserManager;
import de.xikolo.models.User;
import de.xikolo.utils.AndroidDimenUtil;
import de.xikolo.utils.Config;
import de.xikolo.utils.FeatureToggle;
import de.xikolo.views.CustomFontTextView;

public class NavigationAdapter extends RecyclerView.Adapter<NavigationAdapter.BaseNavigationViewHolder> {

    public static final String TAG = NavigationAdapter.class.getSimpleName();

    public static final NavigationItem NAV_PROFILE;
    public static final NavigationItem NAV_ALL_COURSES;
    public static final NavigationItem NAV_MY_COURSES;
    public static final NavigationItem NAV_NEWS;
    public static final NavigationItem NAV_DOWNLOADS;
    public static final NavigationItem NAV_SETTINGS;

    public static final NavigationItem NAV_SECOND_SCREEN;

    public static final List<NavigationItem> NAV_ITEMS;

    static {
        NAV_ITEMS = new ArrayList<>();

        NAV_ITEMS.add(NAV_PROFILE = new NavigationItem(
                R.string.icon_profile,
                R.string.title_section_login,
                NavigationItem.ViewType.PROFILE,
                NAV_ITEMS.size()));

        NAV_ITEMS.add(NAV_ALL_COURSES = new NavigationItem(
                R.string.icon_courses,
                R.string.title_section_all_courses,
                NavigationItem.ViewType.MAIN,
                NAV_ITEMS.size()));

        NAV_ITEMS.add(NAV_MY_COURSES = new NavigationItem(
                R.string.icon_course,
                R.string.title_section_my_courses,
                NavigationItem.ViewType.MAIN,
                NAV_ITEMS.size()));

        NAV_ITEMS.add(NAV_NEWS = new NavigationItem(
                R.string.icon_news,
                R.string.title_section_news,
                NavigationItem.ViewType.MAIN,
                NAV_ITEMS.size()));

        if (FeatureToggle.secondScreen()) {
            NAV_ITEMS.add(NAV_SECOND_SCREEN = new NavigationItem(
                    R.string.icon_second_screen,
                    R.string.title_section_second_screen,
                    NavigationItem.ViewType.MAIN,
                    NAV_ITEMS.size()));
        } else {
            NAV_SECOND_SCREEN = new NavigationItem(
                    R.string.icon_second_screen,
                    R.string.title_section_second_screen,
                    NavigationItem.ViewType.MAIN,
                    -99);
        }

        NAV_ITEMS.add(NAV_DOWNLOADS = new NavigationItem(
                R.string.icon_downloads,
                R.string.title_section_downloads,
                NavigationItem.ViewType.SUB,
                NAV_ITEMS.size()));

        NAV_ITEMS.add(NAV_SETTINGS = new NavigationItem(
                R.string.icon_settings,
                R.string.title_section_settings,
                NavigationItem.ViewType.SUB,
                NAV_ITEMS.size()));
    }

    private CourseManager courseManager;

    private int checkedItem = -1;

    private OnItemClickListener itemClickListener;

    public NavigationAdapter() {
        this.courseManager = new CourseManager();
    }

    @Override
    public int getItemCount() {
        return NAV_ITEMS.size();
    }

    @Override
    public int getItemViewType(int position) {
        return NAV_ITEMS.get(position).getViewType().toInteger();
    }

    @Override
    public BaseNavigationViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView;
        if (viewType == NavigationItem.ViewType.PROFILE.toInteger()) {
            itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.item_navi_profile, viewGroup, false);
            return new ProfileNavigationViewHolder(itemView);
        } else if (viewType == NavigationItem.ViewType.MAIN.toInteger()) {
            itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.item_navi_main, viewGroup, false);
            return new CounterNavigationViewHolder(itemView);
        } else {
            itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.item_navi_sub, viewGroup, false);
            return new BaseNavigationViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(BaseNavigationViewHolder viewHolder, int position) {
        NavigationItem navigationItem = NAV_ITEMS.get(position);

        Context context = GlobalApplication.getInstance();

        viewHolder.textIcon.setText(navigationItem.getIcon(context));

        if (position == NAV_PROFILE.getPosition()) {
            ProfileNavigationViewHolder profileViewHolder = (ProfileNavigationViewHolder) viewHolder;

            if (UserManager.isLoggedIn()) {
                profileViewHolder.viewLogin.setVisibility(View.GONE);
                profileViewHolder.viewProfile.setVisibility(View.VISIBLE);

                User user = UserManager.getSavedUser();
                profileViewHolder.textName.setText(String.format(context.getResources().getString(R.string.user_name),
                        user.first_name, user.last_name));
                profileViewHolder.textEmail.setText(user.email);

                if (user.user_visual != null) {
                    ImageHelper.loadRounded(user.user_visual, profileViewHolder.imageProfile);
                } else {
                    ImageHelper.loadRounded(R.drawable.avatar, profileViewHolder.imageProfile);
                }
            } else {
                profileViewHolder.viewLogin.setVisibility(View.VISIBLE);
                profileViewHolder.viewProfile.setVisibility(View.GONE);
                profileViewHolder.textTitle.setText(navigationItem.getTitle(context));
            }
        } else if (position == NAV_MY_COURSES.getPosition()) {
            CounterNavigationViewHolder counterViewHolder = (CounterNavigationViewHolder) viewHolder;

            viewHolder.textTitle.setText(navigationItem.getTitle(context));

            counterViewHolder.textCounter.setText(String.valueOf(0));
            counterViewHolder.textCounter.setVisibility(View.GONE);
        } else {
            viewHolder.textTitle.setText(navigationItem.getTitle(context));
        }

        if (position == NAV_SECOND_SCREEN.getPosition()) {
            viewHolder.textIcon.setCustomFont(context, Config.FONT_MATERIAL);
        } else {
            viewHolder.textIcon.setCustomFont(context, Config.FONT_XIKOLO);
        }

        if (position == getCheckedItemPosition()) {
            if (position != NAV_PROFILE.getPosition() || !UserManager.isLoggedIn()) {
                viewHolder.textIcon.setTextColor(ContextCompat.getColor(context, R.color.apptheme_main));
                viewHolder.textTitle.setTextColor(ContextCompat.getColor(context, R.color.apptheme_main));
            }
        } else {
            if (position != NAV_PROFILE.getPosition() || !UserManager.isLoggedIn()) {
                viewHolder.textIcon.setTextColor(ContextCompat.getColor(context, R.color.navi_text));
                viewHolder.textTitle.setTextColor(ContextCompat.getColor(context, R.color.navi_text));
            }
        }
    }

    public void setItemChecked(int position) {
        checkedItem = position;
    }

    public int getCheckedItemPosition() {
        return checkedItem;
    }

    public void setOnItemClickListener(final OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    class BaseNavigationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CustomFontTextView textIcon;
        TextView textTitle;

        public BaseNavigationViewHolder(View view) {
            super(view);

            textIcon = (CustomFontTextView) view.findViewById(R.id.textIcon);
            textTitle = (TextView) view.findViewById(R.id.textLabel);

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
                    AndroidDimenUtil.getStatusBarHeight() + view.getPaddingTop(),
                    view.getPaddingRight(),
                    view.getPaddingBottom());
        }

    }

    class CounterNavigationViewHolder extends BaseNavigationViewHolder {

        TextView textCounter;

        public CounterNavigationViewHolder(View view) {
            super(view);

            textCounter = (TextView) view.findViewById(R.id.textCounter);
        }

    }

}
