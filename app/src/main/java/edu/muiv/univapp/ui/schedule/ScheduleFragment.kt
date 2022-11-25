package edu.muiv.univapp.ui.schedule

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import edu.muiv.univapp.R
import edu.muiv.univapp.databinding.FragmentScheduleBinding
import edu.muiv.univapp.user.UserDataHolder
import java.text.SimpleDateFormat
import java.util.*

class ScheduleFragment : Fragment() {

    companion object {
        private const val TAG = "ScheduleFragment"
    }

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    private var notesText: String? = null

    private lateinit var tvSubjectName: TextView
    private lateinit var tvSubjectType: TextView
    private lateinit var tvDate       : TextView
    private lateinit var tvTeacherName: TextView
    private lateinit var tvRoom       : TextView
    private lateinit var tvAttendance : TextView
    private lateinit var etNotes      : EditText

    private val scheduleViewModel: ScheduleViewModel by lazy {
        ViewModelProvider(this)[ScheduleViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args: ScheduleFragmentArgs by navArgs()

        scheduleViewModel.scheduleID = UUID.fromString(args.scheduleId)
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
        tvAttendance = binding.tvAttendance
        etNotes = binding.etNotes

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvAttendance.setOnClickListener {
            showDialog()
        }

        // Update schedule info
        scheduleViewModel.scheduleLiveData.observe(viewLifecycleOwner) { schedule ->
            schedule?.let {
                Log.i(TAG, schedule.toString())
                updateUI(schedule)
                scheduleViewModel.loadTeacher(schedule.teacherID)
            }
        }

        // Update teacher's name
        scheduleViewModel.teacherLiveData.observe(viewLifecycleOwner) {
            val teacher = it[0]
            val nameField = "${teacher.surname} ${teacher.name} ${teacher.patronymic}"
            tvTeacherName.text = nameField
        }

        // Update attendance status
        scheduleViewModel.scheduleAttendanceLiveData.observe(viewLifecycleOwner) {
            scheduleViewModel.scheduleAttendance = it
            val willAttend = it?.willAttend ?: false
            tvAttendance.text = if (willAttend) "+" else ("Н")
        }

        // Update notes
        scheduleViewModel.scheduleUserNotesLiveData.observe(viewLifecycleOwner) {
            scheduleViewModel.scheduleUserNotes = it
            etNotes.setText(it?.notes)
        }

        val notesTextWatcher = object : TextWatcher {
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notesText = p0.toString()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}
        }

        etNotes.addTextChangedListener(notesTextWatcher)
    }

    override fun onStop() {
        super.onStop()
        val notes = if (scheduleViewModel.scheduleUserNotes != null) {
            with(scheduleViewModel.scheduleUserNotes!!) {
                val changedNotes = if(notes != notesText) notesText else notes
                ScheduleUserNotes(
                    id,
                    scheduleID,
                    studentID,
                    changedNotes
                )
            }
        } else {
            ScheduleUserNotes(
                UUID.randomUUID(),
                scheduleViewModel.scheduleID!!,
                UserDataHolder.get().user.id,
                notesText
            )
        }

        scheduleViewModel.upsertScheduleUserNotes(notes)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUI(schedule: Schedule) {
        val formatIn = SimpleDateFormat("dd.MM", Locale.getDefault())
        val date = formatIn.parse(schedule.date)!!
        val formatOut = SimpleDateFormat("dd MMMM, EEEE", Locale.getDefault())
        val formattedDate = formatOut.format(date)

        val dateField = "$formattedDate ${schedule.timeStart} - ${schedule.timeEnd}"
        val roomField = "Аудитория ${schedule.roomNum}"

        tvSubjectName.text = schedule.subjectName
        tvDate.text = dateField
        tvRoom.text = roomField
    }

    private fun showDialog() {
        val dialog = Dialog(requireActivity()).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_attendance)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        val dialogBtn = dialog.findViewById<Button>(R.id.btnDialogYes)
        dialogBtn.setOnClickListener {
            val attendance = if (scheduleViewModel.scheduleAttendance != null) {
                with(scheduleViewModel.scheduleAttendance!!) {
                    ScheduleAttendance(
                        id,
                        scheduleID,
                        studentID,
                        willAttend
                    )
                }
            } else {
                ScheduleAttendance(
                    UUID.randomUUID(),
                    scheduleViewModel.scheduleID!!,
                    UserDataHolder.get().user.id,
                    true
                )
            }

            scheduleViewModel.upsertAttendance(attendance)
            dialog.dismiss()
        }

        dialog.show()
    }
}
