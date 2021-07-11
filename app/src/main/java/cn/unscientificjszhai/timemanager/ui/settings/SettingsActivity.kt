package cn.unscientificjszhai.timemanager.ui.settings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.MenuItem
import cn.unscientificjszhai.timemanager.R
import cn.unscientificjszhai.timemanager.TimeManagerApplication
import cn.unscientificjszhai.timemanager.ui.ActivityUtility
import cn.unscientificjszhai.timemanager.ui.CalendarOperatorActivity
import cn.unscientificjszhai.timemanager.ui.main.MainActivity

/**
 * 设置Activity，设置项的初始化在SettingsFragment中。使用了JetPack库的Preference库。
 *
 * @see SettingsFragment
 */
class SettingsActivity : CalendarOperatorActivity() {

    private lateinit var timeManagerApplication: TimeManagerApplication

    inner class DatabaseChangeReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (context is SettingsActivity) {
                //当在Selector中选择新的课程表时调用此方法更新DataStore中的courseTable对象
                //并且会同时更新子Fragment中的数据简介
                settingsFragment?.updateCourseTable(timeManagerApplication.courseTable!!)
            }
        }
    }

    private lateinit var databaseChangeReceiver: DatabaseChangeReceiver

    private var settingsFragment: SettingsFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        //设置SystemUI颜色
        ActivityUtility.setSystemUIAppearance(window)

        this.timeManagerApplication = application as TimeManagerApplication

        //替换Fragment
        val courseTable = timeManagerApplication.courseTable!!
        if (savedInstanceState == null) {
            this.settingsFragment = SettingsFragment(
                SettingsDataStore(
                    courseTable,
                    timeManagerApplication.getCourseTableDatabase().courseTableDao(),
                    this
                ) {
                    timeManagerApplication.updateTableID(it)
                }
            )
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, this.settingsFragment!!)
                .commit()
        }

        //监听数据库变更
        val intentFilter = IntentFilter()
        intentFilter.addAction(MainActivity.COURSE_DATABASE_CHANGE_ACTION)
        this.databaseChangeReceiver = DatabaseChangeReceiver()
        registerReceiver(this.databaseChangeReceiver, intentFilter)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        if (item.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }

    override fun onDestroy() {
        unregisterReceiver(this.databaseChangeReceiver)
        super.onDestroy()
    }
}