package edu.vt.cs5254.dreamcatcher.database

import androidx.room.TypeConverters
import java.util.Date

class DreamTypeConverters {
    @TypeConverters
    fun getDateFromLong(millis: Long): Date{
        return Date(millis)
    }

    @TypeConverters
    fun getLongFromDate(date: Date): Long{
        return date.time
    }
}