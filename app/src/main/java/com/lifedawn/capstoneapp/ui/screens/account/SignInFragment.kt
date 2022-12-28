package com.lifedawn.capstoneapp.ui.screens.account

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.lifedawn.capstoneapp.R
import com.lifedawn.capstoneapp.common.ui.BaseFragment
import com.lifedawn.capstoneapp.databinding.FragmentSignInBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInFragment : BaseFragment<FragmentSignInBinding>(R.layout.fragment_sign_in) {
    private val accountSignViewModel by activityViewModels<AccountSignViewModel>()

    companion object {
        const val TAG = "SignInFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.signInBtn.setOnClickListener {

        }

        binding.signupBtn.setOnClickListener {
            val signUpFragment = SignUpFragment()

            parentFragmentManager.beginTransaction()
                .hide(parentFragmentManager.primaryNavigationFragment!!)
                .add(R.id.fragmentContainerView, signUpFragment, SignUpFragment.TAG)
                .setPrimaryNavigationFragment(signUpFragment).addToBackStack(SignUpFragment.TAG)
                .commit()
        }
    }
}