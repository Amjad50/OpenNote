package com.amjad.opennote.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amjad.opennote.data.Note
import com.amjad.opennote.databinding.NoteitemViewBinding
import com.amjad.opennote.ui.fragments.NotesListFragmentDirections

class NotesListAdapter(private val selector: NoteListSelector<Long>) :
    ListAdapter<Note, NotesListAdapter.NoteViewHolder>(_NoteListDiffItemCallBack()) {

    init {
        setHasStableIds(true)
        selector.addChangeObserver {
            // TODO: change to better update (performance)
            notifyDataSetChanged()
        }
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
        holder.bind(getItem(position))
    }

    inner class NoteViewHolder(private val binding: NoteitemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(note: Note) {
            binding.apply {
                this.note = note
                selected = selector.isSelected(note.id)
                setOnNoteClick {
                    if (!selector.hasSelection()) {
                        // open note for editing
                        val action =
                            NotesListFragmentDirections.actionMainFragmentToNoteEditFragment(noteId = note.id)
                        it.findNavController()
                            .navigate(action)
                    } else {
                        selector.toggle(note.id)
                    }
                }
                binding.noteViewCard.setOnLongClickListener {
                    if (!selector.hasSelection()) {
                        selector.select(note.id)
                    } else {
                        // TODO: change to preview the note and preserve the selection
                        selector.toggle(note.id)
                    }
                    true
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