package de.xikolo.controllers.base


import android.os.Bundle
import de.xikolo.viewmodels.base.BaseViewModel

abstract class ViewModelActivity<T : BaseViewModel> : BaseActivity(), ViewModelCreationInterface<T> {

    override lateinit var viewModel: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel(this)
    }
}
