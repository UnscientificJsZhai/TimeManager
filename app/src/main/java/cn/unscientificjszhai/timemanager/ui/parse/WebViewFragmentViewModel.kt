package cn.unscientificjszhai.timemanager.ui.parse

import androidx.lifecycle.ViewModel
import com.github.unscientificjszhai.unscientificcourseparser.bean.core.Parser

/**
 * 用于存放当前的解析器的ViewModel。
 *
 * @see WebViewFragment
 */
internal class WebViewFragmentViewModel : ViewModel() {

    lateinit var parser: Parser
}