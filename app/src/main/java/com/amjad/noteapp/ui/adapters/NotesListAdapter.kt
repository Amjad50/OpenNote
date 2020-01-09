package com.amjad.noteapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amjad.noteapp.databinding.NoteitemViewBinding

class NotesListAdapter : ListAdapter<String, NotesListAdapter.NoteViewHolder>(_NoteListDiffItemCallBack()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder(NoteitemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }


    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class NoteViewHolder (private val binding: NoteitemViewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(text: String) {
            binding.apply {
                noteText.text = text
                executePendingBindings()
            }
        }
    }
}


private class _NoteListDiffItemCallBack : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }
}