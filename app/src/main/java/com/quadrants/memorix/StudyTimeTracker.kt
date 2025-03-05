import android.content.Context
import android.content.SharedPreferences

class StudyTimeTracker(context: Context) {
    private var startTime: Long = 0
    private var totalTime: Long = 0
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("StudyTimePrefs", Context.MODE_PRIVATE)

    init {
        totalTime = sharedPreferences.getLong("totalStudyTime", 0) // ✅ Restore saved time
    }

    fun startTracking() {
        startTime = System.currentTimeMillis()
    }

    fun stopTracking() {
        if (startTime > 0) {
            totalTime += (System.currentTimeMillis() - startTime) / 1000 / 60
            startTime = 0
            saveTotalTime()
        }
    }


    fun getTotalStudyTime(): Long {
        return totalTime
    }

    fun reset() {
        totalTime = 0
        startTime = System.currentTimeMillis()
        saveTotalTime() // ✅ Reset and save
    }

    private fun saveTotalTime() {
        sharedPreferences.edit().putLong("totalStudyTime", totalTime).apply() // ✅ Persist time
    }
}
