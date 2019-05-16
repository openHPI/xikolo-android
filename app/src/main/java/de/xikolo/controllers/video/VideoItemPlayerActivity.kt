package de.xikolo.controllers.video

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import butterknife.BindView
import com.google.android.gms.cast.framework.CastState
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.controllers.video.base.BaseVideoPlayerActivity
import de.xikolo.models.dao.VideoDao
import de.xikolo.utils.CastUtil
import de.xikolo.utils.LanalyticsUtil
import de.xikolo.utils.PlaybackSpeedUtil

class VideoItemPlayerActivity : BaseVideoPlayerActivity() {

    companion object {
        val TAG: String = VideoItemPlayerActivity::class.java.simpleName
    }

    @AutoBundleField
    lateinit var courseId: String

    @AutoBundleField
    lateinit var sectionId: String

    @AutoBundleField
    lateinit var itemId: String

    @AutoBundleField
    lateinit var videoId: String

    @AutoBundleField(required = false)
    override var parentIntent: Intent? = null

    @AutoBundleField(required = false)
    override var overrideActualParent: Boolean = false

    @BindView(R.id.videoSecondaryFragment)
    lateinit var descriptionFragmentContainer: ViewGroup

    lateinit var descriptionFragment: VideoDescriptionFragment

    override val layoutResource = R.layout.activity_video_dual

    override fun createPlayerFragment(): VideoStreamPlayerFragment {
        return VideoItemPlayerFragment(courseId, sectionId, itemId, videoId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        updateDescriptionFragment()
    }

    private fun updateDescriptionFragment() {
        descriptionFragment = VideoDescriptionFragment(courseId, sectionId, itemId, videoId)

        val fragmentTag = descriptionFragment.hashCode().toString()
        val fragmentManager = supportFragmentManager
        if (fragmentManager.findFragmentByTag(fragmentTag) == null) {
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.videoSecondaryFragment, descriptionFragment, fragmentTag)
            transaction.commit()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            updateDescriptionFragment()
        }
    }

    override fun onCastStateChanged(newState: Int) {
        super.onCastStateChanged(newState)

        if (newState != CastState.NO_DEVICES_AVAILABLE) {
            (playerFragment as VideoItemPlayerFragment).updateCastButton()
        }

        if (newState == CastState.CONNECTED) {
            LanalyticsUtil.trackVideoPlay(itemId,
                courseId, sectionId,
                playerFragment.currentPosition,
                PlaybackSpeedUtil.x10.speed,
                Configuration.ORIENTATION_LANDSCAPE,
                "hd",
                "cast"
            )

            playerFragment.pause(false)
            VideoDao.Unmanaged.find(videoId)?.let {
                CastUtil.loadMedia(this, it, true)
            }

            finish()
        }
    }

    override fun updateVideoView(orientation: Int) {
        super.updateVideoView(orientation)

        (playerFragment as VideoItemPlayerFragment).updateCastButton()

        val playerLayoutParams = playerFragmentContainer.layoutParams as RelativeLayout.LayoutParams
        if (isInLandscape(orientation)) {
            descriptionFragmentContainer.visibility = View.GONE
            playerLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
            playerLayoutParams.removeRule(RelativeLayout.ALIGN_PARENT_TOP)
        } else {
            descriptionFragmentContainer.visibility = View.VISIBLE
            playerLayoutParams.removeRule(RelativeLayout.CENTER_IN_PARENT)
            playerLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE)
        }

        if (!playerFragment.isShowingControls) {
            actionBar.hide()
        }
    }

    override fun onControlsHidden() {
        super.onControlsHidden()
        actionBar.hide()
    }

}
