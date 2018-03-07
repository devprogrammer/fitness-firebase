package at.shockbytes.corey.dagger

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Vibrator
import android.preference.PreferenceManager
import at.shockbytes.corey.common.core.running.location.GooglePlayLocationManager
import at.shockbytes.corey.common.core.running.location.LocationManager
import at.shockbytes.corey.common.core.util.ExerciseDeserializer
import at.shockbytes.corey.common.core.workout.model.Exercise
import at.shockbytes.corey.storage.FirebaseStorageManager
import at.shockbytes.corey.storage.StorageManager
import at.shockbytes.corey.user.FirebaseUserManager
import at.shockbytes.corey.user.UserManager
import at.shockbytes.corey.schedule.DefaultScheduleManager
import at.shockbytes.corey.schedule.ScheduleManager
import at.shockbytes.corey.wearable.AndroidWearManager
import at.shockbytes.corey.wearable.WearableManager
import at.shockbytes.corey.workout.WorkoutManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * @author Martin Macheiner
 * Date: 21.02.2017.
 */
@Module
class AppModule(private val app: Application) {

    @Provides
    @Singleton
    fun provideSharedPreferences(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(app)
    }

    @Provides
    @Singleton
    fun provideUserManager(): UserManager {
        return FirebaseUserManager(app.applicationContext)
    }

    @Provides
    @Singleton
    fun provideScheduleManager(storageManager: StorageManager,
                               preferences: SharedPreferences): ScheduleManager {
        return DefaultScheduleManager(storageManager, app.applicationContext, preferences)
    }

    @Provides
    @Singleton
    fun provideStorageManager(preferences: SharedPreferences, gson: Gson): StorageManager {
        return FirebaseStorageManager(app.applicationContext, gson, preferences)
    }

    @Provides
    @Singleton
    fun provideWearableManager(workoutManager: WorkoutManager,
                               storageManager: StorageManager,
                               gson: Gson): WearableManager {
        return AndroidWearManager(app.applicationContext, workoutManager, storageManager, gson)
    }

    @Provides
    @Singleton
    fun provideLocationManager(): LocationManager {
        return GooglePlayLocationManager(app.applicationContext)
    }

    @Provides
    @Singleton
    fun provideVibrator(): Vibrator {
        return app.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
                .registerTypeHierarchyAdapter(Exercise::class.java, ExerciseDeserializer())
                .create()
    }

}