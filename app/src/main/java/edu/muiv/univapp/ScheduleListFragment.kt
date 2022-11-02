package edu.muiv.univapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess

class ScheduleListFragment : Fragment() {

    companion object {
        private const val TAG = "ScheduleListFragment"

        fun newInstance(): ScheduleListFragment = ScheduleListFragment()
    }

    private val scheduleListViewModel: ScheduleListViewModel by lazy {
        ViewModelProvider(this)[ScheduleListViewModel::class.java]
    }

    private lateinit var scheduleRecyclerView: RecyclerView
    private var adapter: ScheduleAdapter? = ScheduleAdapter()

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        for (i in 0..10) {
//            val schedule = Schedule(
//                timeStart = "0$i:3$i",
//                timeEnd = "1$i:4$i",
//                subjectName = "Test$i",
//                roomNum = i + 100
//            )
//
//            scheduleListViewModel.addSchedule(schedule)
//        }
//    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_schedule_list, container, false)

        scheduleRecyclerView = view.findViewById(R.id.schedule_recycler_view)
        scheduleRecyclerView.layoutManager = LinearLayoutManager(context)
        scheduleRecyclerView.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scheduleListViewModel.scheduleByDayListLiveData.observe(viewLifecycleOwner) { schedules ->
            schedules?.let {
                Log.i(TAG, "Got ${schedules.size} schedules")
                updateUI(schedules)
            }
        }
    }

    private fun updateUI(schedules: List<Schedule>) {
        adapter?.submitList(schedules)
    }

    // View Holder
    private inner class ScheduleHolder(view: View)
        : RecyclerView.ViewHolder(view), View.OnClickListener {

            private lateinit var schedule: Schedule

            private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
            private val tvTimeStartEnd: TextView = itemView.findViewById(R.id.tvTimeStartEnd)
            private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(schedule: Schedule) {
            this.schedule = schedule

            val df = SimpleDateFormat("dd.MM", Locale.ENGLISH)
            val scheduleDay = scheduleListViewModel.getScheduleByDate(this.schedule.date)
            // FIXME: "unknown method" ?

            val timeStart = scheduleDay[0].timeStart
            val timeEnd = scheduleDay[scheduleDay.size - 1].timeEnd
            val timePeriod = "$timeStart - $timeEnd"
            val subjectsAmount = scheduleDay.size.toString()
            Log.i(TAG, "Amount: $subjectsAmount")

            tvDate.text = df.format(this.schedule.date)
            tvTimeStartEnd.text = timePeriod
            tvAmount.text = subjectsAmount
        }

        override fun onClick(p0: View?) {
            Log.i(TAG, "Clicked item ${schedule.id}")
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