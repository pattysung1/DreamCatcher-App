package edu.vt.cs5254.dreamcatcher.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import edu.vt.cs5254.dreamcatcher.Dream
import edu.vt.cs5254.dreamcatcher.DreamEntry
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface DreamDao {

    // Chapter 12: DreamListFragment:
    @Query("SELECT * FROM dream d JOIN dream_entry e ON e.dreamId = d.id ORDER BY d.lastUpdated DESC")
    fun getDream(): Flow<Map<Dream, List<DreamEntry>>> //"multimap"

    // Chapter 13: DreamDetailFragment:

    @Query("SELECT * FROM dream WHERE id=(:id)")
    suspend fun internalGetDream(id: UUID): Dream

    @Query("SELECT * FROM dream_entry WHERE dreamID=(:dreamId)")
    suspend fun internalGetEntriesForDream(dreamId: UUID): List<DreamEntry>

    @Transaction
    suspend fun getDreamAndEntries(id: UUID): Dream {
        return internalGetDream(id).apply { entries = internalGetEntriesForDream(id) }

    }
}