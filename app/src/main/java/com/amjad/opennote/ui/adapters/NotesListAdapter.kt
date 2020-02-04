package com.amjad.opennote.ui.adapters

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amjad.opennote.R
import com.amjad.opennote.data.entities.Note
import com.amjad.opennote.data.entities.NoteType
import com.amjad.opennote.databinding.CheckablelistNoteViewBinding
import com.amjad.opennote.databinding.NoteitemViewBinding
import com.amjad.opennote.ui.fragments.NotesListFragmentDirections
import kotlin.math.min

class NotesListAdapter(private val selector: NoteListSelector<Long>) :
    ListAdapter<Note, NotesListAdapter.BaseNoteViewHolder>(NoteListDiffItemCallBack()) {

    init {
        setHasStableIds(true)
        selector.addChangeObserver {
            // TODO: change to better update (performance)
            notifyDataSetChanged()
        }
    }

    override fun getItemId(position: Int): Long = getItem(position).id

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).type == NoteType.TEXT_NOTE)
            VIEWTYPE_TEXTNOTE
        else
            VIEWTYPE_LISTNOTE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseNoteViewHolder {
        return when (viewType) {
            VIEWTYPE_TEXTNOTE -> TextNoteViewHolder(
                NoteitemViewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            VIEWTYPE_LISTNOTE ->
                CheckableListNoteViewHolder(
                    CheckablelistNoteViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            else -> {
                throw IllegalArgumentException("Unknown type with code $viewType")
            }
        }
    }

    override fun onBindViewHolder(holder: BaseNoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TextNoteViewHolder(private val binding: NoteitemViewBinding) :
        BaseNoteViewHolder(binding.root) {
        override fun bind(note: Note) {
            // FIXME: this is the same code as below, but cannot move to a method, as it uses two
            //  different binding classes :'(
            binding.apply {
                this.note = note
                selected = selector.isSelected(note.id)
                setOnNoteClick {
                    if (!selector.hasSelection()) {
                        val action =
                            NotesListFragmentDirections.actionMainFragmentToNoteEditFragment(
                                noteId = note.id
                            )

                        it.findNavController().navigate(action)
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

    inner class CheckableListNoteViewHolder(private val binding: CheckablelistNoteViewBinding) :
        BaseNoteViewHolder(binding.root) {
        override fun bind(note: Note) {
            binding.apply {

                val notelist = note.getCheckableListNote().noteList
                notelist.sort()


                var unchecked = notelist.indexOfFirst { it.isChecked }
                if (unchecked == -1) // reached to the end but didn't find any
                    unchecked = notelist.size

                val checked = notelist.size - unchecked

                val numberToView = min(unchecked, 5)
                val numberNotToView = unchecked - numberToView

                listContainer.removeAllViews()
                for (i in 0 until numberToView)
                    listContainer.addView(
                        CheckBox(listContainer.context).apply {
                            text = notelist[i].text
                            setTextColor(0x8a000000.toInt())
                            setButtonDrawable(R.drawable.ic_check_box_unchecked_for_notelist_item)
                            isEnabled = false
                            gravity = Gravity.START
                            isFocusable = false
                            isClickable = false
                        }
                    )

                if (numberNotToView > 0)
                    listContainer.addView(
                        TextView(binding.root.context).apply {
                            text = context.getString(R.string.checked_items_count, numberNotToView)
                        }
                    )
                if (checked > 0)
                    listContainer.addView(
                        TextView(binding.root.context).apply {
                            text = context.getString(R.string.checked_items_count, checked)
                        }
                    )

                this.note = note
                selected = selector.isSelected(note.id)
                setOnNoteClick {
                    if (!selector.hasSelection()) {
                        val action =
                            NotesListFragmentDirections.actionNoteListFragmentToCheckableListNoteEditFragment(
                                noteId = note.id
                            )

                        it.findNavController().navigate(action)
                    } else {
                        selector.toggle(note.id)
                    }
                }
                binding.noteViewCard.setOnLongClickListener {
                    if (!selector.hasSelection()) {
                        selector.select(note.id)
                    } else {
                        selector.toggle(note.id)
                    }
                    true
                }
                executePendingBindings()
            }
        }
    }

    abstract class BaseNoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(note: Note)
    }

    companion object {
        const val VIEWTYPE_TEXTNOTE = 1
        const val VIEWTYPE_LISTNOTE = 2
    }
}


private class NoteListDiffItemCallBack : DiffUtil.ItemCallback<Note>() {
    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean =
        newItem.id == oldItem.id


    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean =
        newItem == oldItem

}