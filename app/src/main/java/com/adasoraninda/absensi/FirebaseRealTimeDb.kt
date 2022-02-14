package com.adasoraninda.absensi

import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase

class FirebaseRealTimeDb private constructor() {

    private val database = FirebaseDatabase.getInstance("https://absensi-aeacd-default-rtdb.asia-southeast1.firebasedatabase.app")
    private val databaseRef = database.getReference(DB_REF_NAME)

    fun sendUserToDatabase(user: User): Task<Void> {
        return databaseRef.setValue(user)
    }

    companion object {
        private const val DB_REF_NAME = "log_attendance"

        private var INSTANCE: FirebaseRealTimeDb? = null
        private val LOCK = Any()

        fun getInstance(): FirebaseRealTimeDb {
            if (INSTANCE == null) {
                synchronized(LOCK) {
                    INSTANCE = FirebaseRealTimeDb()
                }
            }

            return INSTANCE!!
        }
    }

}