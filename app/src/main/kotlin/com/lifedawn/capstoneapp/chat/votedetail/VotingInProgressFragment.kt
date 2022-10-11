package com.lifedawn.capstoneapp.chat.votedetail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lifedawn.capstoneapp.R
import com.lifedawn.capstoneapp.databinding.FragmentVoteBinding
import com.lifedawn.capstoneapp.databinding.FragmentVotingInProgressBinding
import com.lifedawn.capstoneapp.model.VoteInfoDto


class VotingInProgressFragment : Fragment() {
    private lateinit var binding: FragmentVotingInProgressBinding
    private lateinit var voteDto: VoteInfoDto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentVotingInProgressBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}