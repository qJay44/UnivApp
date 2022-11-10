package edu.muiv.univapp.schedule

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.muiv.univapp.R
import edu.muiv.univapp.user.DatabaseTestDataBuilder
import edu.muiv.univapp.login.LoginResult
import java.util.*

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
    private lateinit var user: LoginResult
    private var callbacks: Callbacks? = null
    private var adapter: ScheduleAdapter? = ScheduleAdapter()
    private var scheduleAll: List<Schedule> = listOf()

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

        unpackUserBundle()

        user.groupName?.let { scheduleListViewModel.loadSchedule(it) }

        val welcomeText = "Welcome ${user.name} ${user.surname}"
        Toast.makeText(requireContext(), welcomeText, Toast.LENGTH_SHORT).show()
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
        scheduleListViewModel.scheduleListLiveData.observe(viewLifecycleOwner) { schedules ->
            schedules?.let {
                Log.d(TAG, "Got ${schedules.size} schedules")
                scheduleAll = schedules
            }
        }

        val view = inflater.inflate(R.layout.fragment_schedule_list, container, false)

        rvSchedule = view.findViewById(R.id.schedule_recycler_view)
        rvSchedule.layoutManager = LinearLayoutManager(context)
        rvSchedule.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (user.groupName != null) {
            scheduleListViewModel.scheduleByDayListLiveData.observe(viewLifecycleOwner) { schedules ->
                schedules?.let {
                    updateUI(schedules)
                }
            }
        } else {
            Log.i(TAG, "Got submitting teacher schedules")
            scheduleListViewModel.teacherWithSchedulesLiveData.observe(viewLifecycleOwner) {
                it?.let {
                    updateUI(it.schedules)
                    Log.i(TAG, "Got ${it.schedules.size}schedules")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.title = "${user.name} ${user.surname}, ${user.groupName ?: ""}"
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

    private fun unpackUserBundle() {

        @Suppress("DEPRECATION")
        val unpackedId: UUID =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                arguments?.getSerializable("id", UUID::class.java)!!
            else
                arguments?.getSerializable("id") as UUID

        val groupName: String? = arguments?.getString("groupName")

        user = if (groupName != null) {
            LoginResult(
                unpackedId,
                arguments?.getString("name")!!,
                arguments?.getString("surname")!!,
                groupName,
            )
        } else {
            LoginResult(
                unpackedId,
                arguments?.getString("name")!!,
                arguments?.getString("surname")!!,
                null
            )
        }

        Toast.makeText(requireContext(), user.name, Toast.LENGTH_SHORT).show()
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

            val scheduleWholeDay = object {
                private var timeStart = ""
                private var timeEnd = ""
                var timePeriod = "00:00 - 23:59"
                var amount = "99"

                init {
                    var size = 0
                    for (schedule in scheduleAll) {
                        if (schedule.date == this@ScheduleHolder.scheduleDay.date) {
                            timeStart = if (size == 0) schedule.timeStart else timeStart
                            timeEnd = schedule.timeEnd
                            size++
                        }
                    }

                    timePeriod = "$timeStart - $timeEnd"
                    amount = when (size) {
                        1 -> "$size пара"
                        2, 3, 4 -> "$size пары"
                        else -> "$size пар"
                    }
                }
            }

            tvDate.text = this.scheduleDay.date
            tvTimeStartEnd.text = scheduleWholeDay.timePeriod
            tvAmount.text = scheduleWholeDay.amount
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