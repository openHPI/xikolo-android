package de.xikolo.controllers.base


import android.content.Intent
import android.os.Bundle
import de.xikolo.viewmodels.base.BaseViewModel

abstract class ViewModelActivity<T : BaseViewModel> : BaseActivity(), ViewModelCreationInterface<T> {

    override lateinit var viewModel: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        initViewModel()
    }

    private fun initViewModel(){
        initViewModel(this)
        viewModel.onCreate()
    }

}
