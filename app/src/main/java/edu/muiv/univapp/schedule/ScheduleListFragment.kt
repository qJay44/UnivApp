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
import edu.muiv.univapp.user.User
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
    private lateinit var user: User
    private var callbacks: Callbacks? = null
    private var adapter: ScheduleAdapter? = ScheduleAdapter()
    private var scheduleAll: List<Schedule> = listOf()

    private val scheduleListViewModel: ScheduleListViewModel by lazy {
        ViewModelProvider(this)[ScheduleListViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ADD_TEST_DATA) {
            for (i in 0..3) {
                val date = "1${i}.11"
                val randomScheduleList = ScheduleTestDataBuilder.createScheduleDay(date)

                for (schedule in randomScheduleList) {
                    scheduleListViewModel.addSchedule(schedule)
                }
            }
        }

        unpackUserBundle(arguments)
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

        scheduleListViewModel.scheduleByDayListLiveData.observe(viewLifecycleOwner) { schedules ->
            schedules?.let {
                Log.d(TAG, "Got ${schedules.size} schedules")
                updateUI(schedules)
            }
        }

        Toast.makeText(requireContext(), "${user.id}", Toast.LENGTH_SHORT).show()
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    private fun updateUI(schedules: List<Schedule>) {
        adapter?.submitList(schedules)
    }

    private fun unpackUserBundle(args: Bundle?) {
        args?.getString("login")?.let { Log.w(TAG, it) }

        @Suppress("DEPRECATION")
        val unpackedId: UUID =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                args?.getSerializable("id", UUID::class.java)!!
            else
                args?.getSerializable("id") as UUID

        user = User(
            id = unpackedId,
            login    = arguments?.getString("login")!!,
            password = arguments?.getString("password")!!,
            group    = arguments?.getString("group")!!,
            name     = arguments?.getString("name")!!,
            surname  = arguments?.getString("surname")!!
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

            val scheduleWholeDay = object {
                private val timeStart = this@ScheduleHolder.scheduleDay.timeStart
                private var timeEnd = "66:66"
                var timePeriod = "99:99"
                var amount = "99"

                init {
                    var size = 0
                    for (schedule in scheduleAll) {
                        if (schedule.date == this@ScheduleHolder.scheduleDay.date) {
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