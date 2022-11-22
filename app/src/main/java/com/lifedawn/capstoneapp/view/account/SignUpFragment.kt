package com.lifedawn.capstoneapp.view.account

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.lifedawn.capstoneapp.R
import com.lifedawn.capstoneapp.common.viewmodel.AccountViewModel
import com.lifedawn.capstoneapp.databinding.FragmentSignUpBinding


class SignUpFragment : Fragment() {
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private val accountViewModel by activityViewModels<AccountViewModel>()

    companion object {
        const val TAG = "SignUpFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        binding.toolbar.fragmentTitle.text = getString(R.string.signup)
        binding.toolbar.backBtn.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accountViewModel.signUpResult.observe(viewLifecycleOwner) {
            if (it == null) {
                Toast.makeText(context, R.string.failed_signup, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, R.string.signup_successful, Toast.LENGTH_SHORT).show()
            }
        }

        binding.signupBtn.setOnClickListener {
            //이메일, 비밀번호, 이름 확인
            if (!binding.emailEditText.text.isNullOrEmpty() &&
                    !binding.pwEditText.text.isNullOrEmpty() &&
                    !binding.checkPwEditText.text.isNullOrEmpty() &&
                    !binding.nameEditText.text.isNullOrEmpty()
            ) {
                //비밀번호 두 개 일치 여부 확인
                if (binding.pwEditText.text.toString() == binding.checkPwEditText.text.toString()) {
                    //회원가입 가능

                    val map = mapOf<String, String>(
                            "email" to binding.emailEditText.text.toString(),
                            "pw" to binding.pwEditText.text.toString(),
                            "name" to binding.nameEditText.text.toString(),
                    )
                    accountViewModel.signUp(map)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}