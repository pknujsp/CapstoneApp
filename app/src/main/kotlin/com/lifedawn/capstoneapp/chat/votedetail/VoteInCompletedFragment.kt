package com.lifedawn.capstoneapp.chat.votedetail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lifedawn.capstoneapp.R
import com.lifedawn.capstoneapp.databinding.FragmentVoteInCompletedBinding
import com.lifedawn.capstoneapp.databinding.VoteProgressViewBinding
import com.lifedawn.capstoneapp.databinding.VoteRadioItemBinding
import com.lifedawn.capstoneapp.model.VoteDataDto
import com.lifedawn.capstoneapp.model.VoteInfoDto
import java.time.format.DateTimeFormatter


class VoteInCompletedFragment : Fragment() {
    private lateinit var binding: FragmentVoteInCompletedBinding
    private lateinit var voteDto: VoteInfoDto
    private lateinit var bundle: Bundle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bundle = (arguments ?: savedInstanceState) as Bundle
        voteDto = bundle.getSerializable("voteDto") as VoteInfoDto
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentVoteInCompletedBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (voteDto.voteOnlySingle) {
            createRadio()
        } else {

        }

        binding.btnEndVote.visibility = View.GONE
        binding.btnAddVoteitem.visibility = View.GONE
        binding.btnAgainVote.visibility = View.GONE
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(bundle)
    }

    private fun createRadio() {
        binding.linearLayout.removeAllViews()
        val layoutInflater = layoutInflater
        val arr = arrayListOf<VoteDataDto>(
                VoteDataDto(0, "A", 2), VoteDataDto(1, "B", 2), VoteDataDto(2, "C", 1)
        )

        for (data in arr) {
            val itemBinding = VoteProgressViewBinding.inflate(layoutInflater)
            itemBinding.title.text = data.itemName

            val count: String = data.selectedCount.toString() + getString(R.string.people_participated)
            itemBinding.count.text = count

            itemBinding.progressIndicator.max = voteDto.peopleCount
            itemBinding.progressIndicator.progress = data.selectedCount

            binding.linearLayout.addView(itemBinding.root)
        }
    }
}