package cn.unscientificjszhai.timemanager.ui.settings

import android.content.Context
import androidx.preference.PreferenceDataStore
import cn.unscientificjszhai.timemanager.data.course.ClassTime
import cn.unscientificjszhai.timemanager.data.dao.CourseTableDao
import cn.unscientificjszhai.timemanager.data.tables.CourseTable
import cn.unscientificjszhai.timemanager.providers.EventsOperator
import kotlin.concurrent.thread

/**
 * 用于定义Preferences数据存储的自定义DataStore。
 *
 * @param nowCourseTable 当前正在修改的CourseTable对象，当当前的课程表更改的时候，[SettingsActivity]会更新这里的引用。
 * @param courseTableDao 操作数据库的Dao接口对象。
 * @param context 上下文，用于更新日历。
 */
internal class SettingsDataStore(
    var nowCourseTable: CourseTable,
    private val courseTableDao: CourseTableDao,
    private val context: Context,
    val notifyApplicationCourseTableChanged: (Long) -> Unit
) : PreferenceDataStore() {

    override fun putString(key: String?, value: String?) {

        when (key) {
            SettingsFragment.MAX_WEEK_KEY -> {
                if (value?.isEmpty() == false) {
                    val newValue = try {
                        value.toInt()
                    } catch (e: NumberFormatException) {
                        0
                    }
                    if (newValue in 1..ClassTime.MAX_STORAGE_SIZE) {
                        this.nowCourseTable.maxWeeks = newValue
                    }
                }
            }
            SettingsFragment.CLASSES_PER_DAY_KEY -> {
                if (value?.isEmpty() == false) {
                    val newValue: Int = try {
                        value.toInt()
                    } catch (e: NumberFormatException) {
                        0
                    }
                    if (newValue in 1..CourseTable.MAX_CLASS_PER_DAY) {
                        this.nowCourseTable.classesPerDay = newValue
                    }
                }
            }
            else -> {
                return super.putString(key, value)
            }
        }
        updateCourseTable()
    }

    override fun getString(key: String?, defValue: String?): String? {
        when (key) {
            SettingsFragment.MAX_WEEK_KEY -> {
                return this.nowCourseTable.maxWeeks.toString()
            }
            SettingsFragment.CLASSES_PER_DAY_KEY -> {
                return this.nowCourseTable.classesPerDay.toString()
            }
            else -> {
                return null
            }
        }
    }

    /**
     * 把暂存在DataStore中的CourseTable对象更新到数据库，也同时更新Application中的相应的对象。
     */
    internal fun updateCourseTable() {
        thread(start = true) {
            courseTableDao.updateCourseTable(this.nowCourseTable)
            notifyApplicationCourseTableChanged(nowCourseTable.id!!)

            //向日历插入数据
            EventsOperator.updateAllEvents(context, this.nowCourseTable)
        }
    }
}