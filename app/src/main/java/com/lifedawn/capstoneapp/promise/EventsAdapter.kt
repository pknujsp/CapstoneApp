package com.lifedawn.capstoneapp.promise

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.lifedawn.capstoneapp.R
import com.lifedawn.capstoneapp.common.interfaces.OnClickPromiseItemListener
import com.lifedawn.capstoneapp.databinding.ItemViewPromiseBinding
import com.lifedawn.capstoneapp.model.firestore.EventDto

class EventsAdapter(
        private val onClickPromiseItemListener: OnClickPromiseItemListener,
        options: FirestoreRecyclerOptions<EventDto>,
) : FirestoreRecyclerAdapter<EventDto, EventsAdapter.ViewHolder>(options) {


    class ViewHolder(
            private val binding: ItemViewPromiseBinding,
            private val onClickPromiseItemListener: OnClickPromiseItemListener,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: EventDto) {
            binding.editBtn.visibility = if (event.creatorIsMe) View.GONE else View.VISIBLE

            binding.root.setOnClickListener {
                onClickPromiseItemListener.onClickedEvent(event, bindingAdapterPosition)
            }

            binding.editBtn.setOnClickListener {
                val popupMenu = PopupMenu(binding.root.context, binding.editBtn)
                popupMenu.menuInflater.inflate(R.menu.event_menu, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                    if (item.itemId == R.id.action_edit) {
                        onClickPromiseItemListener.onClickedEdit(event, bindingAdapterPosition)
                    } else if (item.itemId == R.id.action_delete) {
                        onClickPromiseItemListener.onClickedRemoveEvent(event, bindingAdapterPosition)
                    }
                    false
                })
                popupMenu.show()

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(ItemViewPromiseBinding.inflate(LayoutInflater.from(parent.context), parent, false), onClickPromiseItemListener)

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: EventDto) {
        holder.bind(model)
    }
}