package cn.unscientificjszhai.timemanager.ui.main

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import cn.unscientificjszhai.timemanager.R
import cn.unscientificjszhai.timemanager.TimeManagerApplication
import cn.unscientificjszhai.timemanager.data.course.Course
import cn.unscientificjszhai.timemanager.data.course.CourseWithClassTimes
import cn.unscientificjszhai.timemanager.providers.EventsOperator
import cn.unscientificjszhai.timemanager.ui.ActivityUtility.jumpToSystemPermissionSettings
import cn.unscientificjszhai.timemanager.ui.ActivityUtility.runIfPermissionGranted
import cn.unscientificjszhai.timemanager.ui.editor.EditCourseActivity
import java.io.Serializable
import kotlin.concurrent.thread

/**
 * Dialog形式的Activity。用于在[MainActivity]中点击一个项目的时候显示它的详情。
 * 传递进来的Intent中的可序列化Extra应该是[CourseWithClassTimes]类型的。
 *
 * @see MainActivity
 * @see Course
 */
class CourseDetailActivity : Activity() {

    companion object {

        /**
         * 启动此Activity的通用方法。如果context是[MainActivity]的话，则会启动下面的方法。
         *
         * @param context 上下文。
         * @param courseWithClassTimes 数据对象。
         */
        @JvmStatic
        fun startThisActivity(context: Context, courseWithClassTimes: CourseWithClassTimes) {
            val intent = Intent(context, CourseDetailActivity::class.java)
            intent.putExtra(INTENT_EXTRA_COURSE, courseWithClassTimes)
            context.startActivity(intent)
        }

        /**
         * 启动此Activity时随Intent传入了一个整型Extra，其Key为此值。
         */
        const val INTENT_EXTRA_COURSE = "course"

        /**
         * 在[EditCourseActivity]中如果修改了Course对象，不仅会体现在数据库中，
         * 而且会通过Intent将修改后的对象传递回来。其Key为此值。
         */
        const val EDIT_INTENT_RESULT = "courseResult"

        /**
         * 此Activity在[onActivityResult]方法中接收[EditCourseActivity]返回的修改后对象时，requestCode为此值。
         */
        const val EDIT_REQUEST_CODE = 4
    }

    private lateinit var courseWithClassTimes: CourseWithClassTimes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_detail)

        val serializable = intent.getSerializableExtra(INTENT_EXTRA_COURSE)
        updateViewFromCourse(serializable)

        //定义编辑按钮
        findViewById<Button>(R.id.CourseDetailActivity_EditButton).setOnClickListener {
            EditCourseActivity.startThisActivity(this, courseWithClassTimes)
        }

        //定义删除按钮
        findViewById<Button>(R.id.CourseDetailActivity_DeleteButton).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.activity_CourseDetail_DeleteConfirm)
                .setNegativeButton(R.string.common_cancel) { dialog, _ ->
                    dialog?.dismiss()
                }
                .setPositiveButton(R.string.common_confirm) { dialog, _ ->
                    runIfPermissionGranted(Manifest.permission.WRITE_CALENDAR, {
                        dialog.dismiss()
                        AlertDialog.Builder(this)
                            .setTitle(R.string.activity_WelcomeActivity_AskPermissionTitle)
                            .setMessage(R.string.activity_CourseDetail_AskPermissionText)
                            .setNegativeButton(R.string.common_cancel) { permissionDialog, _ ->
                                //拒绝授予日历权限。
                                permissionDialog.dismiss()
                            }.setPositiveButton(R.string.common_confirm) { permissionDialog, _ ->
                                //同意授予日历权限，跳转到系统设置进行授权。
                                this@CourseDetailActivity.jumpToSystemPermissionSettings()
                                permissionDialog.dismiss()
                            }
                    }) {
                        thread(start = true) {
                            //从日历中删除。
                            val courseTable = (application as TimeManagerApplication).courseTable!!
                            EventsOperator.deleteEvent(
                                this@CourseDetailActivity,
                                courseTable,
                                courseWithClassTimes
                            )
                            //从数据库中删除。
                            val database =
                                (application as TimeManagerApplication).getCourseDatabase()
                            val courseDao = database.courseDao()
                            val classTimeDao = database.classTimeDao()
                            courseDao.deleteCourse(courseWithClassTimes.course)
                            classTimeDao.deleteClassTimes(courseWithClassTimes.classTimes)
                        }
                        dialog.dismiss()
                        finish()
                    }
                }.create().show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == EDIT_REQUEST_CODE && resultCode == RESULT_OK && data is Intent) {
            val serializable = data.getSerializableExtra(EDIT_INTENT_RESULT)
            updateViewFromCourse(serializable)
        }
    }

    /**
     * 用于格式化处理描述文字的方法。
     *
     * @param course 数据对象。
     * @return 字符串。应该给DescriptionText的text赋值此字符串。
     */
    private fun createDescriptionText(course: Course): String {
        return "${course.remarks}学分，备注如下：\n${course.remarks}"
    }

    /**
     * 从Course对象中的数据更新界面。Course对象一般来自Intent中的可序列化Extra。
     *
     * @param serializable 从Intent对象中获取的可序列化对象。可空。
     * 如果为空将会立即结束此Activity并弹出一个Toast。
     */
    private fun updateViewFromCourse(serializable: Serializable?) {
        if (!Course.checkLegitimacy(serializable)) {
            Toast.makeText(
                this,
                getText(R.string.activity_CourseDetail_LoadingErrorToast),
                Toast.LENGTH_SHORT
            ).show()
            finish()
            return
        }

        this.courseWithClassTimes = serializable as CourseWithClassTimes
        findViewById<TextView>(R.id.CourseDetailActivity_Title).text =
            courseWithClassTimes.course.title
        findViewById<TextView>(R.id.CourseDetailActivity_DescriptionText).text =
            this.createDescriptionText(courseWithClassTimes.course)
    }
}