package edu.muiv.univapp.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import edu.muiv.univapp.ui.navigation.notifications.Notification
import edu.muiv.univapp.ui.navigation.profile.ProfileAttendance
import edu.muiv.univapp.ui.navigation.schedule.model.Schedule
import edu.muiv.univapp.ui.navigation.schedule.model.ScheduleAttendance
import edu.muiv.univapp.ui.navigation.schedule.model.ScheduleUserNotes
import edu.muiv.univapp.model.Student
import edu.muiv.univapp.model.Subject
import edu.muiv.univapp.model.Teacher

@Database(
    entities = [
        Schedule::class,
        Student::class,
        Teacher::class,
        ScheduleAttendance::class,
        ScheduleUserNotes::class,
        Notification::class,
        ProfileAttendance::class,
        Subject::class
   ],
    version = 1,
    exportSchema = false
)
@TypeConverters(UnivTypeConverters::class)
abstract class UnivDatabase : RoomDatabase() {
    abstract fun univDAO(): UnivDAO
}