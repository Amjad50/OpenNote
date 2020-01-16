package com.amjad.noteapp.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class EmptyStatableRecyclerView : RecyclerView {

    var emptyView: View? = null
//    private var isEmptyNow = false

    private val emptyStateObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() =
            updateEmptyState()

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) =
            updateEmptyState()

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) =
            updateEmptyState()

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) =
            updateEmptyState()

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) =
            updateEmptyState()
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun setAdapter(adapter: Adapter<*>?) {
        this.adapter?.unregisterAdapterDataObserver(emptyStateObserver)
        adapter?.registerAdapterDataObserver(emptyStateObserver)
        super.setAdapter(adapter)

        updateEmptyState()
    }

    private fun updateEmptyState() {
        val isEmpty = adapter?.itemCount == 0
        visibility = if (isEmpty) GONE else VISIBLE
        emptyView?.visibility = if (isEmpty) VISIBLE else GONE


        // TODO: use this cross fade later (fix it)
//        if (isEmpty xor isEmptyNow) {
//            isEmptyNow = isEmpty
//
//            val previousView = if (isEmpty) this else emptyView
//            val currentView = if (isEmpty) emptyView else this
//
//            ObjectAnimator.ofFloat(previousView, "alpha", 1.0f, 0.0f).apply {
//                duration = 100L
//                addListener(object : AnimatorListenerAdapter() {
//                    override fun onAnimationEnd(animation: Animator) {
//                        previousView?.visibility = View.GONE
//                        currentView?.visibility = View.VISIBLE
//                        ObjectAnimator.ofFloat(currentView, "alpha", 0.0f, 1.0f).setDuration(100L)
//                            .start()
//                    }
//                })
//            }.start()
//        }
    }


}