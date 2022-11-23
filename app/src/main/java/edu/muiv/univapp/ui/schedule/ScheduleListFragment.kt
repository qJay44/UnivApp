package edu.muiv.univapp.ui.schedule

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.muiv.univapp.R
import edu.muiv.univapp.databinding.FragmentScheduleListBinding

class ScheduleListFragment : Fragment() {

    companion object {
        private const val TAG = "ScheduleListFragment"
    }

    private var _binding: FragmentScheduleListBinding? = null
    private val binding get() = _binding!!

    private lateinit var tvWeekDays: TextView
    private lateinit var rvSchedule: RecyclerView
    private lateinit var ibPrevWeek: ImageButton
    private lateinit var ibNextWeek: ImageButton

    private val scheduleListViewModel: ScheduleListViewModel by lazy {
        ViewModelProvider(this)[ScheduleListViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            scheduleListViewModel.loadUser()
            scheduleListViewModel.loadCalendar()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleListBinding.inflate(inflater, container, false)
        val root: View = binding.root

        tvWeekDays = binding.tvWeekDays
        tvWeekDays.text = scheduleListViewModel.dayFromTo

        ibPrevWeek = binding.ibPrevWeek
        ibPrevWeek.setOnClickListener {
            scheduleListViewModel.prevWeek()
            tvWeekDays.text = scheduleListViewModel.dayFromTo
        }

        ibNextWeek = binding.ibNextWeek
        ibNextWeek.setOnClickListener {
            scheduleListViewModel.nextWeek()
            tvWeekDays.text = scheduleListViewModel.dayFromTo
        }

        rvSchedule = binding.scheduleRecyclerView
        rvSchedule.layoutManager = LinearLayoutManager(context)
        rvSchedule.adapter = ScheduleAdapter(emptyList())

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (scheduleListViewModel.isTeacher) {
            scheduleListViewModel.teacherSchedulesLiveData.observe(viewLifecycleOwner) { schedules ->
                schedules?.let {
                    Log.i(TAG, "Got ${schedules.size} schedules for teacher")
                    updateUI(schedules)
                }
            }
        } else {
            scheduleListViewModel.studentSchedulesLiveData.observe(viewLifecycleOwner) { schedules ->
                schedules?.let {
                    Log.i(TAG, "Got ${schedules.size} schedules for student")
                    scheduleListViewModel.loadScheduleTeachers(schedules)
                    updateUI(schedules)
                }
            }
        }
        updateUI(emptyList())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUI(schedules: List<Schedule>) {
        rvSchedule.adapter = ScheduleAdapter(schedules)
    }

    // The Adapter
    private inner class ScheduleAdapter(currentList: List<Schedule>)
        : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val typeHeader = 0
        private val typeList = 1
        private val scheduleAll: List<Schedule>
        private val scheduleAllBooleans: List<Boolean>

        init {
            var currentWeekDay = ""
            val listWithSchedules: MutableList<Schedule> = mutableListOf()
            val listWithBooleans: MutableList<Boolean> = mutableListOf()

            for (schedule in currentList) {
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

            scheduleListViewModel.weekTeachersLiveData.observe(viewLifecycleOwner) { teachers ->
                scheduleListViewModel.teachersByIdLiveData.value = teachers.associateBy { it.id }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = if (viewType == typeHeader) {
                ScheduleHolderHeader(layoutInflater.inflate(R.layout.schedule_list_item_header, parent, false))
            } else {
                ScheduleHolder(layoutInflater.inflate(R.layout.schedule_list_item, parent, false))
            }

            return view
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val schedule = scheduleAll.elementAt(position)
            when (holder) {
                is ScheduleHolderHeader -> {
                    val dayIndex = scheduleListViewModel.week.indexOf(schedule.date)
                    val weekDayName = ScheduleWeekDays.getDayNameByIndex(dayIndex)
                    holder.bind(schedule, weekDayName)
                }
                is ScheduleHolder -> {
                    holder.bind(schedule)
                }
                else -> {
                    Log.e(TAG, "onBindViewHolder: unknown holder")
                }
            }
        }

        override fun getItemCount(): Int = scheduleAll.size

        override fun getItemViewType(position: Int): Int {
            return if (scheduleAllBooleans.elementAt(position)) {
                typeHeader
            } else {
                typeList
            }
        }
    }

    // Header view holder //

    private inner class ScheduleHolderHeader(view: View)
        : RecyclerView.ViewHolder(view), View.OnClickListener {

        private lateinit var schedule: Schedule

        private val tvDay        : TextView = itemView.findViewById(R.id.tvDay)
        private val tvWeekDayName: TextView = itemView.findViewById(R.id.tvWeekDayName)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(schedule: Schedule, weekDayName: String) {
            this.schedule = schedule

            tvDay.text = this.schedule.date
            tvWeekDayName.text = weekDayName
        }

        override fun onClick(p0: View?) {
            // TODO: Open a schedule day
            val action = ScheduleListFragmentDirections
                .actionNavigationScheduleListToNavigationSchedule(schedule.date)
            p0?.findNavController()?.navigate(action)
            Log.i(TAG, "Selected schedule (date: ${schedule.date})")
        }
    }
    ////////////////////////

    // Default view holder //

    private inner class ScheduleHolder(view: View)
        : RecyclerView.ViewHolder(view), View.OnClickListener {

        private lateinit var schedule: Schedule

        private val tvTimeStart   : TextView = itemView.findViewById(R.id.tvTimeStart)
        private val tvTimeEnd     : TextView = itemView.findViewById(R.id.tvTimeEnd)
        private val tvSubjectName : TextView = itemView.findViewById(R.id.tvSubjectName)
        private val tvScheduleInfo: TextView = itemView.findViewById(R.id.tvScheduleInfo)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(schedule: Schedule) {
            this.schedule = schedule

            scheduleListViewModel.teachersByIdLiveData.observe(viewLifecycleOwner) {
                val teacher = it[this.schedule.teacherID]
                val teacherField = "${teacher?.surname} ${teacher?.name?.get(0)}. ${teacher?.patronymic?.get(0)}."
                val details = "$teacherField | ${this.schedule.type} | Ауд. ${this.schedule.roomNum}"
                tvScheduleInfo.text = details
            }

            tvTimeStart.text = schedule.timeStart
            tvTimeEnd.text = schedule.timeEnd
            tvSubjectName.text = schedule.subjectName
        }

        override fun onClick(p0: View?) {
            val action = ScheduleListFragmentDirections
                .actionNavigationScheduleListToNavigationSchedule(schedule.id.toString())
            p0?.findNavController()?.navigate(action)

            Log.i(TAG, "Selected schedule (date: ${schedule.date})")
        }
    }

    //////////////////////
}