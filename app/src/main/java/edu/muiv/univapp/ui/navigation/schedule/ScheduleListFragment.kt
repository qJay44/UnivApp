package edu.muiv.univapp.ui.navigation.schedule

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.muiv.univapp.R
import edu.muiv.univapp.databinding.FragmentScheduleListBinding
import edu.muiv.univapp.ui.navigation.schedule.model.ScheduleWithSubjectAndTeacher
import edu.muiv.univapp.ui.navigation.schedule.utils.OnTouchListenerRecyclerView

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

    private fun updateUI(scheduleForUserList: List<ScheduleWithSubjectAndTeacher>) {
        Log.i(TAG, "Got ${scheduleForUserList.size} schedules for user")
        adapter = ScheduleAdapter(scheduleForUserList)
        rvSchedule.adapter = adapter

        // Appearance of the items
        rvSchedule.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                rvSchedule.viewTreeObserver.removeOnPreDrawListener(this)

                for (view in rvSchedule.children) {
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

    // The Adapter
    private inner class ScheduleAdapter(scheduleForUserList: List<ScheduleWithSubjectAndTeacher>)
        : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val typeHeader = 0
        private val typeList = 1
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
            val view = if (viewType == typeHeader) {
                ScheduleHolderHeader(layoutInflater.inflate(R.layout.schedule_list_item_header, parent, false))
            } else {
                ScheduleHolder(layoutInflater.inflate(R.layout.schedule_list_item, parent, false))
            }

            return view
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val scheduleForUser = scheduleAll.elementAt(position)
            when (holder) {
                is ScheduleHolderHeader -> {
                    val dayIndex = scheduleListViewModel.week.indexOf(scheduleForUser.date)
                    val weekDayName = ScheduleWeekDays.getDayNameByIndex(dayIndex)
                    holder.bind(scheduleForUser.date, weekDayName)
                }
                is ScheduleHolder -> {
                    holder.bind(scheduleForUser)
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

        private val tvDay        : TextView = itemView.findViewById(R.id.tvDay)
        private val tvWeekDayName: TextView = itemView.findViewById(R.id.tvWeekDayName)

        fun bind(date: String, weekDayName: String) {
            tvDay.text = date
            tvWeekDayName.text = weekDayName
        }
    }
    ////////////////////////

    // Default view holder //

    private inner class ScheduleHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val tvTimeStart   : TextView = itemView.findViewById(R.id.tvTimeStart)
        private val tvTimeEnd     : TextView = itemView.findViewById(R.id.tvTimeEnd)
        private val tvSubjectName : TextView = itemView.findViewById(R.id.tvSubjectName)
        private val tvScheduleInfo: TextView = itemView.findViewById(R.id.tvScheduleInfo)

        fun bind(scheduleForUser: ScheduleWithSubjectAndTeacher) {
            val teacherField = "" +
                    "${scheduleForUser.teacherSurname} " +
                    "${scheduleForUser.teacherName[0]}. " +
                    "${scheduleForUser.teacherPatronymic[0]}."

            val details = "" +
                    "$teacherField | " +
                    "${scheduleForUser.type} | " +
                    "Ауд. ${scheduleForUser.roomNum}"

            tvScheduleInfo.text = details
            tvTimeStart.text = scheduleForUser.timeStart
            tvTimeEnd.text = scheduleForUser.timeEnd
            tvSubjectName.text = scheduleForUser.subjectName
        }
    }

    //////////////////////
}
