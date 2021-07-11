package cn.unscientificjszhai.timemanager.ui.editor

import androidx.lifecycle.ViewModel
import cn.unscientificjszhai.timemanager.data.course.Course
import cn.unscientificjszhai.timemanager.data.course.ClassTime

/**
 * EditCourseActivity的ViewModel。
 *
 * @see EditCourseActivity
 */
internal class EditCourseActivityViewModel : ViewModel() {
    /**
     * 如果是修改已有Course的话，则不为空。
     */
    var course: Course? = null

    /**
     * 被删除的ClassTime对象。只有删除之前存在的对象（即数据库中存在）时，才将其引用送入此数组。
     */
    val removedClassTimes = ArrayList<ClassTime>()

    /**
     * 目前的ClassTime对象。
     */
    lateinit var classTimes: ArrayList<ClassTime>

    /**
     * 标志classTime列表已经初始化完成的布尔值。
     */
    var classTimesInitialized = false
}