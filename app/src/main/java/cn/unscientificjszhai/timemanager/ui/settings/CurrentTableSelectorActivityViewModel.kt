package cn.unscientificjszhai.timemanager.ui.settings

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cn.unscientificjszhai.timemanager.TimeManagerApplication
import cn.unscientificjszhai.timemanager.data.dao.CourseTableDao
import cn.unscientificjszhai.timemanager.data.tables.CourseTable
import cn.unscientificjszhai.timemanager.features.calendar.CalendarOperator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * CurrentTableSelectorActivity的ViewModel
 *
 * @see CurrentTableSelectorActivity
 * @author UnscientificJsZhai
 */
internal class CurrentTableSelectorActivityViewModel(val tableList: LiveData<List<CourseTable>>) :
    ViewModel() {

    /**
     * 创建CurrentTableSelectorActivity的ViewModel的Factory。
     *
     * @param dao 一个Dao对象，用于初始化ViewModel时传入LiveData的参数
     */
    class Factory(private val dao: CourseTableDao) :
        ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CurrentTableSelectorActivityViewModel(dao.getLiveCourseTables()) as T
        }
    }

    /**
     * 向数据库中添加课程表。
     *
     * @param context 执行添加操作的上下文。
     * @param courseTable 要添加的课程表对象。
     */
    suspend fun addCourseTable(context: Activity, courseTable: CourseTable) {
        val timeManagerApplication = context.application as TimeManagerApplication

        withContext(Dispatchers.IO) {
            CalendarOperator.createCalendarTable(context, courseTable)

            val dao =
                timeManagerApplication.getCourseTableDatabase().courseTableDao()
            val id = dao.insertCourseTable(courseTable)
            timeManagerApplication.updateTableID(id)
        }
    }

    /**
     * 重命名课程表。调用这个函数之前需要把要重命名的课程表对象的新名称赋值给[CourseTable.name]。
     *
     * @param context 执行添加操作的上下文。
     * @param courseTable 要重命名的课程表对象。
     */
    suspend fun renameCourseTable(context: Activity, courseTable: CourseTable) {
        val timeManagerApplication = context.application as TimeManagerApplication

        withContext(Dispatchers.IO) {
            CalendarOperator.updateCalendarTable(context, courseTable)

            val dao =
                timeManagerApplication.getCourseTableDatabase()
                    .courseTableDao()
            dao.updateCourseTable(courseTable)
        }
    }

    suspend fun deleteCourseTable(context: Activity, courseTable: CourseTable) {
        val timeManagerApplication = context.application as TimeManagerApplication

        withContext(Dispatchers.IO) {
            CalendarOperator.deleteCalendarTable(context, courseTable)

            val dao =
                timeManagerApplication.getCourseTableDatabase()
                    .courseTableDao()
            dao.deleteCourseTable(courseTable)
            context.deleteDatabase("table${courseTable.id}.db")
        }
    }
}