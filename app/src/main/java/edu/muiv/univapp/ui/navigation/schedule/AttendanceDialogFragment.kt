package edu.muiv.univapp.ui.navigation.schedule

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.muiv.univapp.R
import edu.muiv.univapp.databinding.DialogStudentsBinding
import edu.muiv.univapp.user.Student
import java.util.UUID

class AttendanceDialogFragment : DialogFragment() {

    companion object {
        private const val SCHEDULE_ID = "schedule_id"

        fun newInstance(scheduleID: UUID): AttendanceDialogFragment {
            val args = Bundle().apply {
                putSerializable(SCHEDULE_ID, scheduleID)
            }
            return AttendanceDialogFragment().apply {
                arguments = args
            }
        }
    }

    private var _binding: DialogStudentsBinding? = null
    private val binding get() = _binding!!
    private val adapter = StudentsWillAttendAdapter()
    private val rvStudentsWillAttend by lazy { binding.rvStudentsWillAttend }
    private val attendanceViewModel by lazy {
        ViewModelProvider(this)[AttendanceViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogStudentsBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        rvStudentsWillAttend.layoutManager = LinearLayoutManager(context)
        rvStudentsWillAttend.adapter = adapter

        @Suppress("DEPRECATION")
        val scheduleID = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getSerializable(SCHEDULE_ID, UUID::class.java)!!
        } else {
            requireArguments().getSerializable(SCHEDULE_ID) as UUID
        }
        attendanceViewModel.loadStudents(scheduleID)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        attendanceViewModel.studentsWillAttend.observe(viewLifecycleOwner) { students ->
            updateUI(students)
        }
    }

    override fun onStart() {
        super.onStart()
        val decorView = dialog?.window?.decorView
        val appear = ObjectAnimator.ofPropertyValuesHolder(decorView,
            PropertyValuesHolder.ofFloat("alpha", 0f, 1f))
        appear.duration = 300
        appear.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUI(studentsList: List<Student>) {
        adapter.submitList(studentsList)
    }

    private inner class StudentsWillAttendAdapter
        : ListAdapter<Student, StudentsWillAttendHolder>(DiffCallback) {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): StudentsWillAttendHolder {
            val view = layoutInflater.inflate(R.layout.students_will_attend_list_item, parent, false)

            return StudentsWillAttendHolder(view)
        }

        override fun onBindViewHolder(holder: StudentsWillAttendHolder, position: Int) {
            val student = currentList[position]
            holder.bind(student)
        }
    }

    private inner class StudentsWillAttendHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvStudentName: TextView = itemView.findViewById(R.id.tvStudentName)
        private val tvStudentGroup: TextView = itemView.findViewById(R.id.tvStudentGroup)

        fun bind(student: Student) {
            val studentName = "${student.name}\n${student.surname}"
            tvStudentName.text = studentName
            tvStudentGroup.text = student.groupName
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Student>() {
        override fun areItemsTheSame(oldItem: Student, newItem: Student): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Student, newItem: Student): Boolean {
            return oldItem == newItem
        }
    }
}
