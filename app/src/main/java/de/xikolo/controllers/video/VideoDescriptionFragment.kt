package de.xikolo.controllers.video

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import butterknife.BindView
import de.xikolo.R
import de.xikolo.controllers.base.ViewModelFragment
import de.xikolo.extensions.observe
import de.xikolo.utils.MarkdownUtil
import de.xikolo.viewmodels.section.VideoDescriptionViewModel

class VideoDescriptionFragment(val itemId: String, val videoId: String) : ViewModelFragment<VideoDescriptionViewModel>() {

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

    override fun createViewModel(): VideoDescriptionViewModel {
        return VideoDescriptionViewModel(itemId, videoId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.video
            .observe(viewLifecycleOwner) { video ->
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
            .observe(viewLifecycleOwner) { item ->
                videoTitleText.text = item.title

                showContent()
            }
    }

}
