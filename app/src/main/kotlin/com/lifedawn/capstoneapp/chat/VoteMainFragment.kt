package com.lifedawn.capstoneapp.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lifedawn.capstoneapp.databinding.FragmentPlaceChatBinding

class VoteMainFragment : Fragment() {
    private lateinit var binding: FragmentPlaceChatBinding;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentPlaceChatBinding.inflate(inflater)
        return binding.root
    }

}