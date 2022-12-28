package com.lifedawn.capstoneapp.ui.screens.account

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.lifedawn.capstoneapp.R
import com.lifedawn.capstoneapp.common.ui.BaseFragment
import com.lifedawn.capstoneapp.databinding.FragmentSignUpBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpFragment : BaseFragment<FragmentSignUpBinding>(R.layout.fragment_sign_up) {
    private val accountViewModel by activityViewModels<AccountSignViewModel>()

    companion object {
        const val TAG = "SignUpFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}