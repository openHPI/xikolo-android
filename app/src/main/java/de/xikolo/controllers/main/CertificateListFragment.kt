package de.xikolo.controllers.main

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import de.xikolo.App
import de.xikolo.R
import de.xikolo.controllers.course.CourseActivityAutoBundle
import de.xikolo.extensions.observe
import de.xikolo.managers.UserManager
import de.xikolo.models.Course
import de.xikolo.viewmodels.main.CertificateListViewModel
import de.xikolo.views.SpaceItemDecoration

class CertificateListFragment : MainFragment<CertificateListViewModel>() {

    companion object {
        val TAG: String = CertificateListFragment::class.java.simpleName
    }

    @BindView(R.id.content_view)
    lateinit var recyclerView: RecyclerView

    private lateinit var certificateListAdapter: CertificateListAdapter

    override val layoutResource = R.layout.content_certificate_list

    override fun createViewModel(): CertificateListViewModel {
        return CertificateListViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        certificateListAdapter = CertificateListAdapter(this, object : CertificateListAdapter.OnCertificateCardClickListener {
            override fun onCourseClicked(courseId: String) {
                val intent = CourseActivityAutoBundle.builder().courseId(courseId).build(App.instance)
                startActivity(intent)
            }
        })

        activity?.let { activity ->
            recyclerView.layoutManager = LinearLayoutManager(App.instance)
            recyclerView.addItemDecoration(SpaceItemDecoration(
                activity.resources.getDimensionPixelSize(R.dimen.card_horizontal_margin),
                activity.resources.getDimensionPixelSize(R.dimen.card_vertical_margin),
                false,
                object : SpaceItemDecoration.RecyclerViewInfo {
                    override fun isHeader(position: Int): Boolean {
                        return false
                    }

                    override val spanCount: Int
                        get() = 1

                    override val itemCount: Int
                        get() = certificateListAdapter.itemCount
                }
            ))
        }
        recyclerView.adapter = certificateListAdapter

        viewModel.courses
            .observe(viewLifecycleOwner) {
                showCertificateList(
                    viewModel.coursesWithCertificates
                )
            }
    }

    private fun showCertificateList(courses: List<Course>) {
        if (!UserManager.isAuthorized) {
            hideContent()
            showLoginRequired()
        } else if (courses.isEmpty()) {
            hideContent()
            showNoCertificatesMessage()
        } else {
            certificateListAdapter.update(courses)
            showContent()
        }
    }

    private fun showNoCertificatesMessage() {
        showMessage(R.string.notification_no_certificates, R.string.notification_no_certificates_summary) {
            activityCallback?.selectDrawerSection(R.id.navigation_all_courses)
        }
    }

    override fun onStart() {
        super.onStart()
        activityCallback?.onFragmentAttached(R.id.navigation_certificates)
    }

}
