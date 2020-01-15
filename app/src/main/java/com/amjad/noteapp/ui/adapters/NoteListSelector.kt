package com.amjad.noteapp.ui.adapters

class NoteListSelector<T> {

    val selection = sortedSetOf<T>()

    private var _handlers = hashSetOf<() -> Unit>()

    fun isSelected(id: T): Boolean =
        selection.find { it == id } != null

    /**
     * @return true if the item is set as selected now, meaning it wasn't in the list before
     */
    fun select(id: T): Boolean = handlerCaller {
        selection.add(id)
    }

    /**
     * @return true if the value was in selection before
     */
    fun deselect(id: T): Boolean = handlerCaller {
        selection.remove(id)
    }

    fun toggle(id: T): Unit = handlerCaller {
        if (isSelected(id)) deselect(id) else select(id)
    }

    fun addChangeObserver(handler: () -> Unit) {
        _handlers.add(handler)
    }

    fun setItemsSelection(items: List<T>, value: Boolean) = handlerCaller {
        val handler: (T) -> Unit = if (value) { id -> select(id) } else { id -> deselect(id) }
        items.forEach {
            handler(it)
        }
    }

    fun hasSelection(): Boolean =
        selection.isNotEmpty()

    fun clearSelection() = handlerCaller {
        selection.clear()
    }

    private fun <T> handlerCaller(body: () -> T): T {
        val retVal = body()
        _handlers.forEach { it() }
        return retVal
    }

}