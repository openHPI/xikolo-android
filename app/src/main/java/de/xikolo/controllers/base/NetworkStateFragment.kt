package de.xikolo.controllers.base


import android.os.Bundle
import android.view.*
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.xikolo.R
import de.xikolo.controllers.helper.NetworkStateHelper

abstract class NetworkStateFragment : BaseFragment(), SwipeRefreshLayout.OnRefreshListener, NetworkStateHelper.NetworkStateOwner {

    override lateinit var networkStateHelper: NetworkStateHelper

    abstract val layoutResource: Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflateHelper(inflater, container, layoutResource)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.refresh, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val itemId = item?.itemId
        when (itemId) {
            R.id.action_refresh -> {
                onRefresh()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        networkStateHelper = NetworkStateHelper(activity, view, this)
    }

}
