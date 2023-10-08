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
        .createFromAsset()
        .build()

    suspend fun getDreams(): List<Dream>{

    }

    suspend fun getDream(id: UUID){

    }
    companion object{
        private var INSTANCE: DreamRepository? =null

        fun initalize(context: Context){
            check(INSTANCE ==null) {"Cannot initialize DreamRepository more than once!"}
            INSTANCE = DreamRepository(context)
        }

        fun get(): DreamRepository{
            return checkNotNull(INSTANCE){"DreamRepository must be initialized!"}
        }
    }
}