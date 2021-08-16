package cn.unscientificjszhai.timemanager.ui.parse

import androidx.lifecycle.ViewModel
import com.github.unscientificjszhai.unscientificcourseparser.core.factory.HardcodeScanner
import com.github.unscientificjszhai.unscientificcourseparser.core.factory.ParserFactory

/**
 * ParseCourseActivity的ViewModel。
 *
 * @see ParseCourseActivity
 */
internal class ParseCourseActivityViewModel : ViewModel() {

    val parserFactory by lazy {
        ParserFactory(HardcodeScanner())
    }
}