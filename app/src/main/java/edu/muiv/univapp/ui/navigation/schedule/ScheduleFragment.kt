package edu.muiv.univapp.ui.navigation.schedule

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
import android.view.WindowManager.LayoutParams
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import edu.muiv.univapp.R
import edu.muiv.univapp.databinding.FragmentScheduleBinding
import edu.muiv.univapp.ui.navigation.schedule.model.Schedule
import edu.muiv.univapp.ui.navigation.schedule.model.ScheduleAttendance
import edu.muiv.univapp.ui.navigation.schedule.model.ScheduleUserNotes
import edu.muiv.univapp.utils.UserDataHolder
import java.text.SimpleDateFormat
import java.util.*

class ScheduleFragment : Fragment() {

    companion object {
        private const val TAG = "ScheduleFragment"
    }

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    private var notesText: String? = null

    private lateinit var tvSubjectName          : TextView
    private lateinit var tvSubjectType          : TextView
    private lateinit var tvDate                 : TextView
    private lateinit var tvTeacherName          : TextView
    private lateinit var tvRoom                 : TextView
    private lateinit var tvBtnAttendance        : TextView
    private lateinit var etNotes                : EditText
    private lateinit var svScroll               : ScrollView
    private lateinit var llTeacherNotesContainer: LinearLayout

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
        tvBtnAttendance = binding.btnAttendance
        etNotes = binding.etNotes
        svScroll = binding.svScroll
        llTeacherNotesContainer = binding.llTeacherNotesContainer

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Update schedule info
        scheduleViewModel.scheduleLiveData.observe(viewLifecycleOwner) { schedule ->
            schedule?.let {
                Log.i(TAG, schedule.toString())
                scheduleViewModel.schedule = schedule
                updateUI(schedule)
            }
        }

        // Update teacher's name
        scheduleViewModel.teacherLiveData.observe(viewLifecycleOwner) {
            val teacher = it[0]
            val nameField = "${teacher.surname} ${teacher.name} ${teacher.patronymic}"
            tvTeacherName.text = nameField
        }

