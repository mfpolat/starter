package com.appfab.basestructure.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class BaseFragmentWithBinding  <T:ViewDataBinding> :BaseFragment(){

    abstract fun getLayoutResId(): Int
    protected lateinit var binding: T
        private set

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return DataBindingUtil.inflate<T>(inflater, getLayoutResId(), container, false)
            .apply {
                binding = this
                binding.lifecycleOwner = viewLifecycleOwner
            }.root
    }
}