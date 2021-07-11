package cn.unscientificjszhai.timemanager.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import cn.unscientificjszhai.timemanager.R
import cn.unscientificjszhai.timemanager.TimeManagerApplication
import cn.unscientificjszhai.timemanager.data.tables.CourseTable
import cn.unscientificjszhai.timemanager.providers.CalendarOperator
import cn.unscientificjszhai.timemanager.ui.ActivityUtility.jumpToSystemPermissionSettings
import cn.unscientificjszhai.timemanager.ui.ActivityUtility.runIfPermissionGranted
import cn.unscientificjszhai.timemanager.ui.main.MainActivity
import kotlin.concurrent.thread

/**
 * 首页Activity，用于处理授权和创建第一个课程表。
 */
class WelcomeActivity : CalendarOperatorActivity(), View.OnClickListener {

    companion object {

        /**
         * 创建第一个课程表的时候请求日历写入权限的请求码。
         */
        private const val CREATE_FIRST_TABLE_REQUEST_CODE = 2
    }

    private lateinit var timeManagerApplication: TimeManagerApplication

    private lateinit var tableTitleEditText: EditText
    private lateinit var startButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.timeManagerApplication = application as TimeManagerApplication

        if (this.timeManagerApplication.nowTableID != TimeManagerApplication.DEFAULT_DATABASE_OBJECT_ID) {
            val mainActivityIntent = Intent(this, MainActivity::class.java)
            startActivity(mainActivityIntent)
            finish()
        } else {
            //如果当前所在表的ID为-1，就是没有初始化
            setContentView(R.layout.activity_welcome)

            ActivityUtility.setSystemUIAppearance(window)

            this.tableTitleEditText = findViewById(R.id.WelcomeActivity_TableNameEditText)
            this.startButton = findViewById(R.id.WelcomeActivity_Button)
            this.progressBar = findViewById(R.id.WelcomeActivity_ProgressBar)
            this.textView = findViewById(R.id.WelcomeActivity_Text)

            //回车确认功能
            this.tableTitleEditText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    this.onClick(this.startButton)
                }
                true
            }

            this.startButton.setOnClickListener(this)
        }
    }

    override fun onClick(v: View?) {
        startButton.isEnabled = false
        textView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE

        val tableName: String = if (tableTitleEditText.text.isEmpty()) {
            //当输入为空时的默认课程表名
            getString(R.string.activity_Welcome_EditTextHint)
        } else {
            tableTitleEditText.text.toString()
        }

        runIfPermissionGranted(Manifest.permission.WRITE_CALENDAR, {
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CALENDAR)) {
                //系统仍然提示权限请求
                AlertDialog.Builder(this)
                    .setTitle(R.string.activity_WelcomeActivity_AskPermissionTitle)
                    .setMessage(R.string.activity_WelcomeActivity_AskPermissionText)
                    .setNegativeButton(R.string.common_cancel) { dialog, _ ->
                        dialog.dismiss()
                    }.setPositiveButton(R.string.common_confirm) { dialog, _ ->
                        if (ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.WRITE_CALENDAR
                            ) == PackageManager.PERMISSION_DENIED
                        ) {
                            //申请日历权限
                            requestPermissions(
                                arrayOf(Manifest.permission.WRITE_CALENDAR),
                                CREATE_FIRST_TABLE_REQUEST_CODE
                            )
                        }
                        dialog.dismiss()
                    }.show()
            } else {
                //系统不再提示权限请求
                AlertDialog.Builder(this)
                    .setTitle(R.string.activity_WelcomeActivity_AskPermissionTitle)
                    .setMessage(
                        getString(R.string.activity_WelcomeActivity_AskPermissionText) + "\n"
                                + getString(R.string.activity_WelcomeActivity_SettingsPermissionText)
                    )
                    .setNegativeButton(R.string.common_cancel) { dialog, _ ->
                        dialog.dismiss()
                    }.setPositiveButton(R.string.common_confirm) { dialog, _ ->
                        jumpToSystemPermissionSettings()
                        dialog.dismiss()
                    }.show()
            }

        }) {
            createFirstTable(tableName)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == CREATE_FIRST_TABLE_REQUEST_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                //获得了写入日历的权限,继续写入日历。
                createFirstTable(
                    if (tableTitleEditText.text.isEmpty()) {
                        //当输入为空时的默认课程表名
                        getString(R.string.activity_Welcome_EditTextHint)
                    } else {
                        tableTitleEditText.text.toString()
                    }
                )
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**
     * 创建本应用的第一个课程表对象，同时也会将其写入日历。
     * 此方法不会检查日历权限，需要在调用钱检查。
     *
     * @param tableName 课程表的标题。
     */
    private fun createFirstTable(tableName: String) {
        thread(start = true) {
            CalendarOperator.deleteAllTables(this) //检查并删除清除数据之前的遗留日历表。
            val courseTable = CourseTable(tableName)

            //写入日历。
            CalendarOperator.createCalendarTable(this, courseTable)

            //写入数据库。
            val dao = timeManagerApplication.getCourseTableDatabase().courseTableDao()
            val id = dao.insertCourseTable(courseTable)
            timeManagerApplication.updateTableID(id)

            runOnUiThread {
                val mainActivityIntent = Intent(this, MainActivity::class.java)
                startActivity(mainActivityIntent)
                this.finish()
            }
        }
    }
}