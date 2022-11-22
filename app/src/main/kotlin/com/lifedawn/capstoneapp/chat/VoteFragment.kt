package com.lifedawn.capstoneapp.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lifedawn.capstoneapp.R
import com.lifedawn.capstoneapp.chat.votedetail.VoteInCompletedFragment
import com.lifedawn.capstoneapp.chat.votedetail.VotingInProgressFragment
import com.lifedawn.capstoneapp.databinding.FragmentVoteBinding
import com.lifedawn.capstoneapp.model.VoteInfoDto
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class VoteFragment : Fragment() {
    private var _binding: FragmentVoteBinding? = null
    private val binding get() = _binding!!
    private lateinit var voteDto: VoteInfoDto
    private lateinit var bundle: Bundle
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd E a hh:mm")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = (arguments ?: savedInstanceState) as Bundle

        voteDto = bundle.getParcelable("voteDto")!!
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentVoteBinding.inflate(inflater, container, false)

        binding.toolbar.fragmentTitle.text = getString(R.string.vote)
        binding.toolbar.backBtn.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        val fragment: Fragment = if (voteDto.completed) VoteInCompletedFragment() else VotingInProgressFragment()
        fragment.arguments = bundle

        childFragmentManager.beginTransaction().replace(binding.fragmentContainer.id,
                fragment).commit()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.title.text = voteDto.title
        binding.choiceType.text = if (voteDto.voteOnlySingle) getString(R.string.vote_only_single) else getString(R.string.vote_multiple)
        val dateTime = ZonedDateTime.parse(voteDto.dateTime).format(dateTimeFormatter)
        binding.dateTime.text = dateTime
        binding.description.text = voteDto.description
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}