package com.lifedawn.capstoneapp.promise

import android.content.Context
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.CheckBox
import android.widget.ExpandableListView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import com.lifedawn.capstoneapp.R
import com.lifedawn.capstoneapp.common.constants.Constant
import com.lifedawn.capstoneapp.common.interfaces.OnClickedExpandableListItemListener
import com.lifedawn.capstoneapp.common.repository.CalendarRepository
import com.lifedawn.capstoneapp.common.viewmodel.FriendViewModel
import com.lifedawn.capstoneapp.databinding.ItemViewPromiseBinding
import com.lifedawn.capstoneapp.databinding.ParentviewPromiseExpandableListBinding
import com.lifedawn.capstoneapp.map.LocationDto
import com.lifedawn.capstoneapp.util.AttendeeUtil
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


class ExpandableListAdapter constructor(private val context: Context,
                                        private val parentList: ArrayList<ExpandableParentData>,
                                        private val childList: ArrayList<ArrayList<ExpandableChildData<CalendarRepository.EventObj>>>,
                                        private val SIGN_IN_ACCOUNT_NAME: String,
                                        private val DATE_TIME_FORMATTER: DateTimeFormatter)
    : BaseExpandableListAdapter() {
    lateinit var onClickedExpandableListItemListener: OnClickedExpandableListItemListener<CalendarRepository.EventObj>
    lateinit var onEditExpandableListItemListener: OnClickedExpandableListItemListener<CalendarRepository.EventObj>
    lateinit var onRemoveExpandableListItemListener: OnClickedExpandableListItemListener<CalendarRepository.EventObj>
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getGroupCount(): Int = parentList.size

    override fun getChildrenCount(groupPosition: Int): Int = childList[groupPosition].size

    override fun getGroup(groupPosition: Int): Any = parentList[groupPosition]

    override fun getChild(groupPosition: Int, childPosition: Int): Any = childList[groupPosition][childPosition]

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()

    override fun hasStableIds(): Boolean = false

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, view: View?, parent: ViewGroup?): View {
        if (view == null) {
            val binding = ParentviewPromiseExpandableListBinding.inflate(layoutInflater, parent, false)
            binding.expandTitle.text = parentList[groupPosition].title
            binding.root.tag = ParentHolder(1)

            return binding.root
        } else {
            return view
        }
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, view: View?, parent: ViewGroup?): View {
        if (view == null) {
            val binding = ItemViewPromiseBinding.inflate(layoutInflater)
            val eventObj = getChild(groupPosition, childPosition) as CalendarRepository.EventObj

            binding.editBtn.visibility = if (eventObj.isMyEvent) View.VISIBLE else View.GONE
            binding.root.setOnClickListener {
                onClickedExpandableListItemListener.onClicked(groupPosition, childPosition, eventObj)
            }

            binding.editBtn.setOnClickListener {
                val popupMenu = PopupMenu(context, binding.editBtn)

                popupMenu.menuInflater.inflate(R.menu.event_menu, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener { item ->
                    if (item!!.itemId == R.id.action_edit) {
                        onEditExpandableListItemListener.onClicked(groupPosition, childPosition, eventObj)
                    } else if (item!!.itemId == R.id.action_delete) {
                        onRemoveExpandableListItemListener.onClicked(groupPosition, childPosition, eventObj)
                    }
                    false
                }

                popupMenu.show()
            }

            val event = eventObj.event
            val dtStart = event.getAsString(CalendarContract.Events.DTSTART)
            val eventTimeZone = event.getAsString(CalendarContract.Events.EVENT_TIMEZONE)
            val start = ZonedDateTime.ofInstant(Instant.ofEpochMilli(dtStart.toLong()), ZoneId.of(eventTimeZone))

            binding.dateTime.text = start.format(DATE_TIME_FORMATTER)
            binding.description.text = if (event.getAsString(CalendarContract.Events.DESCRIPTION) == null
                    || event.getAsString(CalendarContract.Events.DESCRIPTION).isEmpty()) context.getString(R.string.noDescription) else event.getAsString(CalendarContract.Events.DESCRIPTION)

            binding.title.text = if (event.getAsString(CalendarContract.Events.TITLE) == null) context.getString(R.string.no_title) else event.getAsString(CalendarContract.Events.TITLE)

            if (event.getAsString(CalendarContract.Events.EVENT_LOCATION) != null) {
                if (event.getAsString(CalendarContract.Events.EVENT_LOCATION).isEmpty()) {
                    binding.location.text = context.getString(R.string.no_promise_location)
                } else {
                    val locationDto: LocationDto? = LocationDto.toLocationDto(event.getAsString(CalendarContract.Events.EVENT_LOCATION))

                    if (locationDto != null) {
                        binding.location.text
                        if (locationDto.locationType == Constant.ADDRESS) locationDto.addressName else locationDto.placeName
                    } else {
                        binding.location.text = event.getAsString(CalendarContract.Events.EVENT_LOCATION)
                    }

                }
            } else {
                binding.location.text = context.getString(R.string.no_promise_location)
            }

            val attendeeNameList = ArrayList<String>()
            attendeeNameList.add(if (event.getAsString(CalendarContract.Events.ORGANIZER).equals(SIGN_IN_ACCOUNT_NAME))
                context.getString(R.string.me)
            else
                FriendViewModel.getName(event.getAsString(CalendarContract.Events.ORGANIZER)))

            for (attendee in eventObj.attendeeList) {
                if (attendee.getAsString(CalendarContract.Attendees.ATTENDEE_EMAIL).equals(SIGN_IN_ACCOUNT_NAME)) {
                    attendeeNameList.add(context.getString(R.string.me));
                } else {
                    attendeeNameList.add(FriendViewModel.getName(attendee));
                }
            }

            val people = AttendeeUtil.toListString(attendeeNameList)
            binding.people.text = people

            binding.root.tag = ChildHolder(1)

            return binding.root
        } else {
            return view
        }

    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true

    fun setListViewHeight(listView: ExpandableListView, group: Int) {
        val listAdapter = listView.expandableListAdapter as ExpandableListAdapter
        var totalHeight = 0
        val desiredWidth: Int = View.MeasureSpec.makeMeasureSpec(
                listView.width,
                View.MeasureSpec.EXACTLY)
        for (i in 0 until listAdapter.groupCount) {
            val groupItem: View = listAdapter.getGroupView(i, false, null, listView)
            groupItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED)
            totalHeight += groupItem.measuredHeight

            if (listView.isGroupExpanded(i) && i != group
                    || !listView.isGroupExpanded(i) && i == group) {
                for (j in 0 until listAdapter.getChildrenCount(i)) {
                    val listItem: View = listAdapter.getChildView(
                            i, j, false, null, listView
                    )
                    listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED)
                    totalHeight += listItem.measuredHeight
                }
            }

            val params = listView.layoutParams
            var height = (totalHeight + listView.dividerHeight * (listAdapter.groupCount - 1))
            if (height < 10) {
                height = 200
            }
            params.height = height
            listView.layoutParams = params
            listView.requestLayout()
        }
    }

    data class ParentHolder(val i: Int)
    data class ChildHolder(val i: Int)
}