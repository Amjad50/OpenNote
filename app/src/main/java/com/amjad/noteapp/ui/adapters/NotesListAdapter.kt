package com.amjad.noteapp.ui.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amjad.noteapp.R
import com.amjad.noteapp.data.Note
import com.amjad.noteapp.databinding.NoteitemViewBinding
import com.amjad.noteapp.ui.fragments.NoteEditFragment

class NotesListAdapter :
    ListAdapter<Note, NotesListAdapter.NoteViewHolder>(_NoteListDiffItemCallBack()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder(
            NoteitemViewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class NoteViewHolder(private val binding: NoteitemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(note: Note) {
            binding.apply {
                noteText.text = note.title!!
                setOnNoteClick {
                    val bundle = Bundle()
                    bundle.putInt(NoteEditFragment.NOTEID_ARGUMENT, note.id)
                    it.findNavController()
                        .navigate(R.id.action_mainFragment_to_noteEditFragment, bundle)
                }
                executePendingBindings()
            }
        }
    }
}


private class _NoteListDiffItemCallBack : DiffUtil.ItemCallback<Note>() {
    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean =
        newItem.id == oldItem.id


    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean =
        newItem == oldItem

}