package de.xikolo.controllers.course

import android.os.Bundle
import android.support.v4.widget.NestedScrollView
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.LinearLayout
import butterknife.BindView
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.controllers.base.LoadingStatePresenterFragment
import de.xikolo.controllers.helper.DownloadViewHelper
import de.xikolo.models.Course
import de.xikolo.models.DownloadAsset
import de.xikolo.models.Enrollment
import de.xikolo.presenters.base.PresenterFactory
import de.xikolo.presenters.course.CertificatesPresenter
import de.xikolo.presenters.course.CertificatesPresenterFactory
import de.xikolo.presenters.course.CertificatesView

class CertificatesFragment : LoadingStatePresenterFragment<CertificatesPresenter, CertificatesView>(), CertificatesView {

    companion object {
        val TAG = CertificatesFragment::class.java.simpleName!!
    }

    @AutoBundleField
    lateinit var courseId: String

    @BindView(R.id.content_view)
    internal lateinit var scrollView: NestedScrollView

    @BindView(R.id.container)
    internal lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun getLayoutResource(): Int {
        return R.layout.content_certificates
    }

    override fun showCertificates(course: Course, enrollment: Enrollment?) {
        container.removeAllViews()

        var errorMessage: String = getString(R.string.course_certificate_not_achieved)
        if (!course.isEnrolled)
            errorMessage = "" // No button will be shown

        if (activity != null) {
            if (course.certificates.confirmationOfParticipation.available) {
                val confirmationOfParticipationDownloadView = DownloadViewHelper(
                    activity!!,
                    DownloadAsset.Certificate.ConfirmationOfParticipation(enrollment?.certificates?.confirmationOfParticipationUrl, course),
                    getString(R.string.course_confirmation_of_participation),
                    String.format(getString(R.string.course_confirmation_of_participation_desc), course.certificates.confirmationOfParticipation.threshold),
                    errorMessage
                )
                confirmationOfParticipationDownloadView.openFileAsPdf()
                container.addView(confirmationOfParticipationDownloadView.view)
            }

            if (course.certificates.recordOfAchievement.available) {
                val recordOfAchievementDownloadView = DownloadViewHelper(
                    activity!!,
                    DownloadAsset.Certificate.RecordOfAchievement(enrollment?.certificates?.recordOfAchievementUrl, course),
                    getString(R.string.course_record_of_achievement),
                    String.format(getString(R.string.course_record_of_achievement_desc), course.certificates.recordOfAchievement.threshold),
                    errorMessage
                )
                recordOfAchievementDownloadView.openFileAsPdf()
                container.addView(recordOfAchievementDownloadView.view)
            }

            if (course.certificates.qualifiedCertificate.available) {
                val qualifiedCertificateDownloadView = DownloadViewHelper(
                    activity!!,
                    DownloadAsset.Certificate.QualifiedCertificate(enrollment?.certificates?.qualifiedCertificateUrl, course),
                    getString(R.string.course_qualified_certificate),
                    getString(R.string.course_qualified_certificate_desc),
                    errorMessage
                )
                qualifiedCertificateDownloadView.openFileAsPdf()
                container.addView(qualifiedCertificateDownloadView.view)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.refresh, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val itemId = item!!.itemId
        when (itemId) {
            R.id.action_refresh -> {
                onRefresh()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun getPresenterFactory(): PresenterFactory<CertificatesPresenter> {
        return CertificatesPresenterFactory(courseId)
    }

}
