package de.xikolo.controllers.section

import android.os.Bundle
import android.view.View
import de.xikolo.R
import de.xikolo.controllers.base.NetworkStateFragment

class ProctoredItemFragment : NetworkStateFragment() {

    companion object {
        val TAG: String = ProctoredItemFragment::class.java.simpleName
    }

    override val layoutResource = R.layout.fragment_richtext // does not matter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onRefresh() {
        showError()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showError()
    }

    private fun showError() {
        hideAnyProgress()
        showMessage(
            R.string.not_available,
            R.string.item_proctored_summary
        )
    }
}
