package edu.muiv.univapp.api

import com.google.gson.annotations.SerializedName
import edu.muiv.univapp.model.Student

class StudentsResponse {
    @SerializedName("students")
    lateinit var students: List<Student>
}
