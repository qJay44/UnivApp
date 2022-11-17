package edu.muiv.univapp.ui.schedule

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.muiv.univapp.R
import edu.muiv.univapp.databinding.FragmentScheduleListBinding
import edu.muiv.univapp.user.UserDataHolder

class ScheduleListFragment : Fragment() {

    companion object {
        private const val TAG = "ScheduleListFragment"
    }

    private var _binding: FragmentScheduleListBinding? = null
    private val binding get() = _binding!!

    private lateinit var rvSchedule: RecyclerView
    private lateinit var tvWeedDays: TextView
    private lateinit var ibPrevWeek: ImageButton
    private lateinit var ibNextWeek: ImageButton
    private var adapter: ScheduleAdapter? = ScheduleAdapter(emptyList())
    private var pressedOnce = false

    private val scheduleListViewModel: ScheduleListViewModel by lazy {
        ViewModelProvider(this)[ScheduleListViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scheduleListViewModel.loadCalendar()
        scheduleListViewModel.loadUser()
        doubleBackExit()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleListBinding.inflate(inflater, container, false)
        val root: View = binding.root

        tvWeedDays = binding.tvWeekDays
        tvWeedDays.text = scheduleListViewModel.dayFromTo

        ibPrevWeek = binding.ibPrevWeek
        ibPrevWeek.setOnClickListener {
            Toast.makeText(requireContext(), "Prev", Toast.LENGTH_SHORT).show()
        }

        ibNextWeek = binding.ibNextWeek
        ibNextWeek.setOnClickListener {
            Toast.makeText(requireContext(), "Next", Toast.LENGTH_SHORT).show()
        }

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
                    updateUI(schedules)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.title = scheduleListViewModel.title
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.title = "UnivApp"
    }

    private fun updateUI(schedules: List<Schedule>) {
        adapter = ScheduleAdapter(schedules)
        rvSchedule.adapter = adapter
        Log.i(TAG, "Adapter has been updated")
    }

    private fun doubleBackExit() {
        requireActivity().onBackPressedDispatcher.addCallback(
            this, object : OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {
                    if (pressedOnce) {
                        UserDataHolder.uninitialize()
                        requireActivity().finish()
                    }

                    pressedOnce = true

                    Toast.makeText(
                        requireContext(),
                        "Click BACK again to exit",
                        Toast.LENGTH_SHORT
                    ).show()

                    Handler(Looper.myLooper()!!).postDelayed({ pressedOnce = false }, 2000)
                }
            }
        )
    }


    // The Adapter
    private inner class ScheduleAdapter(val currentList: List<Schedule>)
        : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val typeHeader = 0
        private val typeList = 1
        private var lastDay = ""

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = if (viewType == typeHeader) {
                ScheduleHolderHeader(layoutInflater.inflate(R.layout.schedule_list_item_header, parent, false))
            } else {
                ScheduleHolder(layoutInflater.inflate(R.layout.schedule_list_item, parent, false))
            }
            Log.i(TAG, "viewType: $viewType")

            return view
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val schedule = currentList[position]
            when (holder) {
                is ScheduleHolderHeader -> {
                    holder.bind(schedule, lastDay)
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
            val scheduleDate = currentList[position].date
            val dayIndex = scheduleListViewModel.days.indexOf(scheduleDate)
            val currentDay = ScheduleDay.values()[dayIndex].dayName

            // Create header view holder
            return if (lastDay != currentDay) {
                lastDay = currentDay
                typeHeader
            } else {
                typeList
            }
        }
    }

    private inner class ScheduleHolderHeader(view: View) : RecyclerView.ViewHolder(view) {

        private val tvDay        : TextView = itemView.findViewById(R.id.tvDay)
        private val tvWeekDayName: TextView = itemView.findViewById(R.id.tvWeekDayName)
        private val tvTimeStart  : TextView = itemView.findViewById(R.id.tvTimeStart)
        private val tvTimeEnd    : TextView = itemView.findViewById(R.id.tvTimeEnd)
        private val tvSubjectName: TextView = itemView.findViewById(R.id.tvSubjectName)
//        private val tvScheduleInfo: TextView = itemView.findViewById(R.id.tvScheduleInfo)

        fun bind(schedule: Schedule, weekDayName: String) {
            tvDay.text = schedule.date
            tvWeekDayName.text = weekDayName
            tvTimeStart.text = schedule.timeStart
            tvTimeEnd.text = schedule.timeEnd
            tvSubjectName.text = schedule.subjectName
        }
    }

    // View Holder
    private inner class ScheduleHolder(view: View)
        : RecyclerView.ViewHolder(view), View.OnClickListener {

        private lateinit var schedule: Schedule

        private val tvTimeStart   : TextView = itemView.findViewById(R.id.tvTimeStart)
        private val tvTimeEnd     : TextView = itemView.findViewById(R.id.tvTimeEnd)
        private val tvSubjectName : TextView = itemView.findViewById(R.id.tvSubjectName)
//        private val tvScheduleInfo: TextView = itemView.findViewById(R.id.tvScheduleInfo)
        // FIXME: Recycler scroll

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(scheduleDay: Schedule) {
            this.schedule = scheduleDay

            tvTimeStart.text = scheduleDay.timeStart
            tvTimeEnd.text = scheduleDay.timeEnd
            tvSubjectName.text = scheduleDay.subjectName
        }

        override fun onClick(p0: View?) {
            // TODO: Open a schedule day
            Log.i(TAG, "Selected schedule (date: ${schedule.date})")
        }
    }
}