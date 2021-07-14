package cn.unscientificjszhai.timemanager.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cn.unscientificjszhai.timemanager.data.course.CourseWithClassTimes
import cn.unscientificjszhai.timemanager.data.dao.CourseDao

/**
 * MainActivity的ViewModel。
 *
 * @param courseList 从RoomDatabase中获取的全部课程的LiveData对象。
 * @see MainActivity
 */
internal class MainActivityViewModel(var courseList: LiveData<List<CourseWithClassTimes>>) :
    ViewModel() {

    /**
     * 主界面是否只显示今天的课程。
     */
    var showTodayOnly = false

    /**
     * 创建MainActivity的ViewModel的Factory。
     *
     * @param dao 一个Dao对象，用于初始化ViewModel时传入LiveData的参数
     */
    class Factory(private val dao: CourseDao) :
        ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainActivityViewModel(dao.getLiveCourses()) as T
        }
    }
}