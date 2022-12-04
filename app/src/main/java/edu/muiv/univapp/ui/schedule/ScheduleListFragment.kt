package edu.muiv.univapp.ui.schedule

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.muiv.univapp.R
import edu.muiv.univapp.databinding.FragmentScheduleListBinding
import edu.muiv.univapp.utils.OnTouchListenerItem
import edu.muiv.univapp.utils.OnTouchListenerRecyclerView

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
    private lateinit var adapter: ScheduleAdapter

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

        rvSchedule.addOnItemTouchListener(OnTouchListenerItem(
            requireContext(), rvSchedule, object : OnTouchListenerItem.OnTouchActionListener {

            override fun onLeftSwipe(view: View, position: Int) {
                scheduleListViewModel.nextWeek()
            }

            override fun onRightSwipe(view: View, position: Int) {
                scheduleListViewModel.prevWeek()
            }

            override fun onClick(view: View, position: Int) {
                val schedule = adapter.getScheduleByPosition(position)
                val action = ScheduleListFragmentDirections
                    .actionNavigationScheduleListToNavigationSchedule(schedule.id.toString())
                view.findNavController().navigate(action)
                Log.i(TAG, "Selected schedule (date: ${schedule.date})")
            }
        }))

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
        scheduleListViewModel.dayFromTo.observe(viewLifecycleOwner) { dayFromToString ->
            tvWeekDays.text = dayFromToString
        }

        // Wait for animations availability
        postponeEnterTransition()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun updateUI(schedules: List<Schedule>) {
        adapter = ScheduleAdapter(schedules)
        rvSchedule.adapter = adapter

        // Apply extra listener to the recycler view if the list is empty
        rvSchedule.setOnTouchListener(
            if (schedules.isEmpty()) getOnTouchListener() else null
        )

        // Appearance of the items
        rvSchedule.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                rvSchedule.viewTreeObserver.removeOnPreDrawListener(this)

                for (i in 0 until rvSchedule.childCount) {
                    val view = rvSchedule.getChildAt(i)
                    view.alpha = 0f
                    view.animate().alpha(1f)
                        .setDuration(300)
                        .start()
                }

                return true
            }
        })

        // Allow animations to play
        startPostponedEnterTransition()
    }

    // Touch listener for whole recycler view
    private fun getOnTouchListener(): OnTouchListenerRecyclerView {
        return object : OnTouchListenerRecyclerView(context) {
            override fun onSwipeLeft(): Boolean {
                scheduleListViewModel.nextWeek()
                return true
            }
            override fun onSwipeRight(): Boolean {
                scheduleListViewModel.prevWeek()
                return true
            }
        }
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
                scheduleListViewModel.teachersWithId.value = teachers.associateBy { it.id }
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

        fun getScheduleByPosition(pos: Int) = scheduleAll.elementAt(pos)
    }

    // Header view holder //

    private inner class ScheduleHolderHeader(view: View) : RecyclerView.ViewHolder(view) {

        private lateinit var schedule: Schedule

        private val tvDay        : TextView = itemView.findViewById(R.id.tvDay)
        private val tvWeekDayName: TextView = itemView.findViewById(R.id.tvWeekDayName)

        fun bind(schedule: Schedule, weekDayName: String) {
            this.schedule = schedule

            tvDay.text = this.schedule.date
            tvWeekDayName.text = weekDayName
        }
    }
    ////////////////////////

    // Default view holder //

    private inner class ScheduleHolder(view: View) : RecyclerView.ViewHolder(view) {

        private lateinit var schedule: Schedule

        private val tvTimeStart   : TextView = itemView.findViewById(R.id.tvTimeStart)
        private val tvTimeEnd     : TextView = itemView.findViewById(R.id.tvTimeEnd)
        private val tvSubjectName : TextView = itemView.findViewById(R.id.tvSubjectName)
        private val tvScheduleInfo: TextView = itemView.findViewById(R.id.tvScheduleInfo)

        fun bind(schedule: Schedule) {
            this.schedule = schedule

            scheduleListViewModel.teachersWithId.observe(viewLifecycleOwner) { teachersMap ->
                val teacher = teachersMap[schedule.teacherID]
                teacher?.let {
                    val teacherField = "${it.surname} ${it.name[0]}. ${it.patronymic[0]}."
                    val details = "$teacherField | ${this.schedule.type} | Ауд. ${this.schedule.roomNum}"
                    tvScheduleInfo.text = details
                }
            }

            tvTimeStart.text = schedule.timeStart
            tvTimeEnd.text = schedule.timeEnd
            tvSubjectName.text = schedule.subjectName
        }
    }

    //////////////////////
}
