package com.amjad.opennote.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amjad.opennote.data.entities.CheckableListNote
import com.amjad.opennote.databinding.CheckableNoteItemBinding

class CheckableNoteListAdapter :
    ListAdapter<CheckableListNote.Item, CheckableNoteListAdapter.CheckableListNoteItemViewHolder>(
        CheckableNoteListDiffItemCallBack()
    ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CheckableListNoteItemViewHolder {
        return CheckableListNoteItemViewHolder(
            CheckableNoteItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    // not sure where to put this thing
    // FIXME: when loading the recyclerview the whole list is being rendered.
    //  This is due to the way the scrolling is happening right now which is not very nice
    override fun onBindViewHolder(holder: CheckableListNoteItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CheckableListNoteItemViewHolder(private val binding: CheckableNoteItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CheckableListNote.Item) {
            binding.item = item
            binding.setOnDelete {
                TODO(
                    "implement a way to delete the note, as ListAdapter store the items in" +
                            "readonly list"
                )
            }
        }
    }
}


private class CheckableNoteListDiffItemCallBack : DiffUtil.ItemCallback<CheckableListNote.Item>() {
    override fun areContentsTheSame(
        oldItem: CheckableListNote.Item,
        newItem: CheckableListNote.Item
    ): Boolean =
        newItem.text == oldItem.text && newItem.isChecked == oldItem.isChecked


    override fun areItemsTheSame(
        oldItem: CheckableListNote.Item,
        newItem: CheckableListNote.Item
    ): Boolean =
        newItem == oldItem
}