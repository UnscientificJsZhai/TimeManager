package cn.unscientificjszhai.timemanager.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import cn.unscientificjszhai.timemanager.data.course.CourseWithClassTimes

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
}