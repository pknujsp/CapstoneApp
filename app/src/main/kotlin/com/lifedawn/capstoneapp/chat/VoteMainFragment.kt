package com.lifedawn.capstoneapp.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import com.lifedawn.capstoneapp.R
import com.lifedawn.capstoneapp.databinding.FragmentPlaceChatBinding
import java.io.Serializable

class VoteMainFragment : Fragment() {
    private var _binding: FragmentPlaceChatBinding? = null
    private val binding get() = _binding!!

    enum class VoteStatus : Serializable {
        VOTING_IN_PROGRESS, VOTING_IN_COMPLETE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentPlaceChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.fragmentTitle.setText(R.string.vote_promise_places)
        binding.toolbar.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.updateBtn.setOnClickListener {

        }

        binding.toggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                if (checkedId == R.id.votingInProgressToggle) {
                    onCheckedRadio(VoteStatus.VOTING_IN_PROGRESS, true)
                } else {
                    onCheckedRadio(VoteStatus.VOTING_IN_COMPLETE, true)
                }
            }
        }

        binding.toggleGroup.check(R.id.votingInProgressToggle)
    }

    private fun onCheckedRadio(type: VoteStatus, isChecked: Boolean) {
        if (isChecked) {
            val voteListFragment = VoteListFragment()
            val bundle = Bundle()
            bundle.putSerializable("voteStatus", type)

            voteListFragment.arguments = bundle
            childFragmentManager.beginTransaction().replace(binding.fragmentContainer.id, voteListFragment, VoteFragment::class.simpleName).commit();
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}