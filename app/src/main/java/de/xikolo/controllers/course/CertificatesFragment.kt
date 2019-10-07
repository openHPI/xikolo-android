package de.xikolo.controllers.course

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.core.widget.NestedScrollView
import androidx.core.widget.TextViewCompat
import butterknife.BindView
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.controllers.base.ViewModelFragment
import de.xikolo.controllers.helper.DownloadViewHelper
import de.xikolo.extensions.observe
import de.xikolo.models.Course
import de.xikolo.models.DownloadAsset
import de.xikolo.models.Enrollment
import de.xikolo.models.dao.EnrollmentDao
import de.xikolo.viewmodels.course.CertificateListViewModel

class CertificatesFragment : ViewModelFragment<CertificateListViewModel>() {

    companion object {
        val TAG: String = CertificatesFragment::class.java.simpleName
    }

    @AutoBundleField
    lateinit var courseId: String

    @BindView(R.id.content_view)
    internal lateinit var scrollView: NestedScrollView

    @BindView(R.id.container)
    internal lateinit var container: LinearLayout

    override val layoutResource = R.layout.fragment_course_certificates

    override fun createViewModel(): CertificateListViewModel {
        return CertificateListViewModel(courseId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.course
            .observe(viewLifecycleOwner) {
                showCertificates(
                    it,
                    EnrollmentDao.Unmanaged.findForCourse(it.id)
                )
            }
    }

    private fun showCertificates(course: Course, enrollment: Enrollment?) {
        container.removeAllViews()

        activity?.let { activity ->
            val downloadViewHelpers: MutableList<DownloadViewHelper> = ArrayList()

            if (course.certificates.confirmationOfParticipation.available) {
                val dvh = DownloadViewHelper(
                    activity,
                    DownloadAsset.Certificate.ConfirmationOfParticipation(
                        enrollment?.certificates?.confirmationOfParticipationUrl,
                        course
                    ),
                    getString(R.string.course_confirmation_of_participation),
                    String.format(getString(R.string.course_confirmation_of_participation_desc), course.certificates.confirmationOfParticipation.threshold),
                    getString(R.string.course_certificate_not_achieved)
                )

                downloadViewHelpers.add(dvh)
            }

            if (course.certificates.recordOfAchievement.available) {
                val dvh = DownloadViewHelper(
                    activity,
                    DownloadAsset.Certificate.RecordOfAchievement(enrollment?.certificates?.recordOfAchievementUrl, course),
                    getString(R.string.course_record_of_achievement),
                    String.format(getString(R.string.course_record_of_achievement_desc), course.certificates.recordOfAchievement.threshold),
                    getString(R.string.course_certificate_not_achieved)
                )

                downloadViewHelpers.add(dvh)
            }

            if (course.certificates.qualifiedCertificate.available) {
                val dvh = DownloadViewHelper(
                    activity,
                    DownloadAsset.Certificate.QualifiedCertificate(enrollment?.certificates?.qualifiedCertificateUrl, course),
                    getString(R.string.course_qualified_certificate),
                    getString(R.string.course_qualified_certificate_desc),
                    getString(R.string.course_certificate_not_achieved)
                )

                downloadViewHelpers.add(dvh)
            }

            downloadViewHelpers.forEach { dvh ->
                TextViewCompat.setTextAppearance(dvh.textFileName, R.style.TextAppearanceMedium)
                if (!course.isEnrolled) {
                    dvh.buttonDownloadStart.visibility = View.GONE
                }
                container.addView(dvh.view)
            }

            if (downloadViewHelpers.isEmpty()) {
                showEmptyMessage(R.string.empty_message_course_certificates)
            } else {
                showContent()
            }
        }
    }

}
