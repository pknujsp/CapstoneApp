package com.lifedawn.capstoneapp.view.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import com.lifedawn.capstoneapp.R
import com.lifedawn.capstoneapp.common.constants.SharedPreferenceConstant
import com.lifedawn.capstoneapp.common.viewmodel.AccountViewModel
import com.lifedawn.capstoneapp.databinding.FragmentSignInBinding
import com.lifedawn.capstoneapp.main.MainTransactionFragment


class SignInFragment : Fragment() {
    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!
    private val accountViewModel by activityViewModels<AccountViewModel>()

    companion object {
        const val TAG = "SignInFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accountViewModel.signInResult.observe(viewLifecycleOwner) {
            if (it == null) {
                Toast.makeText(context, R.string.failed_signin_account, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, R.string.signin_successful, Toast.LENGTH_SHORT).show()

                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
                sharedPreferences.edit().putBoolean(SharedPreferenceConstant.APP_INIT.name, true).apply()

                val mainTransactionFragment = MainTransactionFragment()
                val fragmentTransaction = parentFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.fragmentContainerView, mainTransactionFragment, MainTransactionFragment.TAG)
                        .setPrimaryNavigationFragment(mainTransactionFragment).commitAllowingStateLoss()
            }
        }

        binding.signInBtn.setOnClickListener {
            if (!binding.emailEditText.text.isNullOrEmpty() &&
                    !binding.pwEditText.text.isNullOrEmpty()) {
                accountViewModel.signIn(binding.emailEditText.text.toString(),
                        binding.pwEditText.text.toString())
            }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}