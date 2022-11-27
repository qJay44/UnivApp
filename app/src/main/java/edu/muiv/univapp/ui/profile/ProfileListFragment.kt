package edu.muiv.univapp.ui.profile

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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.muiv.univapp.R
import edu.muiv.univapp.databinding.FragmentProfileListBinding
import edu.muiv.univapp.user.Subject

class ProfileListFragment : Fragment() {

    companion object {
        private const val TAG = "ProfileFragmentList"
    }

    private var _binding: FragmentProfileListBinding? = null
    private val binding get() = _binding!!
    private val profileListViewModel by lazy {
        ViewModelProvider(this)[ProfileListViewModel::class.java]
    }

    private lateinit var rvSubject: RecyclerView
    private lateinit var tvAttendancePercent: TextView
    private lateinit var tvAttendanceAmount: TextView
    private lateinit var adapter: SubjectAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        adapter = SubjectAdapter()
        _binding = FragmentProfileListBinding.inflate(inflater, container, false)
        val view = binding.root

        tvAttendancePercent = view.findViewById(R.id.tvAttendancePercent)
        tvAttendanceAmount = view.findViewById(R.id.tvAttendanceAmount)

        rvSubject = binding.rvSubjects
        rvSubject.layoutManager = LinearLayoutManager(context)
        rvSubject.adapter = adapter

        profileListViewModel.loadSubjects()
        profileListViewModel.loadProfileAttendance()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileListViewModel.subjects.observe(viewLifecycleOwner) { subjects ->
            Log.i(TAG, "Got ${subjects.size} subjects")
            profileListViewModel.loadSubjectTeachers(subjects)
            updateUI(subjects)
        }

        profileListViewModel.profileAttendance.observe(viewLifecycleOwner) { userProfile ->
            profileListViewModel.scheduleAllSize = userProfile.size
            for (scheduleVisit in userProfile) {
                if (scheduleVisit.visited) profileListViewModel.visitAmount++
            }

            val attendancePercent = profileListViewModel.attendancePercent
            val attendancePercentString = resources.getString(R.string.user_attendance_percent)
            val attendancePercentText = attendancePercentString + getSpannableString(attendancePercent)

            val attendanceAmount = profileListViewModel.attendanceAmount
            val attendanceAmountString = resources.getString(R.string.user_attendance_amount)
            val attendanceAmountText = attendanceAmountString + getSpannableString(attendanceAmount)

            tvAttendancePercent.text = attendancePercentText
            tvAttendanceAmount.text = attendanceAmountText
        }
    }

    override fun onDetach() {
        super.onDetach()
        profileListViewModel.scheduleAllSize = 0
        profileListViewModel.visitAmount = 0
    }

    private fun updateUI(subjects: List<Subject>) {
        adapter.submitList(subjects)
    }

    private fun getSpannableString(text: String): SpannableString {
        val spannableString = SpannableString(text)
        val foregroundSpan = ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.muiv_primary))
        spannableString.setSpan(foregroundSpan, 0, spannableString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        return spannableString
    }

    private inner class SubjectAdapter
        : ListAdapter<Subject, SubjectHolder>(DiffCallback) {

        init {
            profileListViewModel.teachers.observe(viewLifecycleOwner) { teachers ->
                profileListViewModel.teachersById.value = teachers.associateBy { it.id }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectHolder {
            val view = layoutInflater.inflate(R.layout.profile_list_item, parent, false)

            return SubjectHolder(view)
        }

        override fun onBindViewHolder(holder: SubjectHolder, position: Int) {
            val subject = currentList[position]
            holder.bind(subject)
        }
    }

    private inner class SubjectHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvSubjectName: TextView = itemView.findViewById(R.id.tvSubjectName)
        private val tvSubjectDetails: TextView = itemView.findViewById(R.id.tvSubjectDetails)

        fun bind(subject: Subject) {
            profileListViewModel.teachersById.observe(viewLifecycleOwner) {
                val teacher = it[subject.teacherID]
                val teacherField = "${teacher?.surname} ${teacher?.name?.get(0)}. ${teacher?.patronymic?.get(0)}."
                val detailsField = "$teacherField | ${subject.examType}"

                tvSubjectDetails.text = detailsField
            }
            tvSubjectName.text = subject.subjectName
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Subject>() {
        override fun areItemsTheSame(oldItem: Subject, newItem: Subject): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Subject, newItem: Subject): Boolean {
            return oldItem == newItem
        }
    }
}