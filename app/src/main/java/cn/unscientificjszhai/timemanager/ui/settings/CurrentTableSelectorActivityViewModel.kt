package cn.unscientificjszhai.timemanager.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import cn.unscientificjszhai.timemanager.data.tables.CourseTable

/**
 * CurrentTableSelectorActivity的ViewModel
 *
 * @see CurrentTableSelectorActivity
 */
internal class CurrentTableSelectorActivityViewModel(val tableList: LiveData<List<CourseTable>>) :
    ViewModel()