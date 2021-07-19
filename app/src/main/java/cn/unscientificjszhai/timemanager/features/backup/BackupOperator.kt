package cn.unscientificjszhai.timemanager.features.backup

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import cn.unscientificjszhai.timemanager.R
import cn.unscientificjszhai.timemanager.TimeManagerApplication
import cn.unscientificjszhai.timemanager.data.database.CourseDatabase
import cn.unscientificjszhai.timemanager.data.tables.CourseTable
import cn.unscientificjszhai.timemanager.ui.ProgressDialog
import java.io.IOException
import java.io.ObjectInputStream
import java.nio.charset.StandardCharsets
import kotlin.concurrent.thread

/**
 * 导出和导入备份的操作类。
 * 使用序列化和反序列化实现功能。
 */
object BackupOperator {

    /**
     * 导出备份的具体实现。在处理过程中，会在窗口上显示一个Dialog。
     * 这个方法应该在[Activity.onActivityResult]中被调用，
     * 或者在[AppCompatActivity.registerForActivityResult]中注册。
     *
     * @param context 进行备份操作的上下文，因为要显示Dialog，仅接受Activity。
     * @param uri 备份文件的uri，需要可以被写入。
     */
    fun exportBackup(context: Activity, uri: Uri) {
        val timeManagerApplication = (context.applicationContext) as TimeManagerApplication
        val courseTable by timeManagerApplication
        val contentResolver = context.contentResolver

        val progressDialog = ProgressDialog(context)
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
                context.runOnUiThread {
                    Toast.makeText(
                        context,
                        R.string.activity_Settings_FailToBackup,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            progressDialog.postDismiss()
        }
    }

    /**
     * 导入备份的具体实现。在处理过程中，会在窗口上显示一个Dialog。
     * 这个方法应该在[Activity.onActivityResult]中被调用，
     * 或者在[AppCompatActivity.registerForActivityResult]中注册。
     *
     * @param context 进行备份操作的上下文，因为要显示Dialog，仅接受Activity。
     * @param uri 备份文件的uri，需要可以被读取。
     */
    fun importBackup(context: Activity, uri: Uri) {
        val timeManagerApplication = (context.applicationContext) as TimeManagerApplication
        val contentResolver = context.contentResolver

        val progressDialog = ProgressDialog(context)
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
                context.runOnUiThread {
                    Toast.makeText(
                        context,
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
                context.runOnUiThread {
                    Toast.makeText(
                        context,
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
                        context,
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

    /**
     * 生成用于启动导出过程的Intent。
     *
     * @param courseTable 将要备份的课程表对象，用于获取标题以决定文件名。
     * @return 生成的Intent。以这个Intent调用[Activity.startActivityForResult]或者[ActivityResultLauncher.launch]方法，
     * 启动系统文件管理器选择文件存储。
     */
    fun getExportBackupIntent(courseTable: CourseTable) =
        Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_TITLE, "${courseTable.name}.tmb")
        }

    /**
     * 生成用于启动导入过程的Intent。
     *
     * @return 生成的Intent。以这个Intent调用[Activity.startActivityForResult]或者[ActivityResultLauncher.launch]方法，
     * 启动系统文件管理器选择文件读取备份。
     */
    fun getImportBackupIntent() = Intent(Intent.ACTION_GET_CONTENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "application/octet-stream"
    }
}