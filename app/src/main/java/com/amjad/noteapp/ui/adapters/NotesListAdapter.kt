package com.amjad.noteapp.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amjad.noteapp.R
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
                Log.i("BB", text)
                noteText.text = text
                setOnNoteClick {
                    //                    val direction =
                    it.findNavController().navigate(R.id.action_mainFragment_to_noteEditFragment)
                }
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