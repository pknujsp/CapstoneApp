package com.lifedawn.capstoneapp.promise;

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.lifedawn.capstoneapp.R
import com.lifedawn.capstoneapp.common.view.RecyclerViewItemDecoration
import com.lifedawn.capstoneapp.common.viewmodel.AccountViewModel
import com.lifedawn.capstoneapp.databinding.FragmentPromiseTransactionBinding
import com.lifedawn.capstoneapp.promise.addpromise.AddPromiseFragment
import com.lifedawn.capstoneapp.promise.receivedinvitation.ReceivedInvitationFragment

class PromiseMainFragment : Fragment() {
    private var _binding: FragmentPromiseTransactionBinding? = null
    private val binding get() = _binding!!
    private val accountViewModel by activityViewModels<AccountViewModel>()

    companion object {
        const val TAG = "PromiseMainFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentPromiseTransactionBinding.inflate(inflater, container, false);
        binding.recyclerView.addItemDecoration(RecyclerViewItemDecoration(requireContext()))
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.invitedEventsBtn.setOnClickListener {
            val receivedInvitationFragment = ReceivedInvitationFragment()
            val fragmentManager = requireParentFragment().parentFragmentManager

            fragmentManager.beginTransaction().hide(fragmentManager.primaryNavigationFragment!!)
                    .add(R.id.fragmentContainerView, receivedInvitationFragment, ReceivedInvitationFragment.TAG)
                    .setPrimaryNavigationFragment(receivedInvitationFragment).addToBackStack(ReceivedInvitationFragment.TAG)
                    .commit()
        }

        binding.newPromiseBtn.setOnClickListener {
            val addPromiseFragment = AddPromiseFragment()
            val fragmentManager = requireParentFragment().parentFragmentManager

            fragmentManager.beginTransaction().hide(fragmentManager.primaryNavigationFragment!!)
                    .add(R.id.fragmentContainerView, addPromiseFragment, ReceivedInvitationFragment.TAG)
                    .setPrimaryNavigationFragment(addPromiseFragment).addToBackStack(ReceivedInvitationFragment.TAG)
                    .commit()
        }


    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}