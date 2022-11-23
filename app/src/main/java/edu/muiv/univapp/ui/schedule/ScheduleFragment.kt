package edu.muiv.univapp.ui.schedule

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import edu.muiv.univapp.databinding.FragmentScheduleBinding
import java.text.SimpleDateFormat
import java.util.*

class ScheduleFragment : Fragment() {

    companion object {
        private const val TAG = "ScheduleFragment"
    }

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!

    // TODO: implement ibAttendance, etNotes
    private lateinit var tvSubjectName: TextView
    private lateinit var tvSubjectType: TextView
    private lateinit var tvDate       : TextView
    private lateinit var tvTeacherName: TextView
    private lateinit var tvRoom       : TextView
    private lateinit var ibAttendance : ImageButton
    private lateinit var etNotes      : EditText

    private val scheduleDetailListVM: ScheduleDetailListVM by lazy {
        ViewModelProvider(this)[ScheduleDetailListVM::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args: ScheduleFragmentArgs by navArgs()
        val scheduleDate = args.scheduleDate

        scheduleDetailListVM.loadSchedule(scheduleDate)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        val root: View = binding.root

        tvSubjectName = binding.tvSubjectName
        tvSubjectType = binding.tvSubjectType
        tvDate = binding.tvDate
        tvTeacherName = binding.tvTeacherName
        tvRoom = binding.tvRoom
        ibAttendance = binding.ibAttendance
        etNotes = binding.etNotes

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scheduleDetailListVM.scheduleLiveData.observe(viewLifecycleOwner) { schedule ->
            schedule?.let {
                Log.i(TAG, "Updating UI...")
                updateUI(schedule)
                scheduleDetailListVM.loadTeacher(schedule.teacherID)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUI(schedule: Schedule) {
        val formatIn = SimpleDateFormat("dd.MM", Locale.getDefault())
        val date = formatIn.parse(schedule.date)!!
        val formatOut = SimpleDateFormat("dd MMM, EE", Locale.getDefault())
        val formattedDate = formatOut.format(date)

        val dateField = "$formattedDate ${schedule.timeStart} - ${schedule.timeEnd}"
        val roomField = "Аудитория ${schedule.roomNum}"

        tvSubjectName.text = schedule.subjectName
        tvDate.text = dateField
        tvRoom.text = roomField

        scheduleDetailListVM.teacherLiveData.observe(viewLifecycleOwner) {
            val teacher = it[0]
            val nameField = "${teacher.surname} ${teacher.name} ${teacher.patronymic}"
            tvTeacherName.text = nameField
        }
    }
}