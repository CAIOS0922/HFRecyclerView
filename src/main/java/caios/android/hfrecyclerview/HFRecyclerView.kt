package caios.android.hfrecyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewParent
import androidx.recyclerview.widget.RecyclerView
import kotlin.experimental.inv

abstract class HFRecyclerView(
    private val isActiveHeader: Boolean,
    private val isActiveInsert: Boolean,
    private val isActiveHooter: Boolean
): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    companion object {
        private const val TYPE_HEADER = 1
        private const val TYPE_INSERT = 2
        private const val TYPE_HOOTER = 3
        private const val TYPE_ITEM = 4
    }

    //Create item view
    protected abstract fun getHeaderView(inflater: LayoutInflater, parent: ViewGroup): RecyclerView.ViewHolder?

    protected abstract fun getInsertView(inflater: LayoutInflater, parent: ViewGroup): RecyclerView.ViewHolder?

    protected abstract fun getHooterView(inflater: LayoutInflater, parent: ViewGroup): RecyclerView.ViewHolder?

    protected abstract fun getItemView(inflater: LayoutInflater, parent: ViewGroup): RecyclerView.ViewHolder

    //get item data count
    protected abstract fun getNormalItemCount(): Int

    protected abstract fun getInsertItemCount(): Int

    private fun getHeaderCount(): Int = (if(isActiveHeader) 1 else 0)

    private fun getHooterCount(): Int = (if(isActiveHooter) 1 else 0)

    private fun getInsertCount(): Int = (if(isActiveInsert) getNormalItemCount() / getSpacing() else 0)

    //get item spacing
    protected abstract fun getInsertItemSpacing(): Int

    protected abstract fun onSetViewHolder(holder: RecyclerView.ViewHolder, position: Int)

    override fun getItemCount(): Int = getHeaderCount() + getHooterCount() + getInsertCount() + getNormalItemCount()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            TYPE_HEADER -> getHeaderView(inflater, parent) ?: throw RuntimeException("Empty view")
            TYPE_HOOTER -> getHooterView(inflater, parent) ?: throw RuntimeException("Empty view")
            TYPE_INSERT -> getInsertView(inflater, parent) ?: throw RuntimeException("Empty view")
            TYPE_ITEM   -> getItemView(inflater, parent)
            else        -> throw RuntimeException("Unknown view type.")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when {
            isActiveHeader && isPositionHeader(position) -> onSetViewHolder(holder, 0)
            isActiveHooter && isPositionHooter(position) -> onSetViewHolder(holder, 0)
            isActiveInsert && isPositionInsert(position) -> onSetViewHolder(holder, getInsertIndex(position))
            else                                         -> onSetViewHolder(holder, getNormalIndex(position))
        }
    }

    override fun getItemViewType(position: Int): Int = when {
        isActiveHeader && isPositionHeader(position) -> TYPE_HEADER
        isActiveHooter && isPositionHooter(position) -> TYPE_HOOTER
        isActiveInsert && isPositionInsert(position) -> TYPE_INSERT
        else                                         -> TYPE_ITEM
    }

    private fun getSpacing(): Int {
        val spacing = getInsertItemSpacing()
        return when {
            isActiveInsert && spacing > 0 -> spacing
            !isActiveInsert               -> 0
            else                          -> throw IllegalStateException("If isActiveInsert is true, getInsertItemSpacing must be greater than or equal to 1.")
        }
    }

    private fun isPositionHeader(position: Int): Boolean = (position == 0)

    private fun isPositionHooter(position: Int): Boolean = (position == itemCount - 1)

    private fun isPositionInsert(position: Int): Boolean = ((position + if(getHeaderCount() == 0) 1 else 0) % (getSpacing() + 1) == 0 && getInsertIndex(position) < getInsertItemCount())

    private fun getInsertIndex(realPosition: Int): Int = (if(isActiveInsert) (realPosition + if(getHeaderCount() == 0) 1 else 0) / (getSpacing() + 1) else 0)

    private fun getNormalIndex(realPosition: Int): Int = (realPosition - getHeaderCount() - getInsertIndex(realPosition))
}