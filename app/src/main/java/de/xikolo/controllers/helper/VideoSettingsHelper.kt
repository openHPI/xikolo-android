package de.xikolo.controllers.helper

import android.content.Context
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import de.xikolo.R
import de.xikolo.models.SubtitleTrack
import de.xikolo.utils.PlaybackSpeedUtil
import java.util.*

class VideoSettingsHelper(private val context: Context, private val subtitles: List<SubtitleTrack>?, private val changeListener: OnSettingsChangeListener, private val clickListener: OnSettingsClickListener) {
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    var currentQuality: VideoHelper.VideoMode = VideoHelper.VideoMode.AUTO
    var currentSpeed: PlaybackSpeedUtil = PlaybackSpeedUtil.x10
    var currentSubtitleTrack: SubtitleTrack? = null

    fun buildSettingsView(): ViewGroup {
        val list = buildSettingsPanel(null)

        list.addView(
            buildSettingsItem(
                R.string.icon_quality,
                context.getString(R.string.video_settings_quality) + "  " + context.getString(R.string.video_settings_separator) + "  " + currentQuality.toString(),
                View.OnClickListener { clickListener.onQualityClick() },
                false
            )
        )
        list.addView(
            buildSettingsItem(
                R.string.icon_speed,
                context.getString(R.string.video_settings_speed) + "  " + context.getString(R.string.video_settings_separator) + "  " + currentSpeed.toString(),
                View.OnClickListener { clickListener.onPlaybackSpeedClick() },
                false
            )
        )
        if (subtitles != null && subtitles.isNotEmpty()) {
            list.addView(
                buildSettingsItem(
                    R.string.icon_subtitles,
                    context.getString(R.string.video_settings_subtitles) + if (currentSubtitleTrack != null)
                        "  " + context.getString(R.string.video_settings_separator) + "  " + Locale(currentSubtitleTrack?.language).displayLanguage
                    else
                        "",
                    View.OnClickListener { clickListener.onSubtitleClick() },
                    false
                )
            )
        }

        return list.parent as ViewGroup
    }

    fun buildQualityView(): ViewGroup {
        val list = buildSettingsPanel(context.getString(R.string.video_settings_quality))

        list.addView(
            buildSettingsItem(
                null,
                VideoHelper.VideoMode.AUTO.toString(),
                View.OnClickListener {
                    changeListener.onQualityChange(currentQuality, VideoHelper.VideoMode.AUTO)
                    currentQuality = VideoHelper.VideoMode.AUTO
                },
                currentQuality == VideoHelper.VideoMode.AUTO
            )
        )
        list.addView(
            buildSettingsItem(
                null,
                VideoHelper.VideoMode.HD.toString(),
                View.OnClickListener {
                    changeListener.onQualityChange(currentQuality, VideoHelper.VideoMode.HD)
                    currentQuality = VideoHelper.VideoMode.HD
                },
                currentQuality == VideoHelper.VideoMode.HD
            )
        )
        list.addView(
            buildSettingsItem(
                null,
                VideoHelper.VideoMode.SD.toString(),
                View.OnClickListener {
                    changeListener.onQualityChange(currentQuality, VideoHelper.VideoMode.SD)
                    currentQuality = VideoHelper.VideoMode.SD
                },
                currentQuality == VideoHelper.VideoMode.SD
            )
        )

        return list.parent as ViewGroup
    }

    fun buildPlaybackSpeedView(): ViewGroup {
        val list = buildSettingsPanel(context.getString(R.string.video_settings_speed))

        for (speed in PlaybackSpeedUtil.values()) {
            list.addView(
                buildSettingsItem(
                    null,
                    speed.toString(),
                    View.OnClickListener {
                        changeListener.onPlaybackSpeedChange(currentSpeed, speed)
                        currentSpeed = speed
                    },
                    currentSpeed == speed
                )
            )
        }

        return list.parent as ViewGroup
    }

    fun buildSubtitleView(): ViewGroup {
        val list = buildSettingsPanel(context.getString(R.string.video_settings_subtitles))

        list.addView(
            buildSettingsItem(
                null,
                context.getString(R.string.video_settings_subtitles_none),
                View.OnClickListener {
                    changeListener.onSubtitleChange(currentSubtitleTrack, null)
                    currentSubtitleTrack = null
                },
                currentSubtitleTrack == null
            )
        )
        for (subtitleTrack in subtitles!!) {
            var title = Locale(subtitleTrack.language).displayLanguage
            if (subtitleTrack.createdByMachine) {
                title += " " + context.getString(R.string.video_settings_subtitles_generated)
            }

            list.addView(
                buildSettingsItem(
                    null,
                    title,
                    View.OnClickListener {
                        changeListener.onSubtitleChange(currentSubtitleTrack, subtitleTrack)
                        currentSubtitleTrack = subtitleTrack
                    },
                    currentSubtitleTrack?.id == subtitleTrack.id
                )
            )
        }

        return list.parent as ViewGroup
    }

    private fun buildSettingsPanel(title: String?): ViewGroup {
        val list = inflater
            .inflate(R.layout.content_settings, null)
            .findViewById(R.id.content_settings) as LinearLayout

        val titleView = list.findViewById(R.id.content_settings_title) as TextView
        if (title != null) {
            titleView.text = title
        } else {
            titleView.visibility = View.GONE
        }

        return list
    }

    private fun buildSettingsItem(@StringRes icon: Int?, title: String, clickListener: View.OnClickListener, active: Boolean): ViewGroup {
        val item = inflater.inflate(R.layout.item_settings, null) as LinearLayout

        val iconView = item.findViewById(R.id.item_settings_icon) as TextView
        val titleView = item.findViewById(R.id.item_settings_title) as TextView

        if (icon != null) {
            iconView.setText(icon)
        } else {
            iconView.setText(R.string.icon_settings)
            iconView.visibility = View.INVISIBLE
        }
        titleView.text = title

        if (active) {
            val activeColor = ContextCompat.getColor(context, R.color.apptheme_second)
            iconView.setTextColor(activeColor)
            titleView.setTextColor(activeColor)
        }

        item.setOnClickListener(clickListener)

        return item
    }

    interface OnSettingsClickListener {

        fun onQualityClick()

        fun onPlaybackSpeedClick()

        fun onSubtitleClick()
    }

    // also invoked when old value equal to new value
    interface OnSettingsChangeListener {

        fun onQualityChange(old: VideoHelper.VideoMode, new: VideoHelper.VideoMode)

        fun onPlaybackSpeedChange(old: PlaybackSpeedUtil, new: PlaybackSpeedUtil)

        // subtitle is null if 'None' is selected
        fun onSubtitleChange(old: SubtitleTrack?, new: SubtitleTrack?)
    }
}
