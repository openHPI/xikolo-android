package de.xikolo.controllers.video

import android.content.Intent
import android.view.Menu
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.controllers.video.base.BaseVideoPlayerActivity
import de.xikolo.models.VideoStream

class VideoStreamPlayerActivity : BaseVideoPlayerActivity(), VideoStreamPlayerFragment.ControllerInterface {

    companion object {
        val TAG: String = VideoStreamPlayerActivity::class.java.simpleName
    }

    @AutoBundleField
    lateinit var videoStream: VideoStream

    @AutoBundleField(required = false)
    override var parentIntent: Intent? = null

    @AutoBundleField(required = false)
    override var overrideActualParent: Boolean = false

    override val layoutResource = R.layout.activity_video

    override fun createPlayerFragment(): VideoStreamPlayerFragment {
        return VideoStreamPlayerFragmentAutoBundle.builder(videoStream).build()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val returnValue = super.onCreateOptionsMenu(menu)
        enableCastMediaRouterButton(false) // disable cast button because casting is only supported for video items
        return returnValue
    }
}
