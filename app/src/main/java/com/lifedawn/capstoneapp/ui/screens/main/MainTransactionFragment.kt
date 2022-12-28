package com.lifedawn.capstoneapp.ui.screens.main

import android.os.Bundle
import android.view.View
import com.lifedawn.capstoneapp.R
import com.lifedawn.capstoneapp.common.ui.BaseFragment
import com.lifedawn.capstoneapp.databinding.FragmentMainTransactionBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainTransactionFragment : BaseFragment<FragmentMainTransactionBinding>(R.layout.fragment_main_transaction) {
    companion object {
        const val TAG = "MainTransactionFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}