package com.lifedawn.capstoneapp.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.lifedawn.capstoneapp.R
import com.lifedawn.capstoneapp.databinding.FragmentVotelistBinding


class VoteListFragment : Fragment() {
    private lateinit var binding: FragmentVotelistBinding
    private lateinit var bundle: Bundle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bundle = (arguments ?: savedInstanceState) as Bundle
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentVotelistBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.warningLayout.root.visibility = View.VISIBLE
        binding.warningLayout.warningText.text = getString(R.string.empty_fixed_promises)
        binding.warningLayout.btn.visibility = View.GONE

    }

}