package com.lifedawn.capstoneapp.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.lifedawn.capstoneapp.R
import com.lifedawn.capstoneapp.calendar.fragments.CalendarTransactionFragment
import com.lifedawn.capstoneapp.common.viewmodel.AccountViewModel
import com.lifedawn.capstoneapp.databinding.FragmentMainTransactionBinding
import com.lifedawn.capstoneapp.friends.FriendTransactionFragment
import com.lifedawn.capstoneapp.promise.PromiseMainFragment

class MainTransactionFragment : Fragment() {
    private var _binding: FragmentMainTransactionBinding? = null
    private val binding get() = _binding!!
    private val accountViewModel by activityViewModels<AccountViewModel>()

    companion object {
        const val TAG = "MainTransactionFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentMainTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.bottomNavigationView.setOnItemReselectedListener { _ ->
        }

        binding.bottomNavigationView.setOnItemSelectedListener {
            var primaryFragment = childFragmentManager.primaryNavigationFragment
            var newFragment: Fragment? = null
            var tag = ""

            when (it.itemId) {
                R.id.mainPage -> {
                    if (primaryFragment is PromiseMainFragment)
                        false
                    newFragment = PromiseMainFragment()
                    tag = PromiseMainFragment.TAG
                }
                R.id.friendPage -> {
                    if (primaryFragment is FriendTransactionFragment)
                        false
                    newFragment = FriendTransactionFragment()
                    tag = FriendTransactionFragment.TAG
                }
                else -> {
                    if (primaryFragment is CalendarTransactionFragment)
                        false
                    newFragment = CalendarTransactionFragment()
                    tag = CalendarTransactionFragment.TAG
                }
            }

            childFragmentManager.apply {
                beginTransaction().apply {
                    hide(primaryFragment!!)

                    if (findFragmentByTag(tag) == null) {
                        add(binding.fragmentContainerView.id, newFragment, tag)
                    } else {
                        show(newFragment)
                    }
                    setPrimaryNavigationFragment(newFragment).commit()
                }

            }
            true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}