        // Update attendance status
        if (scheduleViewModel.isTeacher) {
            // For teacher
            scheduleViewModel.studentsWillAttend.observe(viewLifecycleOwner) { studentsWillAttend ->
                tvBtnAttendance.text = studentsWillAttend.size.toString()
            }
            // Show student(s) that will attend
            tvBtnAttendance.setOnClickListener {
                if (!scheduleViewModel.studentsWillAttend.value.isNullOrEmpty()) {
                    val dialogFragment =
                        AttendanceDialogFragment.newInstance(scheduleViewModel.scheduleID!!)
                    dialogFragment.show(parentFragmentManager, null)
                }
            }
        } else {
            // For student
            scheduleViewModel.scheduleAttendanceLiveData.observe(viewLifecycleOwner) {
                scheduleViewModel.scheduleAttendance = it
                val willAttend = it?.willAttend ?: false
                tvBtnAttendance.text = if (willAttend) {
                    tvBtnAttendance.textColor(R.color.primary)
                    tvBtnAttendance.backgroundDrawable(R.drawable.attendance_button_yes)
                    ""
                } else {
                    tvBtnAttendance.textColor(android.R.color.white)
                    tvBtnAttendance.backgroundDrawable(R.drawable.attendance_button_no)
                    "Н"
                }
            }

            // Show dialog with choose options if current time respects restrictions
            tvBtnAttendance.setOnClickListener { btn ->
                val textTemplate = "Кнопка не доступна"
                val restrictionText =
                    when (scheduleViewModel.isAllowedToCheckAttendance) {
                        "Late" -> "$textTemplate (слишком поздно)"
                        "Allowed" -> {
                            val btnAnimation = AnimationUtils.loadAnimation(
                                requireContext(),
                                R.anim.attendance_button_animation
                            )
                            btn.startAnimation(btnAnimation)
                            showDialog()
                            null
                        }
                        "Early" -> "$textTemplate (слишком рано)"
                        else -> throw IllegalStateException("tvBtnAttendance: Got unexpected value")
                    }

                if (restrictionText != null) {
                    Toast.makeText(requireContext(), restrictionText, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Update notes
        scheduleViewModel.scheduleUserNotesLiveData.observe(viewLifecycleOwner) {
            scheduleViewModel.scheduleUserNotes = it
            etNotes.setText(it?.notes)
        }

        // Update subject name
        scheduleViewModel.subject.observe(viewLifecycleOwner) { subject ->
            tvSubjectName.text = subject.subjectName
        }

        val notesTextWatcher = object : TextWatcher {
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notesText = p0.toString()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}
        }

        etNotes.addTextChangedListener(notesTextWatcher)

        // Wait for animations availability
        postponeEnterTransition()
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
        if (scheduleViewModel.isTeacher && notesText != null)
            scheduleViewModel.schedule!!.teacherNotes = notesText as String

        scheduleViewModel.upsertScheduleUserNotes(notes)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun TextView.textColor(@ColorRes id: Int) {
        this.setTextColor(ContextCompat.getColor(requireContext(), id))
    }

    private fun TextView.backgroundDrawable(@DrawableRes id: Int) {
        this.background = ContextCompat.getDrawable(requireContext(), id)
    }

    private fun TextView.leftDrawable(@DrawableRes id: Int, @DimenRes paddingRes: Int) {
        val drawable = ContextCompat.getDrawable(requireContext(), id)
        val padding = resources.getDimensionPixelSize(paddingRes)

        this.compoundDrawablePadding = padding
        this.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
    }

    private fun TextView.font(@FontRes id: Int) {
        val typeface = ResourcesCompat.getFont(requireContext(), id)
        this.typeface = typeface
    }

    private fun updateUI(schedule: Schedule) {
        val formatIn = SimpleDateFormat("dd.MM.yyyy", Locale.forLanguageTag("ru"))
        val date = formatIn.parse(schedule.date)!!
        val formatOut = SimpleDateFormat("dd MMMM, EEEE", Locale.forLanguageTag("ru"))
        val formattedDate = formatOut.format(date)

        val dateField = "$formattedDate\n${schedule.timeStart} - ${schedule.timeEnd}"
        val roomField = "Аудитория ${schedule.roomNum}"

        tvSubjectType.text = schedule.type.uppercase(Locale.ROOT)
        tvDate.text = dateField
        tvRoom.text = roomField

        if (scheduleViewModel.isTeacher) etNotes.setText(schedule.teacherNotes)

        // Allow animations to play
        startPostponedEnterTransition()

        val teacherNotes = schedule.teacherNotes.split("\n")
        for (note in teacherNotes) {
            if (note == "") continue
            createBulletTextView(note)
        }
    }

    private fun createBulletTextView(text: String) {
        val tvNote = TextView(requireContext())
        val params = LinearLayout.LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        params.leftMargin = 20
        params.topMargin = 20

        tvNote.text = text
        tvNote.layoutParams = params
        tvNote.leftDrawable(R.drawable.ic_bullet, R.dimen.bullet_padding)
        tvNote.font(R.font.montserrat)

        llTeacherNotesContainer.addView(tvNote)
    }

    private fun showDialog() {
        val dialog = Dialog(requireActivity()).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_attendance)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        val btnLambda: ((Boolean) -> Unit) = { willAttend ->
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
                    willAttend
                )
            }

            scheduleViewModel.upsertAttendance(attendance)
            dialog.dismiss()
        }

        val btnYes = dialog.findViewById<Button>(R.id.btnDialogYes)
        val btnNo = dialog.findViewById<Button>(R.id.btnDialogCancel)

        btnYes.setOnClickListener { btnLambda(true)  }
        btnNo.setOnClickListener  { btnLambda(false) }

        dialog.show()
    }
}
