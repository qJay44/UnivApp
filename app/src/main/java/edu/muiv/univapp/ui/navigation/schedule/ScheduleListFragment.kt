package edu.muiv.univapp.ui.navigation.schedule

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.FontRes
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.google.gson.Gson
import edu.muiv.univapp.R
import edu.muiv.univapp.api.StatusCode
import edu.muiv.univapp.databinding.FragmentScheduleListBinding
import edu.muiv.univapp.ui.navigation.schedule.model.ScheduleWithSubjectAndTeacher
import edu.muiv.univapp.ui.navigation.schedule.utils.AsyncCell
import edu.muiv.univapp.ui.navigation.schedule.utils.OnTouchListenerRecyclerView
import edu.muiv.univapp.utils.VisibleFragment
import edu.muiv.univapp.ui.navigation.schedule.utils.WeekChangeAnimationListener
import edu.muiv.univapp.utils.FetchedListType
import edu.muiv.univapp.utils.PollWorker
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ScheduleListFragment : VisibleFragment() {
    companion object {
        private const val TAG = "ScheduleListFragment"
        private const val LAST_SCHEDULE = "lastSchedule"
        private const val POLL_WORK = "pollWorkSchedule"
    }

    private var _binding: FragmentScheduleListBinding? = null
    private val binding get() = _binding!!

    private lateinit var tvWeekDays  : TextView
    private lateinit var rvSchedule  : RecyclerView
    private lateinit var ibPrevWeek  : ImageButton
    private lateinit var ibNextWeek  : ImageButton
    private lateinit var tvNoSchedule: TextView
    private var adapter: ScheduleAdapter? = null

    private val scheduleListViewModel: ScheduleListViewModel by lazy {
        ViewModelProvider(this)[ScheduleListViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            scheduleListViewModel.loadCalendar()
            setPollingWorker()
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

        rvSchedule = binding.scheduleRecyclerView
        rvSchedule.layoutManager = LinearLayoutManager(context)

        WeekChangeAnimationListener.setBindFunc(TAG) { attachAdapter() }

        // Previous week animation
        val ibPrevWeekAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_out)
        ibPrevWeekAnimation.setAnimationListener(
            WeekChangeAnimationListener { scheduleListViewModel.loadPreviousWeek() }
        )

        ibPrevWeek = binding.ibPrevWeek
        ibPrevWeek.setOnClickListener { rvSchedule.startAnimation(ibPrevWeekAnimation) }

        // Next week animation
        val ibNextWeekAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_out)
        ibNextWeekAnimation.setAnimationListener(
            WeekChangeAnimationListener { scheduleListViewModel.loadNextWeek() }
        )

        ibNextWeek = binding.ibNextWeek
        ibNextWeek.setOnClickListener { rvSchedule.startAnimation(ibNextWeekAnimation) }

        tvWeekDays = binding.tvWeekDays
        tvNoSchedule = binding.tvNoSchedule

        // Swipes //

        rvSchedule.setOnTouchListener(object : OnTouchListenerRecyclerView(context, rvSchedule) {
            override fun onSwipeLeft(): Boolean {
                ibNextWeek.performClick()
                return true
            }

            override fun onSwipeRight(): Boolean {
                ibPrevWeek.performClick()
                return true
            }

            override fun onClick(view: View, position: Int): Boolean {
                if (adapter == null) return false

                val schedule = adapter!!.getScheduleByPosition(position)
                val action = ScheduleListFragmentDirections
                    .actionNavigationScheduleListToNavigationSchedule(schedule.id)
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

        // Week first and last days
        scheduleListViewModel.dayFromTo.observe(viewLifecycleOwner) { dayFromToString ->
            tvWeekDays.text = dayFromToString
        }

        // Update schedule adapter
        if (scheduleListViewModel.isTeacher) {
            scheduleListViewModel.teacherSchedulesLiveData.observe(viewLifecycleOwner) { schedules ->
                schedules?.let {
                    updateUI(schedules)
                    // Create a list of queried schedule ids
                    scheduleListViewModel.createScheduleIdList(schedules, FetchedListType.OLD)
                }
            }
        } else {
            scheduleListViewModel.studentSchedule.observe(viewLifecycleOwner) { schedules ->
                schedules?.let {
                    updateUI(schedules)
                    // Create a list of queried schedule ids
                    scheduleListViewModel.createScheduleIdList(schedules, FetchedListType.OLD)
                }
            }
        }

        scheduleListViewModel.fetchedSchedule.observe(viewLifecycleOwner) { response ->
            val statusCode = response.keys.first()
            val scheduleList = response.values.first()

            if (statusCode == StatusCode.OK) {
                Log.i(TAG, "Updating database with fetched schedule")

                // Create a list with ids of fetched schedule
                scheduleListViewModel.createScheduleIdList(
                    scheduleList!!, FetchedListType.NEW
                )

                lifecycleScope.launch(Dispatchers.Default) {
                    val sp = requireContext().getSharedPreferences(LAST_SCHEDULE, Context.MODE_PRIVATE)
                    val editor = sp.edit()
                    val json = Gson().toJson(scheduleList.map { it.id })

                    editor.putString("schedule", json)
                    editor.apply()
                }
            } else {
                val errorMessage = statusCode.message("Schedule")
                Log.w(TAG, errorMessage)
            }
        }

        // Wait for animations availability
        postponeEnterTransition()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        WeekChangeAnimationListener.resetListener()
        _binding = null
    }

    private fun updateUI(scheduleForUserList: List<ScheduleWithSubjectAndTeacher>) {
        Log.i(TAG, "Got ${scheduleForUserList.size} schedules for user")

        // Without this sorting 01.01 (2023) will be earlier than 10.12 (2022)
        val sortedScheduleList = scheduleForUserList.sortedBy {
            val df = SimpleDateFormat("dd.MM.yyyy", Locale.forLanguageTag("ru"))
            df.parse(it.date)!!.time
        }

        adapter = ScheduleAdapter(sortedScheduleList)

        // Attach new adapter only if animation ended and wasn't attached in listener
        with(WeekChangeAnimationListener) {
            isAdapterUpdated = true
            if (isAnimationEnded && !isAdapterAttached) {
                attachAdapter()
            }
        }

        // Allow animations to play
        startPostponedEnterTransition()

        // Post processing
        postUpdateUI(scheduleForUserList.isEmpty())
    }

    private fun postUpdateUI(noSchedules: Boolean) {
        // Show text if no schedule
        tvNoSchedule.visibility = if (noSchedules) View.VISIBLE else View.GONE
    }

    private fun attachAdapter() {
        if (adapter != null) {
            rvSchedule.adapter = adapter
            rvSchedule.scheduleLayoutAnimation()
        } else {
            Log.e(TAG, "attachAdapter: Adapter wasn't initialized")
        }
    }

    private fun setPollingWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .build()
        val periodicRequest = PeriodicWorkRequest
            .Builder(PollWorker::class.java, 15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
            POLL_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )
    }

    // The Adapter
    private inner class ScheduleAdapter(scheduleForUserList: List<ScheduleWithSubjectAndTeacher>
        ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val scheduleList: List<ScheduleWithSubjectAndTeacher>
        private val scheduleHolderTypeOrder: List<HolderViewType>

        init {
            val scheduleList = mutableListOf<ScheduleWithSubjectAndTeacher>()
            val scheduleHolderTypeOrder = mutableListOf<HolderViewType>()

            var currentWeekDay = ""

            /**
             * Create schedule holder as header
             * and then the same schedule as default holder
             * or only default holder
             */
            for (schedule in scheduleForUserList) {
                if (schedule.date != currentWeekDay) {
                    scheduleHolderTypeOrder += HolderViewType.HEADER
                    scheduleList += schedule
                }

                scheduleHolderTypeOrder += HolderViewType.DEFAULT
                scheduleList += schedule
                currentWeekDay = schedule.date
            }

            this.scheduleList = scheduleList.toList()
            this.scheduleHolderTypeOrder = scheduleHolderTypeOrder.toList()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                HolderViewType.HEADER.ordinal -> {
                    ScheduleHeaderHolder(
                        HeaderItemCell(parent.context).apply { inflate() }
                    )
                }
                HolderViewType.DEFAULT.ordinal -> {
                    ScheduleDefaultHolder(
                        DefaultItemCell(parent.context).apply { inflate() }
                    )
                }
                else -> throw IllegalStateException("onCreateViewHolder: Got unexpected view type")
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val scheduleForUser = scheduleList.elementAt(position)
            when (holder) {
                is ScheduleHeaderHolder -> {
                    setupHeaderViewHolder(
                        holder,
                        scheduleListViewModel.getSimpleDate(scheduleForUser.date),
                        scheduleListViewModel.getWeekDayNameByDate(scheduleForUser.date)
                    )
                }
                is ScheduleDefaultHolder -> {
                    setupDefaultViewHolder(holder, scheduleForUser)
                }
                else -> throw IllegalStateException("onBindViewHolder: Got unexpected holder")
            }
        }

        override fun getItemCount(): Int = scheduleList.size

        override fun getItemViewType(position: Int): Int {
            // Which holder to create
            return scheduleHolderTypeOrder.elementAt(position).ordinal
        }

        // Use this function when async layout inflater can't set fonts by itself
        private fun TextView.font(@FontRes id: Int) {
            val typeface = ResourcesCompat.getFont(requireContext(), id)
            this.typeface = typeface
        }

        // Header holder binding
        private fun setupHeaderViewHolder(
            holder: ScheduleHeaderHolder,
            formattedDate: String,
            weekDayName: String
        ) {
            (holder.itemView as HeaderItemCell).bindWhenInflated {
                with (holder.itemView) {
                    val tvDay: TextView = findViewById(R.id.tvDay)
                    val tvWeekDayName: TextView = findViewById(R.id.tvWeekDayName)

                    tvDay.text = formattedDate
                    tvWeekDayName.text = weekDayName
                }
            }
        }

        // Default holder binding
        private fun setupDefaultViewHolder(
            holder: ScheduleDefaultHolder,
            scheduleForUser: ScheduleWithSubjectAndTeacher
        ) {
            (holder.itemView as DefaultItemCell).bindWhenInflated {
                with (holder.itemView) {
                    val tvTimeStart   : TextView = findViewById(R.id.tvTimeStart)
                    val tvTimeEnd     : TextView = findViewById(R.id.tvTimeEnd)
                    val tvSubjectName : TextView = findViewById(R.id.tvSubjectName)
                    val tvScheduleInfo: TextView = findViewById(R.id.tvScheduleInfo)

                    val teacherField =
                        "${scheduleForUser.teacherSurname} " +
                        "${scheduleForUser.teacherName[0]}. " +
                        "${scheduleForUser.teacherPatronymic[0]}."

                    val details =
                        "$teacherField | " +
                        "${scheduleForUser.type} | " +
                        "Ауд. ${scheduleForUser.roomNum}"

                    tvTimeStart.font(R.font.montserrat_medium)
                    tvTimeEnd.font(R.font.montserrat_medium)
                    tvSubjectName.font(R.font.montserrat_semibold)
                    tvScheduleInfo.font(R.font.montserrat)

                    tvTimeStart.text = scheduleForUser.timeStart
                    tvTimeEnd.text = scheduleForUser.timeEnd
                    tvSubjectName.text = scheduleForUser.subjectName
                    tvScheduleInfo.text = details
                }
            }
        }

        // Inflate holders asynchronously //

        private inner class HeaderItemCell(context: Context) : AsyncCell(context) {
            override val layoutId = R.layout.schedule_list_item_header
        }

        private inner class DefaultItemCell(context: Context) : AsyncCell(context) {
            override val layoutId = R.layout.schedule_list_item
        }

        ////////////////////////////////////

        fun getScheduleByPosition(pos: Int) = scheduleList.elementAt(pos)
    }

    private class ScheduleHeaderHolder(view: View) : RecyclerView.ViewHolder(view)
    private class ScheduleDefaultHolder(view: View) : RecyclerView.ViewHolder(view)

    private enum class HolderViewType {
        HEADER,
        DEFAULT
    }
}
