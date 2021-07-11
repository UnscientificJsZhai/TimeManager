package cn.unscientificjszhai.timemanager.ui.settings

import androidx.lifecycle.ViewModel

/**
 * 用于给[TimeTableEditorActivity]保存每节课间隔的ViewModel。
 */
internal class TimeTableEditorActivityViewModel : ViewModel() {

    var duration = 0

    /**
     * 用于校验前后是否修改过表。
     */
    var timeTableHashCode = 0
}