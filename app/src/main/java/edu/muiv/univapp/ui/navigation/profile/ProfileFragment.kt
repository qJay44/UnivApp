package edu.muiv.univapp.ui.navigation.profile

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.muiv.univapp.R
import edu.muiv.univapp.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    companion object {
        private const val TAG = "ProfileFragmentList"
    }

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val profileViewModel by lazy {
        ViewModelProvider(this)[ProfileViewModel::class.java]
    }

    private lateinit var rvSubject: RecyclerView
    private lateinit var tvAttendancePercent: TextView
    private lateinit var tvAttendanceAmount: TextView
    private var adapter: DisciplineAdapter = DisciplineAdapter(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            profileViewModel.loadSubjects()
            profileViewModel.loadProfileAttendance()
        } else {
            profileViewModel.resetVisitAmount()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        tvAttendancePercent = view.findViewById(R.id.tvAttendancePercent)
        tvAttendanceAmount = view.findViewById(R.id.tvAttendanceAmount)

        rvSubject = binding.rvSubjects
        rvSubject.layoutManager = LinearLayoutManager(context)
        rvSubject.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileViewModel.subjectAndTeacherList.observe(viewLifecycleOwner) { subjectAndTeacherList ->
            Log.i(TAG, "Got ${subjectAndTeacherList.size} subjects")
            updateUI(subjectAndTeacherList)
        }

        profileViewModel.profileAttendance.observe(viewLifecycleOwner) { userProfile ->
            profileViewModel.loadProfileProperties(userProfile)

            val attendancePercent = profileViewModel.attendancePercent
            val attendancePercentBaseString = resources.getString(R.string.user_attendance_percent)
            val attendancePercentText =
                createSpannableString(attendancePercentBaseString + attendancePercent)

            val attendanceAmount = profileViewModel.attendanceAmount
            val attendanceAmountBaseString = resources.getString(R.string.user_attendance_amount)
            val attendanceAmountText =
                createSpannableString(attendanceAmountBaseString + attendanceAmount)

            tvAttendancePercent.text = attendancePercentText
            tvAttendanceAmount.text = attendanceAmountText
        }
        postponeEnterTransition()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUI(subjectAndTeacherList: List<SubjectAndTeacher>) {
        adapter = DisciplineAdapter(subjectAndTeacherList)
        rvSubject.adapter = adapter
    }

    private fun createSpannableString(text: String): SpannableString {
        val spannableString = SpannableString(text)
        val startIndex = text.indexOf(":") + 2
        val endIndex = spannableString.length
        val foregroundSpan = ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.primary))

        spannableString.setSpan(foregroundSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        return spannableString
    }

    private inner class DisciplineAdapter(private val subjectAndTeacherList: List<SubjectAndTeacher>)
        : RecyclerView.Adapter<DisciplineHolder>() {

        init {
            startPostponedEnterTransition()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DisciplineHolder {
            val view = layoutInflater.inflate(R.layout.profile_list_item, parent, false)

            return DisciplineHolder(view)
        }

        override fun onBindViewHolder(holder: DisciplineHolder, position: Int) {
            val subjectAndTeacher = subjectAndTeacherList[position]
            holder.bind(subjectAndTeacher)
        }

        override fun getItemCount(): Int = subjectAndTeacherList.size
    }

    private inner class DisciplineHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvSubjectName: TextView = itemView.findViewById(R.id.tvSubjectName)
        private val tvSubjectDetails: TextView = itemView.findViewById(R.id.tvSubjectDetails)

        fun bind(subjectAndTeacher: SubjectAndTeacher) {
            val teacherField =
                "${subjectAndTeacher.teacherSurname} " +
                "${subjectAndTeacher.teacherName[0]}. " +
                "${subjectAndTeacher.teacherPatronymic[0]}."

            val detailsField = "$teacherField | ${subjectAndTeacher.subjectExamType}"

            tvSubjectDetails.text = detailsField
            tvSubjectName.text = subjectAndTeacher.subjectName
        }
    }
}
