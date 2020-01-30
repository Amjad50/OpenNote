package com.amjad.opennote.ui.adapters

import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class CheckableNoteListAdapter :
    ListAdapter<Pair<String, Boolean>, CheckableNoteListAdapter.CheckableListNoteItemViewHolder>(
        CheckableNoteListDiffItemCallBack()
    ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CheckableListNoteItemViewHolder {
        return CheckableListNoteItemViewHolder(parent.context)
    }

    override fun onBindViewHolder(holder: CheckableListNoteItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CheckableListNoteItemViewHolder(context: Context) :
        RecyclerView.ViewHolder(TextView(context)) {
        fun bind(note: Pair<String, Boolean>) {
            (itemView as TextView).text = note.first
        }
    }
}


private class CheckableNoteListDiffItemCallBack : DiffUtil.ItemCallback<Pair<String, Boolean>>() {
    override fun areContentsTheSame(
        oldItem: Pair<String, Boolean>,
        newItem: Pair<String, Boolean>
    ): Boolean =
        newItem.first == oldItem.first && newItem.second == oldItem.second


    override fun areItemsTheSame(
        oldItem: Pair<String, Boolean>,
        newItem: Pair<String, Boolean>
    ): Boolean =
        newItem.hashCode() == oldItem.hashCode()
}