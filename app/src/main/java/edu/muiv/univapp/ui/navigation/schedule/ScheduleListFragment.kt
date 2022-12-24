package edu.muiv.univapp.ui.navigation.schedule

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.FontRes
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.muiv.univapp.R
import edu.muiv.univapp.databinding.FragmentScheduleListBinding
import edu.muiv.univapp.ui.navigation.schedule.model.ScheduleWithSubjectAndTeacher
import edu.muiv.univapp.ui.navigation.schedule.utils.AsyncCell
import edu.muiv.univapp.ui.navigation.schedule.utils.OnTouchListenerRecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ScheduleListFragment : Fragment() {

    companion object {
        private const val TAG = "ScheduleListFragment"
    }

    private enum class HolderViewTypes(val type: Int) {
        HEADER(0),
        DEFAULT(1)
    }

    private var _binding: FragmentScheduleListBinding? = null
    private val binding get() = _binding!!

    private lateinit var tvWeekDays: TextView
    private lateinit var rvSchedule: RecyclerView
    private lateinit var ibPrevWeek: ImageButton
    private lateinit var ibNextWeek: ImageButton
    private lateinit var adapter: ScheduleAdapter

    private val scheduleListViewModel: ScheduleListViewModel by lazy {
        ViewModelProvider(this)[ScheduleListViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null)
            scheduleListViewModel.loadCalendar()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleListBinding.inflate(inflater, container, false)
        val root: View = binding.root

        adapter = ScheduleAdapter(emptyList())
        tvWeekDays = binding.tvWeekDays

        ibPrevWeek = binding.ibPrevWeek
        ibPrevWeek.setOnClickListener {
            scheduleListViewModel.prevWeek()
        }

        ibNextWeek = binding.ibNextWeek
        ibNextWeek.setOnClickListener {
            scheduleListViewModel.nextWeek()
        }

        rvSchedule = binding.scheduleRecyclerView
        rvSchedule.layoutManager = LinearLayoutManager(context)
        rvSchedule.adapter = adapter

        // Swipes //

        rvSchedule.setOnTouchListener(object : OnTouchListenerRecyclerView(context, rvSchedule) {
            override fun onSwipeLeft(): Boolean {
                scheduleListViewModel.nextWeek()
                return true
            }
            override fun onSwipeRight(): Boolean {
                scheduleListViewModel.prevWeek()
                return true
            }

            override fun onClick(view: View, position: Int): Boolean {
                val schedule = adapter.getScheduleByPosition(position)
                val action = ScheduleListFragmentDirections
                    .actionNavigationScheduleListToNavigationSchedule(schedule.id.toString())
                view.findNavController().navigate(action)
                Log.i(TAG, "Selected schedule (date: ${schedule.date})")

                return true
            }
        })

        ////////////

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (scheduleListViewModel.isTeacher) {
            scheduleListViewModel.teacherSchedulesLiveData.observe(viewLifecycleOwner) { schedules ->
                schedules?.let {
                    updateUI(schedules)
                }
            }
        } else {
            scheduleListViewModel.studentSchedule.observe(viewLifecycleOwner) { schedules ->
                schedules?.let {
                    updateUI(schedules)
                }
            }
        }
        // Week borders
        scheduleListViewModel.dayFromTo.observe(viewLifecycleOwner) { dayFromToString ->
            tvWeekDays.text = dayFromToString
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUI(scheduleForUserList: List<ScheduleWithSubjectAndTeacher>) {
        Log.i(TAG, "Got ${scheduleForUserList.size} schedules for user")

        // Without this sorting 01.01 (2023) will be earlier than 10.12 (2022)
        val sortedScheduleList = scheduleForUserList.sortedBy {
            val df = SimpleDateFormat("dd.MM.yyyy", Locale.forLanguageTag("ru"))
            df.parse(it.date)!!.time
        }

        adapter = ScheduleAdapter(sortedScheduleList)
        rvSchedule.adapter = adapter
    }

    // The Adapter
    private inner class ScheduleAdapter(scheduleForUserList: List<ScheduleWithSubjectAndTeacher>)
        : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val scheduleAll: List<ScheduleWithSubjectAndTeacher>
        private val scheduleAllBooleans: List<Boolean>

        init {
            var currentWeekDay = ""
            val listWithSchedules: MutableList<ScheduleWithSubjectAndTeacher> = mutableListOf()
            val listWithBooleans: MutableList<Boolean> = mutableListOf()

            for (schedule in scheduleForUserList) {
                if (schedule.date != currentWeekDay) {
                    // Create schedule holder as header
                    listWithSchedules += schedule
                    listWithBooleans += true

                    // and then the same schedule as default holder
                    listWithSchedules += schedule
                    listWithBooleans += false
                } else {
                    // Create only default holder
                    listWithSchedules += schedule
                    listWithBooleans += false
                }
                currentWeekDay = schedule.date
            }

            scheduleAll = listWithSchedules.toList()
            scheduleAllBooleans = listWithBooleans.toList()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                HolderViewTypes.HEADER.type -> {
                    ScheduleHolderHeader(
                        HeaderItemCell(parent.context).apply { inflate() }
                    )
                }
                HolderViewTypes.DEFAULT.type -> {
                    ScheduleDefaultHolder(
                        DefaultItemCell(parent.context).apply { inflate() }
                    )
                }
                else -> throw IllegalStateException("onCreateViewHolder: Unexpected view type")
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val scheduleForUser = scheduleAll.elementAt(position)
            when (holder) {
                is ScheduleHolderHeader -> {
                    setUpHeaderViewHolder(
                        holder,
                        scheduleListViewModel.getSimpleDate(scheduleForUser.date),
                        scheduleListViewModel.getWeekDayNameByDate(scheduleForUser.date)
                    )
                }
                is ScheduleDefaultHolder -> {
                    setUpDefaultViewHolder(holder, scheduleForUser)
                }
                else -> Log.e(TAG, "onBindViewHolder: Got unexpected holder")
            }
        }

        override fun getItemCount(): Int = scheduleAll.size

        override fun getItemViewType(position: Int): Int {
            // Which holder to create
            return when (scheduleAllBooleans.elementAt(position)) {
                true  -> HolderViewTypes.HEADER.type
                false -> HolderViewTypes.DEFAULT.type
            }
        }

        // Use this function when async layout inflater can't set fonts by itself
        private fun TextView.font(@FontRes id: Int) {
            val typeface = ResourcesCompat.getFont(requireContext(), id)
            this.typeface = typeface
        }

        // Header holder binding
        private fun setUpHeaderViewHolder(
            holder: ScheduleHolderHeader,
            formattedDate: String,
            weekDayName: String
        ) {
            (holder.itemView as HeaderItemCell).bindWhenInflated {
                with (holder.itemView) {
                    val tvDay: TextView = findViewById(R.id.tvDay)
                    val tvWeekDayName: TextView = findViewById(R.id.tvWeekDayName)

                    tvDay.text = formattedDate
                    tvWeekDayName.text = weekDayName
                }
            }
        }

        // Default holder binding
        private fun setUpDefaultViewHolder(
            holder: ScheduleDefaultHolder,
            scheduleForUser: ScheduleWithSubjectAndTeacher
        ) {
            (holder.itemView as DefaultItemCell).bindWhenInflated {
                with (holder.itemView) {
                    val tvTimeStart   : TextView = findViewById(R.id.tvTimeStart)
                    val tvTimeEnd     : TextView = findViewById(R.id.tvTimeEnd)
                    val tvSubjectName : TextView = findViewById(R.id.tvSubjectName)
                    val tvScheduleInfo: TextView = findViewById(R.id.tvScheduleInfo)

                    val teacherField =
                        "${scheduleForUser.teacherSurname} " +
                        "${scheduleForUser.teacherName[0]}. " +
                        "${scheduleForUser.teacherPatronymic[0]}."

                    val details =
                        "$teacherField | " +
                        "${scheduleForUser.type} | " +
                        "Ауд. ${scheduleForUser.roomNum}"


                    tvTimeStart.font(R.font.montserrat_medium)
                    tvTimeEnd.font(R.font.montserrat_medium)
                    tvSubjectName.font(R.font.montserrat_semibold)
                    tvScheduleInfo.font(R.font.montserrat)

                    tvTimeStart.text = scheduleForUser.timeStart
                    tvTimeEnd.text = scheduleForUser.timeEnd
                    tvSubjectName.text = scheduleForUser.subjectName
                    tvScheduleInfo.text = details
                }
            }
        }

        // Inflate holders asynchronously //

        private inner class HeaderItemCell(context: Context) : AsyncCell(context) {
            override val layoutId = R.layout.schedule_list_item_header
        }

        private inner class DefaultItemCell(context: Context) : AsyncCell(context) {
            override val layoutId = R.layout.schedule_list_item
        }

        ////////////////////////////////////

        fun getScheduleByPosition(pos: Int) = scheduleAll.elementAt(pos)
    }

    private class ScheduleHolderHeader(view: View) : RecyclerView.ViewHolder(view)
    private class ScheduleDefaultHolder(view: View) : RecyclerView.ViewHolder(view)
}
