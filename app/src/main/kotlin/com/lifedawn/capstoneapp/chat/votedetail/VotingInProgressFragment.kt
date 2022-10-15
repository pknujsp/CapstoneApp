package com.lifedawn.capstoneapp.chat.votedetail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.core.view.get
import com.lifedawn.capstoneapp.R
import com.lifedawn.capstoneapp.databinding.FragmentVotingInProgressBinding
import com.lifedawn.capstoneapp.databinding.VoteRadioItemBinding
import com.lifedawn.capstoneapp.model.VoteDataDto
import com.lifedawn.capstoneapp.model.VoteInfoDto


class VotingInProgressFragment : Fragment() {
    private lateinit var binding: FragmentVotingInProgressBinding
    private lateinit var voteDto: VoteInfoDto
    private val voteItemArr: ArrayList<VoteDataDto> = ArrayList<VoteDataDto>()
    private val viewTagIdx = 0
    private lateinit var bundle: Bundle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = (arguments ?: savedInstanceState) as Bundle
        voteDto = bundle.getSerializable("voteDto") as VoteInfoDto
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentVotingInProgressBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (voteDto.voteOnlySingle) {
            createRadio()
        } else {

        }
    }

    private fun createRadio() {
        binding.linearLayout.removeAllViews()
        val layoutInflater = layoutInflater

        voteItemArr.clear()
        voteItemArr.addAll(arrayListOf<VoteDataDto>(
                VoteDataDto(0, "A", 2), VoteDataDto(1, "B", 1), VoteDataDto(2, "C", 1)
        ))


        for ((idx, data) in voteItemArr.withIndex()) {
            val itemBinding = VoteRadioItemBinding.inflate(layoutInflater)
            itemBinding.voteProgressView.title.text = data.itemName

            val count: String = data.selectedCount.toString() + getString(R.string.people_participated)
            itemBinding.voteProgressView.count.text = count

            itemBinding.voteProgressView.progressIndicator.max = voteDto.peopleCount
            itemBinding.voteProgressView.progressIndicator.progress = data.selectedCount

            binding.linearLayout.addView(itemBinding.root)
        }
    }
}