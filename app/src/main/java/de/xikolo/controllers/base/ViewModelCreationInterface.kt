package de.xikolo.controllers.base

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import de.xikolo.viewmodels.base.BaseViewModel

interface ViewModelCreationInterface<T : BaseViewModel> {

    var viewModel: T

    fun createViewModel(): T

    private fun createViewModelFactory(vm: ViewModel): ViewModelProvider.NewInstanceFactory = object : ViewModelProvider.NewInstanceFactory() {
        override fun <S : ViewModel> create(modelClass: Class<S>): S {
            @Suppress("unchecked_cast")
            return vm as S
        }
    }

    fun initViewModel(fragment: Fragment) {
        val vm = createViewModel()
        val factory = createViewModelFactory(vm)
        viewModel = ViewModelProviders.of(fragment, factory).get(vm.javaClass)
    }

    fun initViewModel(fragmentActivity: FragmentActivity) {
        val vm = createViewModel()
        val factory = createViewModelFactory(vm)
        viewModel = ViewModelProviders.of(fragmentActivity, factory).get(vm.javaClass)
    }

}
