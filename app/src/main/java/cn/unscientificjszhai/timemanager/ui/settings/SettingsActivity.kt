package cn.unscientificjszhai.timemanager.ui.settings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import cn.unscientificjszhai.timemanager.R
import cn.unscientificjszhai.timemanager.TimeManagerApplication
import cn.unscientificjszhai.timemanager.features.backup.BackupOperator
import cn.unscientificjszhai.timemanager.features.backup.CourseICS
import cn.unscientificjszhai.timemanager.ui.ActivityUtility
import cn.unscientificjszhai.timemanager.ui.CalendarOperatorActivity
import cn.unscientificjszhai.timemanager.ui.main.MainActivity
import kotlin.concurrent.thread

/**
 * 设置Activity，设置项的初始化在SettingsFragment中。使用了JetPack库的Preference库。
 *
 * @see SettingsFragment
 */
class SettingsActivity : CalendarOperatorActivity() {

    private lateinit var timeManagerApplication: TimeManagerApplication

    private lateinit var backupLauncher: ActivityResultLauncher<Intent>
    private lateinit var importLauncher: ActivityResultLauncher<Intent>
    private lateinit var exportIcsLauncher: ActivityResultLauncher<Intent>

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
        ActivityUtility.setSystemUIAppearance(this)

        this.timeManagerApplication = application as TimeManagerApplication

        //替换Fragment
        val courseTable by timeManagerApplication
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

        //定义保存备份的逻辑
        this.backupLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    val uri = it.data?.data
                    if (uri != null) {
                        BackupOperator.exportBackup(this, uri)
                    }
                }
            }

        //定义导入备份的逻辑
        this.importLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    val uri = it.data?.data
                    if (uri != null) {
                        BackupOperator.importBackup(this, uri)
                    }
                }
            }

        //定义导出ICS的逻辑
        this.exportIcsLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    val uri = it.data?.data
                    if (uri != null) {
                        thread(start = true) {
                            val courseList =
                                timeManagerApplication.getCourseDatabase().courseDao().getCourses()
                            val courseICS = CourseICS(courseList, courseTable)
                            runOnUiThread {
                                courseICS.writeToFile(this, uri)
                            }
                        }
                    }
                }
            }
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

    /**
     * Fragment调用的，保存备份时调用的方法。
     *
     * @return 给onPreferenceClick做返回值的true。
     */
    internal fun saveBackup(): Boolean {
        val courseTable by timeManagerApplication
        backupLauncher.launch(BackupOperator.getExportBackupIntent(courseTable))
        return true
    }

    /**
     * Fragment调用的，导入备份时调用的方法。
     *
     * @return 给onPreferenceClick做返回值的true。
     */
    internal fun importBackup(): Boolean {
        importLauncher.launch(BackupOperator.getImportBackupIntent())
        return true
    }

    /**
     * Fragment调用的，导出ics时调用的方法。
     *
     * @return 给onPreferenceClick做返回值的true。
     */
    internal fun exportIcs(): Boolean {
        val courseTable by timeManagerApplication
        exportIcsLauncher.launch(CourseICS.getExportIcsIntent(courseTable))
        return true
    }
}