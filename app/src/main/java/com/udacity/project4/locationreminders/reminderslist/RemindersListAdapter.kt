package com.udacity.project4.locationreminders.reminderslist

import com.udacity.project4.R
import com.udacity.project4.base.BaseRecyclerViewAdapter
import com.udacity.project4.base.DataBindingViewHolder

/**
 * @DrStart:     Adapter for the [RecyclerView] in [RemindersListFragment].
 */
class RemindersListAdapter(private val callBack: (selectedReminder: ReminderDataItem) -> Unit) :
    BaseRecyclerViewAdapter<ReminderDataItem>() {
    override fun getLayoutRes(viewType: Int) = R.layout.it_reminder
    override fun onBindViewHolder(holder: DataBindingViewHolder<ReminderDataItem>, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.itemView.setOnClickListener {
            callBack(getItem(position))
        }
    }

}
