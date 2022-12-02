package edu.muiv.univapp.ui.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import edu.muiv.univapp.R

class LoginFragmentChoice : Fragment() {

    companion object {
        fun newInstance(): LoginFragmentChoice = LoginFragmentChoice()
    }

    interface Callbacks {
        fun onLoginChoice(isTeacher: Boolean)
    }

    private lateinit var btnAsStudent: Button
    private lateinit var btnAsTeacher: Button
    private var callbacks: Callbacks? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login_choice, container, false)

        btnAsStudent = view.findViewById(R.id.btnAsStudent)
        btnAsTeacher = view.findViewById(R.id.btnAsTeacher)

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnAsStudent.setOnClickListener {
            callbacks?.onLoginChoice(false)
        }

        btnAsTeacher.setOnClickListener {
            callbacks?.onLoginChoice(true)
        }
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }
}