package de.xikolo.controllers.video

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import butterknife.BindView
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.controllers.base.ViewModelFragment
import de.xikolo.extensions.observe
import de.xikolo.utils.extensions.setMarkdownText
import de.xikolo.viewmodels.section.VideoDescriptionViewModel
import de.xikolo.viewmodels.shared.VideoDescriptionDelegate

class VideoDescriptionFragment : ViewModelFragment<VideoDescriptionViewModel>() {

    companion object {
        val TAG: String = VideoDescriptionFragment::class.java.simpleName
    }

    @AutoBundleField
    lateinit var itemId: String

    @AutoBundleField
    lateinit var videoId: String

    @BindView(R.id.textTitle)
    lateinit var videoTitleText: TextView

    @BindView(R.id.textDescription)
    lateinit var videoDescriptionText: TextView

    @BindView(R.id.textSubtitles)
    lateinit var videoSubtitlesText: TextView

    override val layoutResource = R.layout.fragment_video_description

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
                if (VideoDescriptionDelegate.isVideoSummaryAvailable(video.summary)) {
                    videoDescriptionText.setTypeface(videoDescriptionText.typeface, Typeface.NORMAL)
                    videoDescriptionText.setMarkdownText(video.summary)
                }

                if (video.subtitles != null && video.subtitles.isNotEmpty()) {
                    videoSubtitlesText.text = VideoDescriptionDelegate
                        .getAvailableSubtitlesText(
                            getString(R.string.video_settings_subtitles),
                            video.subtitles
                        )
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
