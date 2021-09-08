package cn.unscientificjszhai.timemanager.ui.settings

import android.app.Activity
import androidx.lifecycle.ViewModel
import cn.unscientificjszhai.timemanager.TimeManagerApplication
import cn.unscientificjszhai.timemanager.data.tables.CourseTable
import cn.unscientificjszhai.timemanager.data.tables.TimetableTypeConverter
import cn.unscientificjszhai.timemanager.features.calendar.EventsOperator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.reflect.KProperty

/**
 * 用于给[TimeTableEditorActivity]保存每节课间隔的ViewModel。
 */
internal class TimeTableEditorActivityViewModel : ViewModel() {

    var duration = 0

    lateinit var courseTable: CourseTable

    /**
     * 用于校验前后是否修改过表。
     */
    var originTimeTable = ""

    private val typeConverter = TimetableTypeConverter()

    operator fun getValue(thisRef: Any?, property: KProperty<*>) = this.typeConverter

    /**
     * 保存更改后的上课时间数据。
     *
     * @param context 执行此操作的Activity上下文。
     */
    suspend fun save(context: Activity) {
        val timeManagerApplication = context.application as TimeManagerApplication

        withContext(Dispatchers.Default) {
            timeManagerApplication.getCourseTableDatabase().courseTableDao()
                    .updateCourseTable(this@TimeTableEditorActivityViewModel.courseTable)
            timeManagerApplication.updateTableID(this@TimeTableEditorActivityViewModel.courseTable.id!!)

            EventsOperator.updateAllEvents(
                    context,
                    this@TimeTableEditorActivityViewModel.courseTable
            )
        }
    }
}