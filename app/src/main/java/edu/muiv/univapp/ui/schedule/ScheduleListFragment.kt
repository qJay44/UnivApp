package edu.muiv.univapp.ui.schedule

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.muiv.univapp.R
import edu.muiv.univapp.databinding.FragmentScheduleListBinding
import edu.muiv.univapp.user.Teacher

class ScheduleListFragment : Fragment() {

    companion object {
        private const val TAG = "ScheduleListFragment"
    }

    private var _binding: FragmentScheduleListBinding? = null
    private val binding get() = _binding!!

    private lateinit var tvWeekDays: TextView

    private lateinit var rvSchedule: RecyclerView
    private var adapter: ScheduleAdapter = ScheduleAdapter(emptyList())

    private val scheduleListViewModel: ScheduleListViewModel by lazy {
        ViewModelProvider(this)[ScheduleListViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scheduleListViewModel.loadCalendar()
        scheduleListViewModel.loadUser()
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

        rvSchedule = binding.scheduleRecyclerView
        rvSchedule.layoutManager = LinearLayoutManager(context)
        rvSchedule.adapter = adapter

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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUI(schedules: List<Schedule>) {
        adapter = ScheduleAdapter(schedules)
        rvSchedule.adapter = adapter
    }

    // The Adapter
    private inner class ScheduleAdapter(private val currentList: List<Schedule>)
        : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val typeHeader = 0
        private val typeList = 1
        // TODO: Try as setOf
        private val viewTypeIsHeader: Array<Boolean> = Array(currentList.size) { false }

        init {
            var currentWeekDay = ""
            for (i in currentList.indices) {
                if (currentList[i].date != currentWeekDay) {
                    currentWeekDay = currentList[i].date
                    viewTypeIsHeader[i] = true
                }
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
            val schedule = currentList[position]
            when (holder) {
                is ScheduleHolderHeader -> {
                    val dayIndex = scheduleListViewModel.days.indexOf(schedule.date)
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

        override fun getItemCount(): Int = currentList.size

        override fun getItemViewType(position: Int): Int {
            return if (viewTypeIsHeader[position]) {
                typeHeader
            } else {
                typeList
            }
        }
    }

    // View holder with header //
    // TODO: Cut header from schedule part
    // TODO: Load teachers without loop

    private inner class ScheduleHolderHeader(view: View)
        : RecyclerView.ViewHolder(view), View.OnClickListener {

        private lateinit var schedule: Schedule
        private lateinit var teacher: Teacher

        private val tvDay         : TextView = itemView.findViewById(R.id.tvDay)
        private val tvWeekDayName : TextView = itemView.findViewById(R.id.tvWeekDayName)
        private val tvTimeStart   : TextView = itemView.findViewById(R.id.tvTimeStart)
        private val tvTimeEnd     : TextView = itemView.findViewById(R.id.tvTimeEnd)
        private val tvSubjectName : TextView = itemView.findViewById(R.id.tvSubjectName)
        private val tvScheduleInfo: TextView = itemView.findViewById(R.id.tvScheduleInfo)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(schedule: Schedule, weekDayName: String) {
            this.schedule = schedule
            scheduleListViewModel.weekTeachersLiveData.observe(viewLifecycleOwner) { teachers ->
                for (teacher in teachers) {
                    if (teacher.id == schedule.teacherID) this.teacher = teacher
                }
                val teacherField = "${teacher.surname} ${teacher.name[0]}. ${teacher.patronymic[0]}."
                val details = "$teacherField | ${this.schedule.type} | Ауд. ${this.schedule.roomNum}"

                tvScheduleInfo.text = details
            }

            tvDay.text = this.schedule.date
            tvWeekDayName.text = weekDayName
            tvTimeStart.text = this.schedule.timeStart
            tvTimeEnd.text = this.schedule.timeEnd
            tvSubjectName.text = this.schedule.subjectName
        }

        override fun onClick(p0: View?) {
            // TODO: Open a schedule day
            Log.i(TAG, "Selected schedule (date: ${schedule.date})")
        }
    }
    ////////////////////////

    // Main view holder //

    private inner class ScheduleHolder(view: View)
        : RecyclerView.ViewHolder(view), View.OnClickListener {

        private lateinit var schedule: Schedule
        private lateinit var teacher: Teacher

        private val tvTimeStart   : TextView = itemView.findViewById(R.id.tvTimeStart)
        private val tvTimeEnd     : TextView = itemView.findViewById(R.id.tvTimeEnd)
        private val tvSubjectName : TextView = itemView.findViewById(R.id.tvSubjectName)
        private val tvScheduleInfo: TextView = itemView.findViewById(R.id.tvScheduleInfo)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(schedule: Schedule) {
            this.schedule = schedule
            scheduleListViewModel.weekTeachersLiveData.observe(viewLifecycleOwner) { teachers ->
                for (teacher in teachers) {
                    if (teacher.id == schedule.teacherID) this.teacher = teacher
                }

                val teacherField = "${teacher.surname} ${teacher.name[0]}. ${teacher.patronymic[0]}."
                val details = "$teacherField | ${this.schedule.type} | Ауд. ${this.schedule.roomNum}"

                tvScheduleInfo.text = details
            }

            tvTimeStart.text = schedule.timeStart
            tvTimeEnd.text = schedule.timeEnd
            tvSubjectName.text = schedule.subjectName
        }

        override fun onClick(p0: View?) {
            // TODO: Open a schedule day
            Log.i(TAG, "Selected schedule (date: ${schedule.date})")
        }
    }

    //////////////////////
}