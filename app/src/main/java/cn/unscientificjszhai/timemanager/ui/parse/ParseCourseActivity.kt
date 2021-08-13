package cn.unscientificjszhai.timemanager.ui.parse

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import cn.unscientificjszhai.timemanager.R
import cn.unscientificjszhai.timemanager.ui.others.ActivityUtility
import com.github.unscientificjszhai.unscientificcourseparser.bean.factory.ParserFactory
import kotlin.reflect.KProperty

class ParseCourseActivity : AppCompatActivity() {

    private lateinit var viewModel: ParseCourseActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parse_course)

        ActivityUtility.setSystemUIAppearance(this)

        this.viewModel = ViewModelProvider(this)[ParseCourseActivityViewModel::class.java]

        if (savedInstanceState == null) {
            //旋转屏幕时不重新进入ParserListFragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.ParseCourseActivity_RootView, ParserListFragment())
                .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 提供工厂类的属性委托功能。
     */
    operator fun getValue(thisRef: Any?, property: KProperty<*>): ParserFactory {
        return this.viewModel.parserFactory
    }

    internal fun startWebViewFragment(beanName: String) {
        val webViewFragment = WebViewFragment.newInstance(beanName)
        supportFragmentManager.beginTransaction()
            .replace(R.id.ParseCourseActivity_RootView, webViewFragment)
            .commit()
    }
}