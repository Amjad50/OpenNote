package com.amjad.opennote.ui.adapters

import androidx.recyclerview.widget.*

abstract class OffsettedListAdapter<T, VH : RecyclerView.ViewHolder>(
    diffCallback: DiffUtil.ItemCallback<T>,
    protected val headerImageOffset: Int = 0,
    private val headerOffset: Int = 0,
    private val footerOffset: Int = 0
) : RecyclerView.Adapter<VH>() {

    private val mDiffer = AsyncListDiffer<T>(
        OffsettedListUpdateCallback(),
        AsyncDifferConfig.Builder(diffCallback).build()
    )

    val currentList: List<T>
        get() = mDiffer.currentList

    override fun getItemViewType(position: Int): Int {
        return when {
            position < headerImageOffset -> VIEWTYPE_HEADER_IMAGE
            position >= headerImageOffset && position < headerImageOffset + headerOffset -> VIEWTYPE_HEADER
            position >= (itemCount - footerOffset) -> VIEWTYPE_FOOTER
            else -> VIEWTYPE_LIST_ITEM
        }
    }

    fun submitList(newList: List<T>) {
        mDiffer.submitList(newList)
    }

    fun getItem(position: Int): T {
        return mDiffer.currentList[position - headerOffset - headerImageOffset]
    }

    override fun getItemCount(): Int {
        return mDiffer.currentList.size + headerOffset + headerImageOffset + footerOffset
    }

    private inner class OffsettedListUpdateCallback : ListUpdateCallback {
        private fun offsetPosition(position: Int) = position + headerOffset + headerImageOffset

        override fun onChanged(position: Int, count: Int, payload: Any?) {
            notifyItemRangeChanged(offsetPosition(position), count, payload)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            notifyItemMoved(offsetPosition(fromPosition), offsetPosition(toPosition))
        }

        override fun onInserted(position: Int, count: Int) {
            notifyItemRangeInserted(offsetPosition(position), count)
        }

        override fun onRemoved(position: Int, count: Int) {
            notifyItemRangeRemoved(offsetPosition(position), count)
        }
    }

    companion object {
        const val VIEWTYPE_HEADER = 0
        const val VIEWTYPE_HEADER_IMAGE = 1
        const val VIEWTYPE_LIST_ITEM = 2
        const val VIEWTYPE_FOOTER = 3
    }
}