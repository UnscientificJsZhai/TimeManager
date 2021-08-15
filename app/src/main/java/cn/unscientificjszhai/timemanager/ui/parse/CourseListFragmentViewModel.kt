package cn.unscientificjszhai.timemanager.ui.parse

import androidx.lifecycle.ViewModel
import cn.unscientificjszhai.timemanager.TimeManagerApplication
import cn.unscientificjszhai.timemanager.data.course.CourseWithClassTimes
import cn.unscientificjszhai.timemanager.features.calendar.EventsOperator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 用于CourseListFragment的ViewModel。
 *
 * @see CourseListFragment
 */
internal class CourseListFragmentViewModel : ViewModel() {

    lateinit var courseList: List<CourseWithClassTimes>

    /**
     * 将从教务系统解析到的数据导入当前课程表。
     *
     * @param application 用于导入和获取数据库对象的上下文。
     */
    suspend fun save(application: TimeManagerApplication) {
        if (this.courseList.isEmpty()) {
            return
        }
        withContext(Dispatchers.IO) {
            //导入德奥当前的课程表中
            val courseDatabase = application.getCourseDatabase()
            val courseTable by application
            val courseDao = courseDatabase.courseDao()
            val classTimesDao = courseDatabase.classTimeDao()

            for (courseWithClassTime in this@CourseListFragmentViewModel.courseList) {
                val course = courseWithClassTime.course
                EventsOperator.addEvent(application,courseTable,courseWithClassTime)
                val courseId = courseDao.insertCourse(course)
                for (classTime in courseWithClassTime.classTimes) {
                    classTime.courseId = courseId
                    classTimesDao.insertClassTime(classTime)
                }
            }
        }
    }
}