package com.amjad.noteapp.ui.adapters

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView

class NoteItemDetailsLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<Long>() {
    override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
        val child = recyclerView.findChildViewUnder(e.x, e.y)

        return child?.let {
            (recyclerView.getChildViewHolder(child) as NotesListAdapter.NoteViewHolder).getItemDetails()
        }
    }

}