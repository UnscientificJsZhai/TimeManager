package cn.unscientificjszhai.timemanager.ui.parse

import androidx.lifecycle.ViewModel
import com.github.unscientificjszhai.unscientificcourseparser.bean.factory.HardcodeScanner
import com.github.unscientificjszhai.unscientificcourseparser.bean.factory.ParserFactory

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