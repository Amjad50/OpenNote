package com.amjad.noteapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amjad.noteapp.data.Note
import com.amjad.noteapp.databinding.NoteitemViewBinding
import com.amjad.noteapp.ui.fragments.NotesListFragmentDirections

class NotesListAdapter :
    ListAdapter<Note, NotesListAdapter.NoteViewHolder>(_NoteListDiffItemCallBack()) {

    var tracker: SelectionTracker<Long>? = null

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = getItem(position).id

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
        holder.bind(getItem(position), tracker?.isSelected(getItemId(position)) ?: false)
    }

    class NoteViewHolder(private val binding: NoteitemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(note: Note, isSelected: Boolean) {
            binding.apply {
                noteTitle = note.title
                selected = isSelected
                setOnNoteClick {
                    val action =
                        NotesListFragmentDirections.actionMainFragmentToNoteEditFragment(noteId = note.id)
                    it.findNavController()
                        .navigate(action)
                }
                executePendingBindings()
            }
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getSelectionKey(): Long = itemId
                override fun getPosition(): Int = adapterPosition
            }

    }

    companion object {
        const val SELECTION_ID: String = "note_selection"
    }
}


private class _NoteListDiffItemCallBack : DiffUtil.ItemCallback<Note>() {
    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean =
        newItem.id == oldItem.id


    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean =
        newItem == oldItem

}