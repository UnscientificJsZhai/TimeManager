package cn.unscientificjszhai.timemanager.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cn.unscientificjszhai.timemanager.data.dao.CourseDao

/**
 * 创建MainActivity的ViewModel的Factory。
 *
 * @param dao 一个Dao对象，用于初始化ViewModel时传入LiveData的参数
 */
internal class MainActivityViewModelFactory(private val dao: CourseDao) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainActivityViewModel(dao.getLiveCourses()) as T
    }
}