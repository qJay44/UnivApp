package edu.muiv.univapp.ui.schedule

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
import edu.muiv.univapp.R
import java.text.SimpleDateFormat
import java.util.*

class ScheduleFragment : Fragment() {

    companion object {

        private const val TAG = "ScheduleFragment"
        private const val ARG_SCHEDULE_DATE = "schedule_date"

        fun newInstance(date: String): ScheduleFragment {
            val args = Bundle().apply {
                putString(ARG_SCHEDULE_DATE, date)
            }

            return ScheduleFragment().apply {
                arguments = args
            }
        }
    }

    private lateinit var rvScheduleDetail: RecyclerView
    private var adapter: ScheduleDetailAdapter? = ScheduleDetailAdapter()

    private val scheduleDetailListVM: ScheduleDetailListVM by lazy {
        ViewModelProvider(this)[ScheduleDetailListVM::class.java]
    }

    // Load schedule and set toolbar text to according date
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scheduleDate = arguments?.getString(ARG_SCHEDULE_DATE)!!
        val formatter = SimpleDateFormat("dd.MM", Locale.ENGLISH)
        val date = formatter.parse(scheduleDate)
        val df = SimpleDateFormat("d MMMM", Locale.forLanguageTag("RU"))

        activity?.title = date?.let { df.format(it) }
        scheduleDetailListVM.loadSchedule(scheduleDate)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_schedule_detail_list, container, false)

        rvScheduleDetail = view.findViewById(R.id.schedule_detail_recycler_view)
        rvScheduleDetail.layoutManager = LinearLayoutManager(context)
        rvScheduleDetail.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scheduleDetailListVM.scheduleLiveData.observe(viewLifecycleOwner) { schedule ->
            schedule?.let {
                updateUI(schedule)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.title = "UnivApp"
        Log.i(TAG, "Destroying fragment")
    }

    private fun updateUI(schedules: List<Schedule>) {
        adapter?.submitList(schedules)
    }

    // View Holder
    private inner class ScheduleDetailHolder(view: View)
        : RecyclerView.ViewHolder(view) {

            private lateinit var schedule: Schedule

            private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
            private val tvSubjectName: TextView = itemView.findViewById(R.id.tvSubjectName)
            private val tvRoomNumber: TextView = itemView.findViewById(R.id.tvRoomNumber)

        fun bind(schedule: Schedule) {
            this.schedule = schedule
            val timeText = "${this.schedule.timeStart} - ${this.schedule.timeEnd}"
            val roomText = "ауд. ${this.schedule.roomNum}"

            tvTime.text = timeText
            tvSubjectName.text = this.schedule.subjectName
            tvRoomNumber.text = roomText
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
    private inner class ScheduleDetailAdapter
        : ListAdapter<Schedule, ScheduleDetailHolder>(DiffCallBack) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleDetailHolder {
            val view = layoutInflater.inflate(R.layout.schedule_detail_list_item, parent, false)

            return ScheduleDetailHolder(view)
        }

        override fun onBindViewHolder(holder: ScheduleDetailHolder, position: Int) {
            val schedule = currentList[position]
            holder.bind(schedule)
        }

        override fun getItemCount(): Int = currentList.size
    }
}