package de.xikolo.controllers.helper

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import de.xikolo.R
import de.xikolo.config.FeatureConfig
import de.xikolo.managers.PermissionManager
import de.xikolo.models.VideoSubtitles
import de.xikolo.utils.PlaybackSpeedUtil
import java.util.*

class VideoSettingsHelper(private val context: Context, private val subtitles: List<VideoSubtitles>?, private val changeListener: OnSettingsChangeListener, private val clickListener: OnSettingsClickListener, private val qualityOfflineInfo: QualityOfflineInfo) {

    enum class VideoMode(val title: String) {
        SD("SD"), HD("HD"), AUTO("Auto")
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    var currentQuality: VideoMode = VideoMode.HD
    var currentSpeed: PlaybackSpeedUtil = PlaybackSpeedUtil.x10
    var currentVideoSubtitles: VideoSubtitles? = null

    fun buildSettingsView(): ViewGroup {
        val list = buildSettingsPanel(null)

        list.addView(
            buildSettingsItem(
                R.string.icon_quality,
                context.getString(R.string.video_settings_quality) + "  " + context.getString(R.string.video_settings_separator) + "  " + currentQuality.title +
                    if (qualityOfflineInfo.isOfflineAvailable(currentQuality)) " " + context.getString(R.string.video_settings_quality_offline) else "",
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
        if (subtitles?.isNotEmpty() == true) {
            list.addView(
                buildSettingsItem(
                    R.string.icon_subtitles,
                    context.getString(R.string.video_settings_subtitles) + if (currentVideoSubtitles != null)
                        "  " + context.getString(R.string.video_settings_separator) + "  " + Locale(currentVideoSubtitles?.language).displayLanguage
                    else
                        "",
                    View.OnClickListener { clickListener.onSubtitleClick() },
                    false
                )
            )
        }
        if(FeatureConfig.PIP && PermissionManager.hasPipPermission(context)) {
            list.addView(
                buildSettingsItem(
                    R.string.icon_pip,
                    context.getString(R.string.video_settings_pip),
                    View.OnClickListener { clickListener.onPipClick() },
                    false
                )
            )
        }

        return list.parent as ViewGroup
    }

    fun buildQualityView(): ViewGroup {
        val list = buildSettingsPanel(context.getString(R.string.video_settings_quality))

        if(FeatureConfig.HLS_VIDEO) {
            list.addView(
                buildSettingsItem(
                    null,
                    VideoMode.AUTO.title +
                        if (qualityOfflineInfo.isOfflineAvailable(VideoMode.AUTO)) " " + context.getString(R.string.video_settings_quality_offline) else "",
                    View.OnClickListener {
                        val oldQuality = currentQuality
                        currentQuality = VideoMode.AUTO
                        changeListener.onQualityChanged(oldQuality, currentQuality)
                    },
                    currentQuality == VideoMode.AUTO
                )
            )
        }
        list.addView(
            buildSettingsItem(
                null,
                VideoMode.HD.title +
                    if (qualityOfflineInfo.isOfflineAvailable(VideoMode.HD)) " " + context.getString(R.string.video_settings_quality_offline) else "",
                View.OnClickListener {
                    val oldQuality = currentQuality
                    currentQuality = VideoMode.HD
                    changeListener.onQualityChanged(oldQuality, currentQuality)
                },
                currentQuality == VideoMode.HD
            )
        )
        list.addView(
            buildSettingsItem(
                null,
                VideoMode.SD.title +
                    if (qualityOfflineInfo.isOfflineAvailable(VideoMode.SD)) " " + context.getString(R.string.video_settings_quality_offline) else "",
                View.OnClickListener {
                    val oldQuality = currentQuality
                    currentQuality = VideoMode.SD
                    changeListener.onQualityChanged(oldQuality, currentQuality)
                },
                currentQuality == VideoMode.SD
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
                        val oldSpeed = currentSpeed
                        currentSpeed = speed
                        changeListener.onPlaybackSpeedChanged(oldSpeed, currentSpeed)
                    },
                    currentSpeed == speed
                )
            )
        }

        return list.parent as ViewGroup
    }

    fun buildSubtitleView(): ViewGroup {
        val list = buildSettingsPanel(
            context.getString(R.string.video_settings_subtitles),
            context.getString(R.string.icon_settings),
            View.OnClickListener {
                ContextCompat.startActivity(context, Intent(Settings.ACTION_CAPTIONING_SETTINGS), null)
            }
        )

        list.addView(
            buildSettingsItem(
                null,
                context.getString(R.string.video_settings_subtitles_none),
                View.OnClickListener {
                    val oldVideoSubtitles = currentVideoSubtitles
                    currentVideoSubtitles = null
                    changeListener.onSubtitleChanged(oldVideoSubtitles, currentVideoSubtitles)
                },
                currentVideoSubtitles == null
            )
        )
        for (videoSubtitles in subtitles!!) {
            list.addView(
                buildSettingsItem(
                    null,
                    Locale(videoSubtitles.language).displayLanguage +
                        if (videoSubtitles.createdByMachine) " " + context.getString(R.string.video_settings_subtitles_generated) else "",
                    View.OnClickListener {
                        val oldVideoSubtitles = currentVideoSubtitles
                        currentVideoSubtitles = videoSubtitles
                        changeListener.onSubtitleChanged(oldVideoSubtitles, currentVideoSubtitles)
                    },
                    currentVideoSubtitles == videoSubtitles
                )
            )
        }

        return list.parent as ViewGroup
    }

    @SuppressLint("InflateParams")
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

    private fun buildSettingsPanel(title: String?, icon: String, iconClickListener: View.OnClickListener): ViewGroup {
        val panel = buildSettingsPanel(title)
        val iconView = panel.findViewById(R.id.content_settings_icon) as TextView
        iconView.text = icon
        iconView.setOnClickListener(iconClickListener)
        iconView.visibility = View.VISIBLE
        return panel
    }

    @SuppressLint("InflateParams")
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

        fun onPipClick()
    }

    // also invoked when old value equal to new value
    interface OnSettingsChangeListener {

        fun onQualityChanged(old: VideoMode, new: VideoMode)

        fun onPlaybackSpeedChanged(old: PlaybackSpeedUtil, new: PlaybackSpeedUtil)

        // subtitle is null if 'None' is selected
        fun onSubtitleChanged(old: VideoSubtitles?, new: VideoSubtitles?)
    }

    interface QualityOfflineInfo {

        fun isOfflineAvailable(videoMode: VideoMode): Boolean
    }
}
