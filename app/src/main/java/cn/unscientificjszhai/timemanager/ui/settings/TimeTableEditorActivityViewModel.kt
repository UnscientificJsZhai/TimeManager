package cn.unscientificjszhai.timemanager.ui.settings

import androidx.lifecycle.ViewModel
import cn.unscientificjszhai.timemanager.data.tables.TimetableTypeConverter
import kotlin.reflect.KProperty

/**
 * 用于给[TimeTableEditorActivity]保存每节课间隔的ViewModel。
 */
internal class TimeTableEditorActivityViewModel : ViewModel() {

    var duration = 0

    /**
     * 用于校验前后是否修改过表。
     */
    var originTimeTable = ""

    private val typeConverter = TimetableTypeConverter()

    operator fun getValue(thisRef: Any?, property: KProperty<*>) = this.typeConverter
}