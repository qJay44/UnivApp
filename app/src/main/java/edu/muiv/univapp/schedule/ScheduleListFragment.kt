package edu.muiv.univapp.schedule

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.muiv.univapp.R
import edu.muiv.univapp.user.DatabaseTestDataBuilder

class ScheduleListFragment : Fragment() {

    companion object {
        private const val TAG = "ScheduleListFragment"
        private const val ADD_TEST_DATA = false

        fun newInstance(bundle: Bundle): ScheduleListFragment {
            return ScheduleListFragment().apply {
                arguments = bundle
            }
        }
    }

    interface Callbacks {
        fun onScheduleDaySelect(scheduleDate: String)
    }

    private lateinit var rvSchedule: RecyclerView
    private var callbacks: Callbacks? = null
    private var adapter: ScheduleAdapter? = ScheduleAdapter()
    private var pressedOnce = false

    private val scheduleListViewModel: ScheduleListViewModel by lazy {
        ViewModelProvider(this)[ScheduleListViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ADD_TEST_DATA) {
            for (schedule in DatabaseTestDataBuilder.scheduleList)
                scheduleListViewModel.addSchedule(schedule)
            Log.i(TAG, "Schedules were added: ${DatabaseTestDataBuilder.scheduleList.size}")
        }

        scheduleListViewModel.loadUser(arguments)
        doubleBackExit()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        scheduleListViewModel.scheduleListLiveData.observe(viewLifecycleOwner) {
            Log.d(TAG, "schedulesAll observe")
        }

        val view = inflater.inflate(R.layout.fragment_schedule_list, container, false)

        rvSchedule = view.findViewById(R.id.schedule_recycler_view)
        rvSchedule.layoutManager = LinearLayoutManager(context)
        rvSchedule.adapter = adapter

        return view
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

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.title = "UnivApp"
    }

    private fun updateUI(schedules: List<Schedule>) {
        adapter?.submitList(schedules)
        Log.i(TAG, "Adapter has been updated")
    }

    private fun doubleBackExit() {
        requireActivity().onBackPressedDispatcher.addCallback(
            this, object : OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {
                    if (pressedOnce) requireActivity().finish()

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

    // View Holder
    private inner class ScheduleHolder(view: View)
        : RecyclerView.ViewHolder(view), View.OnClickListener {

            private lateinit var scheduleDay: Schedule

            private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
            private val tvTimeStartEnd: TextView = itemView.findViewById(R.id.tvTimeStartEnd)
            private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(scheduleDay: Schedule) {
            this.scheduleDay = scheduleDay

            // FIXME: getting all schedules latency

            val schedulesAll = scheduleListViewModel.scheduleListLiveData.value
            Log.i(TAG, "General schedules: ${schedulesAll?.size}")
            schedulesAll?.let {
                var timeStart = ""
                var timeEnd = ""
                var size = 0

                for (schedule in it) {
                    if (schedule.date == this@ScheduleHolder.scheduleDay.date) {
                        timeStart = if (size == 0) schedule.timeStart else timeStart
                        timeEnd = schedule.timeEnd
                        size++
                    }
                }

                val timePeriod = "$timeStart - $timeEnd"
                val amount = when (size) {
                    1 -> "$size пара"
                    2, 3, 4 -> "$size пары"
                    else -> "$size пар"
                }

                tvDate.text = this@ScheduleHolder.scheduleDay.date
                tvTimeStartEnd.text = timePeriod
                tvAmount.text = amount
            }
        }

        override fun onClick(p0: View?) {
            Log.i(TAG, "Selected schedule (date: ${scheduleDay.date})")
            callbacks?.onScheduleDaySelect(scheduleDay.date)
        }
    }

    // The object to calculate the difference on list change
    private object DiffCallBack : DiffUtil.ItemCallback<Schedule>() {
        override fun areItemsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
            return oldItem == newItem
        }
    }

    // The Adapter
    private inner class ScheduleAdapter
        : ListAdapter<Schedule, ScheduleHolder>(DiffCallBack) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleHolder {
            val view = layoutInflater.inflate(R.layout.schedule_list_item, parent, false)

            return ScheduleHolder(view)
        }

        override fun onBindViewHolder(holder: ScheduleHolder, position: Int) {
            val schedule = currentList[position]
            holder.bind(schedule)
        }

        override fun getItemCount(): Int = currentList.size
    }
}