package de.xikolo.controllers.video

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import butterknife.BindView
import de.xikolo.R
import de.xikolo.controllers.base.NetworkStateFragment
import de.xikolo.extensions.observe
import de.xikolo.utils.MarkdownUtil
import de.xikolo.viewmodels.video.VideoViewModel

class VideoDescriptionFragment(var courseId: String, var sectionId: String, var itemId: String, var videoId: String) : NetworkStateFragment<VideoViewModel>() {

    companion object {
        val TAG: String = VideoDescriptionFragment::class.java.simpleName
    }

    @BindView(R.id.textTitle)
    lateinit var videoTitleText: TextView

    @BindView(R.id.textDescription)
    lateinit var videoDescriptionText: TextView

    @BindView(R.id.textSubtitles)
    lateinit var videoSubtitlesText: TextView

    override val layoutResource = R.layout.content_video_description

    override fun createViewModel(): VideoViewModel {
        return VideoViewModel(courseId, sectionId, itemId, videoId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.video
            .observe(this) { video ->
                if (video.summary != null
                    && video.summary.trim { it <= ' ' }.isNotEmpty()
                    && !video.summary.trim { it <= ' ' }.contentEquals("Enter content")) {
                    videoDescriptionText.setTypeface(videoDescriptionText.typeface, Typeface.NORMAL)
                    MarkdownUtil.formatAndSet(video.summary, videoDescriptionText)
                }

                if (video.subtitles != null && video.subtitles.isNotEmpty()) {
                    val text = StringBuilder(getString(R.string.video_settings_subtitles) + ": ")
                    for (subtitles in video.subtitles) {
                        text.append(subtitles.language).append(", ")
                    }
                    text.delete(text.length - 2, text.length)
                    videoSubtitlesText.text = text
                    videoSubtitlesText.visibility = View.VISIBLE
                }

                showContent()
            }

        viewModel.item
            .observe(this) { item ->
                videoTitleText.text = item.title

                showContent()
            }
    }

    override fun onRefresh() {
        // ToDo remove this when Items are refactored to ViewModel
        hideAnyProgress()
    }
}
