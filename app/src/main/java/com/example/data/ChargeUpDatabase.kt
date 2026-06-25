package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "sessions")
data class ChargeUpSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val breathing: String,
    val mentalState: String,
    val desiredOutcome: String,
    val culturalInspiration: String,
    val environmentPreference: String,
    val generatedScriptJson: String
)

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<ChargeUpSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChargeUpSession)

    @Query("DELETE FROM sessions")
    suspend fun clearHistory()
}

@Database(entities = [ChargeUpSession::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chargeup_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class SessionRepository(private val sessionDao: SessionDao) {
    val allSessions: Flow<List<ChargeUpSession>> = sessionDao.getAllSessions()

    suspend fun saveSession(session: ChargeUpSession) {
        sessionDao.insertSession(session)
    }

    suspend fun clearAll() {
        sessionDao.clearHistory()
    }
}
