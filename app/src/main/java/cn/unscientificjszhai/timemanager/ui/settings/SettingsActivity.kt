package cn.unscientificjszhai.timemanager.ui.settings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.room.Room
import cn.unscientificjszhai.timemanager.R
import cn.unscientificjszhai.timemanager.TimeManagerApplication
import cn.unscientificjszhai.timemanager.data.database.CourseDatabase
import cn.unscientificjszhai.timemanager.features.backup.TableWithCourses
import cn.unscientificjszhai.timemanager.ui.ActivityUtility
import cn.unscientificjszhai.timemanager.ui.CalendarOperatorActivity
import cn.unscientificjszhai.timemanager.ui.ProgressDialog
import cn.unscientificjszhai.timemanager.ui.main.MainActivity
import java.io.IOException
import java.io.ObjectInputStream
import java.nio.charset.StandardCharsets
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
                        val progressDialog = ProgressDialog(this)
                        progressDialog.show()
                        thread(start = true) {
                            val objectString: String
                            val courseList =
                                timeManagerApplication.getCourseDatabase().courseDao().getCourses()
                            val serializableObject = TableWithCourses(courseTable, courseList)
                            try {
                                objectString = serializableObject.serializeObject()
                                val outputStream = contentResolver.openOutputStream(uri)
                                outputStream!!.write(objectString.toByteArray(StandardCharsets.ISO_8859_1))
                                outputStream.close()
                            } catch (e: IOException) {
                                runOnUiThread {
                                    Toast.makeText(
                                        this,
                                        R.string.activity_Settings_FailToBackup,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            progressDialog.postDismiss()
                        }
                    }
                }
            }

        //定义导入备份的逻辑
        this.importLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    val uri = it.data?.data
                    if (uri != null) {
                        val progressDialog = ProgressDialog(this)
                        progressDialog.show()
                        thread(start = true) {
                            val inputStream = contentResolver.openInputStream(uri)
                            val objectInputStream = ObjectInputStream(inputStream)
                            val tableWithCourses: TableWithCourses?
                            try {
                                tableWithCourses =
                                    objectInputStream.readObject() as TableWithCourses?
                            } catch (e: Exception) {
                                progressDialog.postDismiss()
                                runOnUiThread {
                                    Toast.makeText(
                                        this,
                                        R.string.activity_Settings_ImportError,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                return@thread
                            } finally {
                                objectInputStream.close()
                                inputStream?.close()
                            }

                            if (tableWithCourses == null) {
                                progressDialog.postDismiss()
                                runOnUiThread {
                                    Toast.makeText(
                                        this,
                                        R.string.activity_Settings_ImportError,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                return@thread
                            } else {
                                val newCourseTable = tableWithCourses.courseTable.apply {
                                    id = null
                                }
                                val courseTableDao =
                                    timeManagerApplication.getCourseTableDatabase()
                                        .courseTableDao()
                                val tableID = courseTableDao.insertCourseTable(newCourseTable)
                                val courseDatabase =
                                    Room.databaseBuilder(
                                        this,
                                        CourseDatabase::class.java,
                                        "table$tableID.db"
                                    ).build()
                                //添加课程
                                val courseDao = courseDatabase.courseDao()
                                val classTimeDao = courseDatabase.classTimeDao()
                                for (courseWithClassTimes in tableWithCourses.courses) {
                                    courseDao.insertCourse(courseWithClassTimes.course.apply {
                                        associatedEventsId.clear()
                                    })
                                    for (classTime in courseWithClassTimes.classTimes) {
                                        classTimeDao.insertClassTime(classTime)
                                    }
                                }
                            }
                            progressDialog.postDismiss()
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
     */
    internal fun saveBackup() {
        val courseTable by timeManagerApplication

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/octet-stream"
        intent.putExtra(Intent.EXTRA_TITLE, "${courseTable.name}.tmb")
        backupLauncher.launch(intent)
    }

    internal fun importBackup() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/octet-stream"
        importLauncher.launch(intent)
    }
}