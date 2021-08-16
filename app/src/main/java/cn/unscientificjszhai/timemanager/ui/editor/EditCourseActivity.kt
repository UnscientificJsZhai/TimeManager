package cn.unscientificjszhai.timemanager.ui.editor

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.unscientificjszhai.timemanager.R
import cn.unscientificjszhai.timemanager.TimeManagerApplication
import cn.unscientificjszhai.timemanager.data.course.ClassTime
import cn.unscientificjszhai.timemanager.data.course.Course
import cn.unscientificjszhai.timemanager.data.course.CourseWithClassTimes
import cn.unscientificjszhai.timemanager.data.database.CourseDatabase
import cn.unscientificjszhai.timemanager.ui.main.CourseDetailActivity
import cn.unscientificjszhai.timemanager.ui.others.ActivityUtility
import cn.unscientificjszhai.timemanager.ui.others.ActivityUtility.jumpToSystemPermissionSettings
import cn.unscientificjszhai.timemanager.ui.others.ActivityUtility.runIfPermissionGranted
import cn.unscientificjszhai.timemanager.ui.others.CalendarOperatorActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class EditCourseActivity : CalendarOperatorActivity() {

    companion object {

        /**
         * 保存数据时检查日历写入权限的请求码。
         */
        private const val SAVING_REQUEST_CODE = 3

        /**
         * 启动此Activity的通用方法。
         *
         * @param context 上下文。
         */
        @JvmStatic
        fun startThisActivity(context: Context) {
            val intent = Intent(context, EditCourseActivity::class.java)
            context.startActivity(intent)
        }

        /**
         * 启动此Activity的专用方法，使用[startActivityForResult]方法启动，
         * 请求码为[CourseDetailActivity.EDIT_REQUEST_CODE]。
         *
         * @param context 上下文。
         * @param courseWithClassTimes 数据对象。
         * @see CourseDetailActivity
         */
        @JvmStatic
        fun startThisActivity(
            context: Context,
            courseWithClassTimes: CourseWithClassTimes
        ) {
            val intent = Intent(context, EditCourseActivity::class.java)
            intent.putExtra(CourseDetailActivity.INTENT_EXTRA_COURSE, courseWithClassTimes)
            context.startActivity(intent)
        }
    }

    private lateinit var timeManagerApplication: TimeManagerApplication

    private lateinit var viewModel: EditCourseActivityViewModel

    private lateinit var courseDatabase: CourseDatabase

    private lateinit var rootRecyclerView: RecyclerView
    private lateinit var adapter: EditCourseAdapter
    private lateinit var headerAdapter: EditCourseHeaderAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_course)

        //初始化ViewModel
        this.viewModel = ViewModelProvider(this)[EditCourseActivityViewModel::class.java]

        ActivityUtility.setSystemUIAppearance(this)

        this.timeManagerApplication = application as TimeManagerApplication

        this.rootRecyclerView = findViewById(R.id.EditCourseActivity_RecyclerView)

        val courseWithClassTimes =
            intent.getSerializableExtra(CourseDetailActivity.INTENT_EXTRA_COURSE)
        if (courseWithClassTimes is CourseWithClassTimes) {
            //给ViewModel设定值
            this.viewModel.course = courseWithClassTimes.course

            if (!viewModel.classTimesInitialized) {
                viewModel.classTimes = ArrayList(courseWithClassTimes.classTimes)
            }
        } else {
            this.viewModel.course = Course()

            if (!viewModel.classTimesInitialized) {
                viewModel.classTimes = ArrayList()
                viewModel.classTimes.add(ClassTime())
            }
        }
        //保证ViewModel中的classTimes数组已经初始化
        viewModel.classTimesInitialized = true

        this.headerAdapter = EditCourseHeaderAdapter(viewModel.course ?: Course())
        this.adapter = EditCourseAdapter(
            viewModel.classTimes,
            timeManagerApplication.courseTable?.maxWeeks ?: ClassTime.MAX_STORAGE_SIZE
        )

        val linearLayoutManager = LinearLayoutManager(this)
        this.rootRecyclerView.layoutManager = linearLayoutManager
        this.rootRecyclerView.adapter = ConcatAdapter(headerAdapter, adapter)

        //从Application获取Database的引用
        this.courseDatabase = (application as TimeManagerApplication).getCourseDatabase()

        //浮动按钮的监听器
        val floatingActionButton: FloatingActionButton =
            findViewById(R.id.EditCourseActivity_PlusButton)
        floatingActionButton.setOnClickListener {
            val lastClassTime = viewModel.classTimes.lastOrNull()
            if (lastClassTime == null || !viewModel.copyFromPrevious) {
                viewModel.classTimes.add(ClassTime())
            } else {
                clearChildViewFocus()
                viewModel.classTimes.add(ClassTime(lastClassTime))
            }
            this.adapter.notifyItemInserted(adapter.itemCount - 1)

            //滚动到底部
            ActivityUtility.RecyclerScrollHelper.scrollToBottom(this.rootRecyclerView)
        }
        floatingActionButton.setOnLongClickListener {
            viewModel.copyFromPrevious = !viewModel.copyFromPrevious

            Toast.makeText(
                this,
                if (viewModel.copyFromPrevious) {
                    R.string.activity_EditCourse_CopyFromPrevious_True
                } else {
                    R.string.activity_EditCourse_CopyFromPrevious_False
                },
                Toast.LENGTH_SHORT
            ).show()

            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_course_edit_activity, menu)
        return true
    }

    /**
     * 菜单栏项目点击监听。实现保存功能。
     *
     * @param item 菜单项目
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.EditCourseActivity_Done) {
            runIfPermissionGranted(Manifest.permission.WRITE_CALENDAR, {
                //没有获得权限时。
                AlertDialog.Builder(this)
                    .setTitle(R.string.activity_WelcomeActivity_AskPermissionTitle)
                    .setMessage(R.string.activity_EditCourse_AskPermissionText)
                    .setNegativeButton(R.string.common_cancel) { dialog, _ ->
                        dialog.dismiss()
                    }.setPositiveButton(R.string.common_confirm) { dialog, _ ->
                        if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CALENDAR)) {
                            requestPermissions(
                                arrayOf(Manifest.permission.WRITE_CALENDAR),
                                SAVING_REQUEST_CODE
                            )
                        } else {
                            jumpToSystemPermissionSettings()
                        }
                        dialog.dismiss()
                    }
            }) {
                clearChildViewFocus()
                viewModel.viewModelScope.launch {
                    viewModel.saveData(this@EditCourseActivity)
                }
            }
        } else if (item.itemId == android.R.id.home) {
            //按下左上角返回箭头的逻辑
            this.onBackPressed()
        }
        return true
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this).setTitle(R.string.activity_EditCourse_UnsavedAlertTitle)
            //确定按键
            .setPositiveButton(R.string.common_confirm) { dialog, _ ->
                dialog?.dismiss()
                this.finish()
            }
            //取消按键
            .setNegativeButton(R.string.common_cancel) { dialog, _ ->
                dialog?.dismiss()
            }.create().show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == SAVING_REQUEST_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                viewModel.viewModelScope.launch {
                    viewModel.saveData(this@EditCourseActivity)
                }
            }
        } else super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**
     * 移除一个[ClassTime]。
     *
     * @param classTime 要移除的对象。
     * @return 如果成功移除则返回true，否则false。
     */
    internal fun removeClassTime(classTime: ClassTime): Boolean =
        if (viewModel.classTimes.size < 2) {
            Toast.makeText(
                this,
                getString(R.string.activity_EditCourse_NoMoreClassTimeObjectToast),
                Toast.LENGTH_SHORT
            ).show()
            false
        } else if (!viewModel.classTimes.contains(classTime)) {
            false
        } else {
            //滚动到被删除项的前一个
            val index = viewModel.classTimes.indexOf(classTime)
            if (index > -1) {
                viewModel.classTimes.remove(classTime)
                this.adapter.notifyItemRemoved(index)
                if (classTime.id != null) {
                    //当id为空时则说明该对象还未插入数据库
                    viewModel.removedClassTimes.add(classTime)
                }
            }

            true
        }

    /**
     * 清除所有输入框的焦点，迫使数据保存。
     */
    private fun clearChildViewFocus() {
        rootRecyclerView.clearFocus()
    }
}