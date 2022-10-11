package com.lifedawn.capstoneapp.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lifedawn.capstoneapp.R
import com.lifedawn.capstoneapp.common.interfaces.OnClickedListItemListener
import com.lifedawn.capstoneapp.databinding.FragmentVotelistBinding
import com.lifedawn.capstoneapp.databinding.VoteItemViewBinding
import com.lifedawn.capstoneapp.databinding.VoteSimpleInfoBinding
import com.lifedawn.capstoneapp.model.VoteInfoDto
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


class VoteListFragment : Fragment() {
    private lateinit var binding: FragmentVotelistBinding
    private lateinit var bundle: Bundle
    private lateinit var voteStatus: VoteMainFragment.VoteStatus
    private val voteListAdapter = VoteListAdapter()

    private val itemOnClickedListener = OnClickedListItemListener<VoteInfoDto> {
        val voteFragment = VoteFragment()
        val bundle = Bundle()
        bundle.putSerializable("voteDto", it)
        voteFragment.arguments = bundle

        val fragmentManager = requireParentFragment().parentFragmentManager
        fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(VoteMainFragment::class.java.name) as Fragment)
                .add(R.id.fragmentContainerView, voteFragment, VoteFragment::class.simpleName).addToBackStack(VoteFragment::class.simpleName)
                .commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = (arguments ?: savedInstanceState) as Bundle
        voteStatus = bundle.getSerializable("voteStatus") as VoteMainFragment.VoteStatus
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentVotelistBinding.inflate(inflater)

        binding.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        //test data
        val testData = VoteInfoDto(0, 0, "장소 투표", "장소 선정 투표바랍니다", ZonedDateTime.now().toString(), 5, true, true)
        voteListAdapter.list.add(testData)

        binding.recyclerView.adapter = voteListAdapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(bundle)
    }

    private inner class VoteListAdapter : RecyclerView.Adapter<VoteListAdapter.ViewHolder>() {
        val list: ArrayList<VoteInfoDto> = ArrayList()
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd E a hh:mm")

        private inner class ViewHolder(val binding: VoteSimpleInfoBinding) : RecyclerView.ViewHolder(binding.root) {

            fun bind() {
                val position = bindingAdapterPosition
                val dto = list[position]

                binding.root.setOnClickListener {
                    itemOnClickedListener.onClicked(dto)
                }

                binding.title.text = dto.title
                val dateTime = ZonedDateTime.parse(dto.dateTime).format(dateTimeFormatter)
                binding.dateTime.text = dateTime

                val people = dto.peopleCount.toString() + getString(R.string.people_participated)
                binding.peopleCount.text = people


                if (dto.completed) {
                    binding.status.text = getString(R.string.votingInComplete)
                    binding.status.setTextColor(context!!.getColor(R.color.vote_completed))
                } else {
                    binding.status.text = getString(R.string.votingInProgress)
                    binding.status.setTextColor(context!!.getColor(R.color.vote_progress))
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(VoteSimpleInfoBinding.inflate(LayoutInflater.from(parent.context)))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind()
        }

        override fun getItemCount(): Int = list.size
    }

}