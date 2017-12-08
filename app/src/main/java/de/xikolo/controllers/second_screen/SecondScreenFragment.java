package de.xikolo.controllers.second_screen;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import de.xikolo.R;
import de.xikolo.config.Config;
import de.xikolo.config.GlideApp;
import de.xikolo.controllers.base.BaseFragment;
import de.xikolo.managers.SecondScreenManager;
import de.xikolo.models.Item;
import de.xikolo.models.Section;
import de.xikolo.models.SubtitleTrack;
import de.xikolo.models.Video;
import de.xikolo.storages.ApplicationPreferences;
import de.xikolo.utils.LanalyticsUtil;
import de.xikolo.utils.NotificationUtil;
import de.xikolo.utils.TimeUtil;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class SecondScreenFragment extends BaseFragment {

    public static final String TAG = SecondScreenFragment.class.getSimpleName();

    @BindView(R.id.text_video_title) TextView textVideoTitle;
    @BindView(R.id.card_video) View cardVideo;
    @BindView(R.id.card_no_video) View cardNoVideo;
    @BindView(R.id.image_video_poster) ImageView imageVideoPoster;
    @BindView(R.id.text_video_time) TextView textVideoTime;
    @BindView(R.id.layout_video_actions) LinearLayout layoutVideoActions;

    private ApplicationPreferences appPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appPreferences = new ApplicationPreferences();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_second_screen, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cardNoVideo.setVisibility(View.VISIBLE);
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSecondScreenNewVideoEvent(SecondScreenManager.SecondScreenNewVideoEvent event) {
        Item item = Item.get(event.itemId);
        Video video = Video.getForContentId(item.contentId);

        if (item != null && cardVideo != null) {
            if (cardVideo.getVisibility() == View.VISIBLE) {
                // for animation
                cardVideo.setVisibility(View.GONE);
            }
            cardNoVideo.setVisibility(View.GONE);
            cardVideo.setVisibility(View.VISIBLE);
            textVideoTitle.setText(item.title);

            long minutes = TimeUnit.SECONDS.toMinutes(video.duration);
            long seconds = video.duration - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(video.duration));
            textVideoTime.setText(getString(R.string.duration, minutes, seconds));

            GlideApp.with(this).load(video.thumbnailUrl).into(imageVideoPoster);

            initSeconScreenActions(item, video, event);

            // clear notification, user is already here
            NotificationUtil notificationUtil = new NotificationUtil(getActivity());
            notificationUtil.cancelSecondScreenNotification();
        }

        if (appPreferences != null) {
            appPreferences.setUsedSecondScreen(true);
        }
    }

    private void initSeconScreenActions(final Item item, final Video video, final SecondScreenManager.SecondScreenNewVideoEvent event) {
        if (layoutVideoActions != null) {
            layoutVideoActions.removeAllViews();

            final View viewSlides = addSlidesAction(event);
            final View viewTranscript = addTranscriptAction(event);
            final View viewQuiz = addQuizAction(event);
            final View viewPinboard = addPinboardAction(event);

            // pdf
            if (video.slidesUrl != null) {
                viewSlides.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = SlideViewerActivityAutoBundle.builder(
                                event.courseId,
                                event.sectionId,
                                event.itemId
                        ).build(getActivity());
                        startActivity(intent);

                        LanalyticsUtil.trackVisitedSecondScreenSlides(event.itemId, event.courseId, event.sectionId);
                    }
                });
                viewSlides.setVisibility(View.VISIBLE);
            }

            // transcript
            if (SubtitleTrack.listForVideoId(video.id).size() > 0) {
                viewTranscript.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = TranscriptViewerActivityAutoBundle.builder(
                                event.courseId,
                                event.sectionId,
                                event.itemId
                        ).build(getActivity());
                        startActivity(intent);

                        LanalyticsUtil.trackVisitedSecondScreenTranscript(event.itemId, event.courseId, event.sectionId);
                    }
                });
                viewTranscript.setVisibility(View.VISIBLE);
            }

            // quiz
            Section section = Section.get(event.sectionId);
            List<Item> sectionItems = section.getAccessibleItems();
            if (sectionItems.size() > 0) {
                final int itemIndex = sectionItems.indexOf(item);

                final Item nextItem;
                if (itemIndex + 1 < sectionItems.size()) {
                    nextItem = sectionItems.get(itemIndex + 1);
                } else {
                    nextItem = null;
                }

                if (nextItem != null && Item.EXERCISE_TYPE_SELFTEST.equals(nextItem.exerciseType)) {
                    viewQuiz.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = QuizActivityAutoBundle.builder(
                                    item.title + " - " + getString(R.string.second_screen_action_title_quiz),
                                    Config.HOST_URL + "go/items/" + nextItem.id,
                                    true,
                                    false,
                                    event.courseId,
                                    event.sectionId,
                                    event.itemId
                            ).build(getActivity());
                            startActivity(intent);

                            LanalyticsUtil.trackVisitedSecondScreenQuiz(event.itemId, event.courseId, event.sectionId);
                        }
                    });
                    viewQuiz.setVisibility(View.VISIBLE);
                }
            }

            // pinboard
            viewPinboard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = PinboardActivityAutoBundle.builder(
                            item.title + " - " + getString(R.string.tab_discussions),
                            Config.HOST_URL + "go/items/" + item.id + "/pinboard",
                            true,
                            false,
                            event.courseId,
                            event.sectionId,
                            event.itemId
                    ).build(getActivity());
                    startActivity(intent);

                    LanalyticsUtil.trackVisitedSecondScreenPinboard(event.itemId, event.courseId, event.sectionId);
                }
            });
            viewPinboard.setVisibility(View.VISIBLE);
        }
    }

    private View addSlidesAction(final SecondScreenManager.SecondScreenNewVideoEvent event) {
        View view = inflateSeconScreenAction(
                R.string.second_screen_action_title_pdf_viewer,
                R.string.second_screen_action_description_pdf_viewer,
                R.string.icon_download_pdf);
        view.setVisibility(View.GONE);
        layoutVideoActions.addView(view);

        return view;
    }

    private View addTranscriptAction(final SecondScreenManager.SecondScreenNewVideoEvent event) {
        View view = inflateSeconScreenAction(
                R.string.second_screen_action_title_transcript,
                R.string.second_screen_action_description_transcript,
                R.string.icon_text);
        view.setVisibility(View.GONE);
        layoutVideoActions.addView(view);

        return view;
    }

    private View addQuizAction(final SecondScreenManager.SecondScreenNewVideoEvent event) {
        View view = inflateSeconScreenAction(
                R.string.second_screen_action_title_quiz,
                R.string.second_screen_action_description_quiz,
                R.string.icon_selftest);
        view.setVisibility(View.GONE);
        layoutVideoActions.addView(view);

        return view;
    }

    private View addPinboardAction(final SecondScreenManager.SecondScreenNewVideoEvent event) {
        View view = inflateSeconScreenAction(
                R.string.second_screen_action_title_pinboard,
                R.string.second_screen_action_description_pinboard,
                R.string.icon_pinboard);
        view.setVisibility(View.GONE);
        layoutVideoActions.addView(view);

        return view;
    }

    private View inflateSeconScreenAction(@StringRes int title, @StringRes int description, @StringRes int icon) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View layout = inflater.inflate(R.layout.item_second_screen, null);

        TextView textTitle = (TextView) layout.findViewById(R.id.text_action_title);
        textTitle.setText(getContext().getString(title));

        TextView textDescription = (TextView) layout.findViewById(R.id.text_action_description);
        textDescription.setText(getContext().getString(description));

        TextView textIcon = (TextView) layout.findViewById(R.id.text_icon_action);
        textIcon.setText(getContext().getString(icon));

        return layout;
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSecondScreenUpdateVideoEvent(SecondScreenManager.SecondScreenUpdateVideoEvent event) {
        Item item = Item.get(event.itemId);
        Video video = Video.getForContentId(item.contentId);

        if (event.webSocketMessage.payload.containsKey("current_time")) {
            long minutes = TimeUnit.SECONDS.toMinutes(video.duration);
            long seconds = video.duration - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(video.duration));

            textVideoTime.setText(
                    TimeUtil.getTimeString(TimeUtil.secondsToMillis(event.webSocketMessage.payload.get("current_time"))) +
                    " / " +
                    getString(R.string.duration, minutes, seconds)            );
        }

        if (cardVideo != null && event.webSocketMessage.action.equals("video_close")) {
            cardVideo.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this);
    }

}
