package com.amjad.opennote.ui.adapters

import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.BackgroundColorSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amjad.opennote.R
import com.amjad.opennote.data.entities.CheckableListNote
import com.amjad.opennote.data.entities.Note
import com.amjad.opennote.data.entities.NoteType
import com.amjad.opennote.databinding.NoteitemViewBinding
import com.amjad.opennote.ui.fragments.NotesListFragmentDirections
import kotlin.math.min

class NotesListAdapter(
    private val selector: NoteListSelector<Long>,
    private val highlightColor: Int
) :
    ListAdapter<Note, NotesListAdapter.NoteViewHolder>(NoteListDiffItemCallBack()) {

    private var currentFilter = ""

    init {
        setHasStableIds(true)
        selector.addChangeObserver {
            // TODO: change to better update (performance)
            //  Maybe use a mapping system between the id of the note and its position to trigger
            //  update events only to the needed notes.
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

    fun updateFilter(filter: String) {
        currentFilter = filter
        // update every item
        notifyDataSetChanged()
    }

    inner class NoteViewHolder(private val binding: NoteitemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private fun openNote(note: Note) {
            val action =
                when (note.type) {
                    NoteType.TEXT_NOTE ->
                        NotesListFragmentDirections.actionMainFragmentToNoteEditFragment(
                            noteId = note.id
                        )
                    NoteType.CHECKABLE_LIST_NOTE ->
                        NotesListFragmentDirections.actionNoteListFragmentToCheckableListNoteEditFragment(
                            noteId = note.id
                        )
                    NoteType.UNDEFINED_TYPE ->
                        throw IllegalArgumentException("UNDEFINED_TYPE note, should not exist")
                }

            binding.root.findNavController().navigate(action)
        }

        fun bind(note: Note) {
            binding.apply {

                noteTitleView.text = getSpannedText(note.title, currentFilter)

                // clear views
                binding.innerNoteContainer.removeAllViews()

                if (note.type == NoteType.CHECKABLE_LIST_NOTE)
                    bindCheckableListNote(note.getCheckableListNote())
                else
                    bindTextNote(note)

                this.note = note
                selected = selector.isSelected(note.id)
                setOnNoteClick {
                    if (!selector.hasSelection()) {
                        openNote(note)
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

        private fun bindCheckableListNote(note: CheckableListNote) {
            val notelist = note.noteList
            notelist.sort()


            var unchecked = notelist.indexOfFirst { it.isChecked }
            if (unchecked == -1) // reached to the end but didn't find any
                unchecked = notelist.size

            val checked = notelist.size - unchecked

            val numberToView = min(unchecked, MAX_SHOWN_CHECKABLE_NOTE_LIST_ITEMS)
            val numberNotToView = unchecked - numberToView

            binding.innerNoteContainer.run {
                for (i in 0 until numberToView)
                    addView(
                        CheckBox(context).apply {
                            // TODO: show the items in the list that match the search

                            // FIXME: This is a feature, start with 'x' if you want to match
                            //  CheckableNoteList only, but u need to search for the first part of
                            //  the list item only. XD ::  BUG -> FEATURE
                            val filter = if (currentFilter.startsWith('x', true))
                                currentFilter.substring(1)
                            else
                                currentFilter

                            // apply the filter colors if they apply
                            text = getSpannedText(notelist[i].text, filter)
                            // match the color of the text to the default note text
                            setTextColor(DEFAULT_NOTE_VIEW_TEXT_COLOR)
                            setButtonDrawable(R.drawable.ic_check_box_unchecked_for_notelist_item)

                            // this checkbox is just for show and is treated as text
                            //  (does not take action).
                            isEnabled = false
                            gravity = Gravity.START
                            isFocusable = false
                            isClickable = false
                        }
                    )

                if (numberNotToView > 0)
                    addView(
                        TextView(binding.root.context).apply {
                            text =
                                context.getString(R.string.unchecked_items_count, numberNotToView)
                        }
                    )
                if (checked > 0)
                    addView(
                        TextView(binding.root.context).apply {
                            text = context.getString(R.string.checked_items_count, checked)
                        }
                    )
            }
        }


        private fun bindTextNote(note: Note) {
            binding.innerNoteContainer.run {
                addView(
                    TextView(context).apply {
                        ellipsize = TextUtils.TruncateAt.END
                        maxLines = 5
                        text = getSpannedText(note.note, currentFilter)
                    })
            }
        }

        // FIXME: slow algorithm to compute the spans
        private fun getSpannedText(string: String, filter: String): SpannableString {
            val spannedNote = SpannableString(string)
            if (filter.isNotEmpty()) {
                var lastIndex = -1

                do {
                    lastIndex = spannedNote.indexOf(filter, lastIndex + 1, true)

                    if (lastIndex == -1)
                        break

                    spannedNote.setSpan(
                        BackgroundColorSpan(highlightColor),
                        lastIndex,
                        lastIndex + filter.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                } while (true)
            }
            return spannedNote
        }
    }

    companion object {
        private const val MAX_SHOWN_CHECKABLE_NOTE_LIST_ITEMS = 5
        private const val DEFAULT_NOTE_VIEW_TEXT_COLOR = 0x8a000000.toInt()
    }
}


private class NoteListDiffItemCallBack : DiffUtil.ItemCallback<Note>() {
    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean =
        newItem.id == oldItem.id


    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean =
        newItem == oldItem

}