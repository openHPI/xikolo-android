package de.xikolo.controllers.dialogs.base

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.ButterKnife
import de.xikolo.controllers.helper.NetworkStateHelper

abstract class NetworkStateDialogFragment : BaseDialogFragment(), SwipeRefreshLayout.OnRefreshListener, NetworkStateHelper.NetworkStateOwner {

    abstract val layoutResource: Int

    override lateinit var networkStateHelper: NetworkStateHelper

    protected lateinit var dialogView: View
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dialogView = createView(LayoutInflater.from(activity), null, savedInstanceState)

        onDialogViewCreated(dialogView, savedInstanceState)
    }

    private fun createView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflateHelper(inflater, container, layoutResource)
    }

    open fun onDialogViewCreated(view: View, savedInstanceState: Bundle?) {
        ButterKnife.bind(this, view)

        networkStateHelper = NetworkStateHelper(activity, view, this)
    }

    abstract override fun onCreateDialog(savedInstanceState: Bundle?): Dialog

}
