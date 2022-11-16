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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.muiv.univapp.R
import edu.muiv.univapp.databinding.FragmentScheduleListBinding

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
    private var adapter: ScheduleAdapter? = ScheduleAdapter()
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
//            private val tvTimeStartEnd: TextView = itemView.findViewById(R.id.tvTimeStartEnd)
//            private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(scheduleDay: Schedule) {
            this.scheduleDay = scheduleDay


            // TODO: Recreate recycler item layout
            tvDate.text = this@ScheduleHolder.scheduleDay.date
//            tvTimeStartEnd.text = "timePeriod"
//            tvAmount.text = "amount4"
        }

        override fun onClick(p0: View?) {
            // TODO: Open a schedule day
            Log.i(TAG, "Selected schedule (date: ${scheduleDay.date})")
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