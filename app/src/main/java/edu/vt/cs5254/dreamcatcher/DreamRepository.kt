package edu.vt.cs5254.dreamcatcher

import android.content.Context
import androidx.room.Room
import edu.vt.cs5254.dreamcatcher.database.DreamDatabase
import java.util.UUID


private const val DATABASE_NAME = "dream-database"
class DreamRepository(context: Context) {
    private val database: DreamDatabase = Room.databaseBuilder(
        context,
        DreamDatabase::class.java,
        DATABASE_NAME
    )
        .createFromAsset(DATABASE_NAME)
        .build()

    // Chapter 12: DreamListFragment
    suspend fun getDreams(): List<Dream>{
        val dreamMap = database.dreamDao().getDream() //returns Map<Dream, List<DreamEntry>>
        return dreamMap.keys.map{ dream ->
            dream.apply { entries = dreamMap.getValue(dream) }
        }
    }

    // Chapter 13: DreamDetailFragment
    suspend fun getDream(id: UUID): Dream{
        return database.dreamDao().getDreamAndEntries(id)
    }

    companion object{
        private var INSTANCE: DreamRepository? =null

        fun initialize(context: Context){
            check(INSTANCE ==null) {"Cannot initialize DreamRepository more than once!"}
            INSTANCE = DreamRepository(context)
        }

        fun get(): DreamRepository{
            return checkNotNull(INSTANCE){"DreamRepository must be initialized!"}
        }
    }
}