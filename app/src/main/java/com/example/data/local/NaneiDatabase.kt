package com.example.data.local

import android.content.Context
import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

// --- Type Converters ---
class Converters {
    @TypeConverter
    fun fromEventType(value: EventType): String = value.name

    @TypeConverter
    fun toEventType(value: String): EventType = try {
        EventType.valueOf(value)
    } catch (e: Exception) {
        EventType.NOTE
    }
}

// --- DAOs ---

@Dao
interface BabyDao {
    @Query("SELECT * FROM babies ORDER BY id ASC")
    fun getAllBabies(): Flow<List<Baby>>

    @Query("SELECT * FROM babies WHERE isSelected = 1 LIMIT 1")
    fun getSelectedBaby(): Flow<Baby?>

    @Query("SELECT * FROM babies WHERE isSelected = 1 LIMIT 1")
    suspend fun getSelectedBabySync(): Baby?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBaby(baby: Baby): Long

    @Update
    suspend fun updateBaby(baby: Baby)

    @Query("UPDATE babies SET isSelected = 0")
    suspend fun clearSelectedBabies()

    @Query("UPDATE babies SET isSelected = 1 WHERE id = :babyId")
    suspend fun setSelectedBaby(babyId: Long)

    @Query("DELETE FROM babies WHERE id = :babyId")
    suspend fun deleteBaby(babyId: Long)
}

@Dao
interface EventDao {
    @Query("SELECT * FROM events WHERE babyId = :babyId ORDER BY startTimeMs DESC")
    fun getEventsForBaby(babyId: Long): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE babyId = :babyId AND startTimeMs >= :fromTimeMs ORDER BY startTimeMs DESC")
    fun getEventsSince(babyId: Long, fromTimeMs: Long): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE babyId = :babyId AND type = :type ORDER BY startTimeMs DESC LIMIT 1")
    suspend fun getLastEventOfType(babyId: Long, type: EventType): Event?

    @Query("SELECT * FROM events WHERE babyId = :babyId AND type = :type ORDER BY startTimeMs DESC LIMIT 1")
    fun observeLastEventOfType(babyId: Long, type: EventType): Flow<Event?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event): Long

    @Update
    suspend fun updateEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)

    @Query("DELETE FROM events WHERE babyId = :babyId")
    suspend fun deleteAllEventsForBaby(babyId: Long)
}

@Dao
interface MilestoneDao {
    @Query("SELECT * FROM milestones WHERE babyId = :babyId ORDER BY targetAgeMonths ASC")
    fun getMilestonesForBaby(babyId: Long): Flow<List<Milestone>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestones(milestones: List<Milestone>)

    @Update
    suspend fun updateMilestone(milestone: Milestone)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE babyId = :babyId ORDER BY hour ASC, minute ASC")
    fun getRemindersForBaby(babyId: Long): Flow<List<Reminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestampMs DESC")
    fun getAllAuditLogs(): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLog)

    @Query("DELETE FROM audit_logs")
    suspend fun clearAuditLogs()
}

// --- Database ---
@Database(
    entities = [Baby::class, Event::class, Milestone::class, Reminder::class, AuditLog::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NaneiDatabase : RoomDatabase() {
    abstract fun babyDao(): BabyDao
    abstract fun eventDao(): EventDao
    abstract fun milestoneDao(): MilestoneDao
    abstract fun reminderDao(): ReminderDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: NaneiDatabase? = null

        fun getDatabase(context: Context): NaneiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NaneiDatabase::class.java,
                    "nanei_db_v2"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
