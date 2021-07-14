package cn.unscientificjszhai.timemanager.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cn.unscientificjszhai.timemanager.data.dao.CourseTableDao
import cn.unscientificjszhai.timemanager.data.tables.CourseTable

/**
 * CurrentTableSelectorActivity的ViewModel
 *
 * @see CurrentTableSelectorActivity
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
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return CurrentTableSelectorActivityViewModel(dao.getLiveCourseTables()) as T
        }
    }
}