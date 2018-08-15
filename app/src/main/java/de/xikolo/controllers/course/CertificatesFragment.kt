package de.xikolo.controllers.course

import android.os.Bundle
import android.support.v4.widget.NestedScrollView
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import butterknife.BindView
import com.yatatsu.autobundle.AutoBundleField
import de.xikolo.R
import de.xikolo.controllers.base.LoadingStatePresenterFragment
import de.xikolo.controllers.helper.DownloadViewHelper
import de.xikolo.models.Course
import de.xikolo.models.DownloadAsset
import de.xikolo.presenters.base.PresenterFactory
import de.xikolo.presenters.course.CertificatesPresenter
import de.xikolo.presenters.course.CertificatesPresenterFactory
import de.xikolo.presenters.course.CertificatesView

class CertificatesFragment : LoadingStatePresenterFragment<CertificatesPresenter, CertificatesView>(), CertificatesView {

    companion object {
        val TAG = CertificatesFragment::class.java.simpleName
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun showCertificates(course: Course) {
        val isEnrolled = course.isEnrolled

        container.removeAllViews()

        if (activity != null) {
            if (course.certificates.confirmationOfParticipation.available) {
                val confirmationOfParticipationView = DownloadViewHelper(
                    activity!!,
                    DownloadAsset.Certificate.ConfirmationOfParticipation(course.certificates.confirmationOfParticipation.url, course),
                    getString(R.string.course_confirmation_of_participation)
                )
                container.addView(confirmationOfParticipationView.view)
            }

            if (course.certificates.recordOfAchievement.available) {
                val recordOfAchievementView = DownloadViewHelper(
                    activity!!,
                    DownloadAsset.Certificate.RecordOfAchievement(course.certificates.recordOfAchievement.url, course),
                    getString(R.string.course_record_of_achievement)
                )
                container.addView(recordOfAchievementView.view)
            }

            if (course.certificates.qualifiedCertificate.available) {
                val qualifiedCertificateView = DownloadViewHelper(
                    activity!!,
                    DownloadAsset.Certificate.QualifiedCertificate(course.certificates.qualifiedCertificate.url, course),
                    getString(R.string.course_qualified_certificate)
                )
                container.addView(qualifiedCertificateView.view)
            }
        }

        /*for(DownloadAsset.Course.Certificate certificate : certificates){
            if(certificate instanceof DownloadAsset.Course.Certificate.ConfirmationOfParticipation){
                setupItem(getString(R.string.course_confirmation_of_participation),
                    getString(R.string.course_confirmation_of_participation_desc, ((DownloadAsset.Course.Certificate.ConfirmationOfParticipation) certificate).getThreshold()),
                    isEnrolled,
                    certificate
                    );
            }
            else if(certificate instanceof DownloadAsset.Course.Certificate.RecordOfAchievement){
                setupItem(getString(R.string.course_record_of_achievement),
                    getString(R.string.course_record_of_achievement_desc, ((DownloadAsset.Course.Certificate.RecordOfAchievement) certificate).getThreshold()),
                    isEnrolled,
                    certificate
                );
            }
            else if(certificate instanceof DownloadAsset.Course.Certificate.QualifiedCertificate){
                setupItem(getString(R.string.course_qualified_certificate),
                    getString(R.string.course_qualified_certificate_desc),
                    isEnrolled,
                    certificate
                );
            }
        }*/
    }

    /*private fun setupItem(header: String, text: String, enrolled: Boolean, certificate: DownloadAsset.Certificate) {
        val h = layoutInflater.inflate(R.layout.item_section_header, null)
        (h.findViewById<View>(R.id.textHeader) as TextView).text = header

        val v = layoutInflater.inflate(R.layout.item_certificate, null)
        (v.findViewById<View>(R.id.textContent) as TextView).text = Html.fromHtml(text)
        val downloadButton = v.findViewById<Button>(R.id.button_certificate_download)
        if (certificate.url != null) {
            downloadButton.setText(R.string.course_certificate_view)
            downloadButton.setOnClickListener { view -> IntentUtil.openDoc(App.getInstance(), certificate.url!!) }
        } else {
            if (enrolled) {
                downloadButton.isEnabled = false
                downloadButton.setText(R.string.course_certificate_not_available)
            } else {
                downloadButton.visibility = View.GONE
            }
        }

        container!!.addView(h)
        container!!.addView(v)
    }*/

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
