package edu.muiv.univapp.ui.schedule

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
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

    // TODO: implement etNotes
    private lateinit var tvSubjectName: TextView
    private lateinit var tvSubjectType: TextView
    private lateinit var tvDate       : TextView
    private lateinit var tvTeacherName: TextView
    private lateinit var tvRoom       : TextView
    private lateinit var tvAttendance : TextView
    private lateinit var etNotes      : EditText

    private val scheduleDetailListVM: ScheduleDetailListVM by lazy {
        ViewModelProvider(this)[ScheduleDetailListVM::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args: ScheduleFragmentArgs by navArgs()

        scheduleDetailListVM.scheduleID = UUID.fromString(args.scheduleId)
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

        tvAttendance.setOnClickListener {
            showDialog()
        }

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
        val formatOut = SimpleDateFormat("dd MMMM, EEEE", Locale.getDefault())
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

        scheduleDetailListVM.scheduleAttendanceLiveData.observe(viewLifecycleOwner) {
            scheduleDetailListVM.scheduleAttendance = it
            val willAttend = it?.willAttend ?: false

            tvAttendance.text = if (willAttend) "+" else ("Н")
        }
    }

    private fun showDialog() {
        val dialog = Dialog(requireActivity()).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_attendance)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        val dialogBtn = dialog.findViewById<Button>(R.id.btnDialogYes)
        dialogBtn.setOnClickListener {
            val attendance = if (scheduleDetailListVM.scheduleAttendance != null) {
                with(scheduleDetailListVM.scheduleAttendance!!) {
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
                    scheduleDetailListVM.scheduleID!!,
                    UserDataHolder.get().user.id,
                    true
                )
            }

            scheduleDetailListVM.upsertAttendance(attendance)
            dialog.dismiss()
        }

        dialog.show()
    }
}
