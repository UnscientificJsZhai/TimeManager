package cn.unscientificjszhai.timemanager.ui.main

import android.Manifest
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.unscientificjszhai.timemanager.R
import cn.unscientificjszhai.timemanager.TimeManagerApplication
import cn.unscientificjszhai.timemanager.data.CurrentTimeMarker
import cn.unscientificjszhai.timemanager.data.course.CourseWithClassTimes
import cn.unscientificjszhai.timemanager.data.database.CourseDatabase
import cn.unscientificjszhai.timemanager.ui.WelcomeActivity
import cn.unscientificjszhai.timemanager.ui.editor.EditCourseActivity
import cn.unscientificjszhai.timemanager.ui.others.ActivityUtility
import cn.unscientificjszhai.timemanager.ui.others.ActivityUtility.jumpToSystemPermissionSettings
import cn.unscientificjszhai.timemanager.ui.others.ActivityUtility.runIfPermissionGranted
import cn.unscientificjszhai.timemanager.ui.others.RecyclerViewWithContextMenu
import cn.unscientificjszhai.timemanager.ui.settings.SettingsActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.*
import kotlin.reflect.KProperty

/**
 * 主页Activity。其中的RecyclerView的Adapter参见[CourseAdapter]。
 *
 * @see CourseAdapter
 * @see MainActivityViewModel
 */
class MainActivity : AppCompatActivity(), CurrentTimeMarker.Getter {

    companion object {

        /**
         * 发送课程表变更的广播的Action。
         */
        const val COURSE_DATABASE_CHANGE_ACTION =
            "cn.unscientificjszhai.timemanager.COURSE_DATABASE_CHANGE"

        /**
         * 确定是否显示帮助的Key，在SharedPreference：[TimeManagerApplication.INITIAL]中查找。
         */
        const val SHOW_GUIDE_KEY = "mainActivityGuideShowed"
    }

    private lateinit var timeManagerApplication: TimeManagerApplication
    private val currentTimeMarker: CurrentTimeMarker by lazy {
        timeManagerApplication.courseTable?.let {
            CurrentTimeMarker(it.startDate)
        } ?: CurrentTimeMarker(Calendar.getInstance())
    }

    private lateinit var viewModel: MainActivityViewModel

    private lateinit var rootView: CoordinatorLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewAdapter: CourseAdapter

    private lateinit var progressBar: ProgressBar

    private lateinit var courseDatabase: CourseDatabase

    /**
     * 用于处理当前课程表更改事件的广播接收器。
     */
    inner class DatabaseChangeReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            if (context is MainActivity) {
                context.courseDatabase = context.timeManagerApplication.getCourseDatabase()
                context.viewModel.courseList.removeObservers(this@MainActivity)
                context.viewModel.courseList = context.courseDatabase.courseDao().getLiveCourses()
                context.viewModel.courseList.observe(this@MainActivity) { courseList ->
                    bindData(courseList)
                }

                //更新NowTimeTagger
                val courseTable by context.timeManagerApplication
                context.currentTimeMarker.setStartDate(courseTable.startDate)
                updateActionBarLabel()
            }
        }
    }

    /**
     * 处理系统日期变更的广播接收器。
     */
    inner class DateChangeReceiver : BroadcastReceiver() {

        /**
         * 上次收到时间变化广播的时间。
         */
        private var timeBeforeUpdate = Calendar.getInstance()

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_TIME_TICK) {
                //检测日期是否发生变化
                val timeRightNow = Calendar.getInstance()
                if (this.timeBeforeUpdate.get(Calendar.DAY_OF_YEAR) != timeRightNow.get(Calendar.DAY_OF_YEAR) ||
                    this.timeBeforeUpdate.get(Calendar.YEAR) != timeRightNow.get(Calendar.YEAR)
                ) {
                    if (context is MainActivity) {
                        //如果为只显示今天则更新数据集
                        bindData(viewModel.courseList.value ?: ArrayList())
                    }
                }

                //最后更新新的当前时间
                this.timeBeforeUpdate = timeRightNow
            }
        }
    }

    private lateinit var databaseChangeReceiver: DatabaseChangeReceiver
    private lateinit var dateChangeReceiver: DateChangeReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.timeManagerApplication = application as TimeManagerApplication
        //与初次启动判定有关
        if (this.timeManagerApplication.nowTableID < 0) {
            Toast.makeText(this, R.string.activity_Main_NoTableFound, Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        //设置SystemUI颜色
        ActivityUtility.setSystemUIAppearance(this)

        this.courseDatabase = this.timeManagerApplication.getCourseDatabase()

        //初始化ViewModel
        this.viewModel =
            ViewModelProvider(
                this,
                MainActivityViewModel.Factory(courseDatabase.courseDao())
            )[MainActivityViewModel::class.java]

        this.progressBar = findViewById(R.id.MainActivity_ProgressBar)
        this.rootView = findViewById(R.id.MainActivity_RootView)

        //初始化列表
        this.recyclerView = findViewById(R.id.MainActivity_RootRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        this.recyclerViewAdapter = CourseAdapter(this)
        recyclerView.adapter = this.recyclerViewAdapter
        registerForContextMenu(recyclerView)

        //监听LiveData变更
        viewModel.courseList.observe(this) { courseList ->
            bindData(courseList)
        }

        findViewById<FloatingActionButton>(R.id.MainActivity_FloatingActionButton)
            .setOnClickListener {
                EditCourseActivity.startThisActivity(this)
            }

        //监听数据库变更
        this.databaseChangeReceiver = DatabaseChangeReceiver()
        registerReceiver(this.databaseChangeReceiver, IntentFilter().apply {
            addAction(COURSE_DATABASE_CHANGE_ACTION)
        })

        //监听日期变更
        this.dateChangeReceiver = DateChangeReceiver()
        registerReceiver(this.dateChangeReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
        })

        //首次打开则显示帮助
        val sharedPreferences =
            getSharedPreferences(TimeManagerApplication.INITIAL, Context.MODE_PRIVATE)
        if (!sharedPreferences.getBoolean(SHOW_GUIDE_KEY, false)) {
            Toast.makeText(this, R.string.activity_Main_GuideToast, Toast.LENGTH_LONG).show()
            sharedPreferences.edit().putBoolean(SHOW_GUIDE_KEY, true).apply()
        }
    }

    override fun onStart() {
        super.onStart()
        //更新ActionBar的内容
        updateActionBarLabel()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main_activity, menu)
        return true
    }

    override fun onMenuOpened(featureId: Int, menu: Menu): Boolean {
        menu.findItem(R.id.MainActivity_ShowTodayOnly).isChecked = viewModel.showTodayOnly
        return super.onMenuOpened(featureId, menu)
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.context_main, menu)
        if (v is RecyclerView && menuInfo is RecyclerViewWithContextMenu.PositionMenuInfo) {
            try {
                val courseWithClassTimes =
                    (recyclerView.adapter as CourseAdapter).currentList[menuInfo.position]
                menu?.setHeaderTitle(courseWithClassTimes.course.title)
            } catch (e: NullPointerException) {
                menu?.close()
            }
        } else {
            menu?.close()
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo
        if (info is RecyclerViewWithContextMenu.PositionMenuInfo) {
            val courseWithClassTimes =
                (recyclerView.adapter as CourseAdapter).currentList[info.position]

            when (item.itemId) {
                R.id.MainActivity_Edit -> {
                    if (courseWithClassTimes != null) {
                        EditCourseActivity.startThisActivity(this, courseWithClassTimes)
                    } else {
                        Toast.makeText(
                            this,
                            R.string.activity_CourseDetail_DataError,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                R.id.MainActivity_Delete -> {
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
                                    }
                                    .setPositiveButton(R.string.common_confirm) { permissionDialog, _ ->
                                        //同意授予日历权限，跳转到系统设置进行授权。
                                        this@MainActivity.jumpToSystemPermissionSettings()
                                        permissionDialog.dismiss()
                                    }
                            }) {
                                if (courseWithClassTimes != null) {
                                    viewModel.viewModelScope.launch {
                                        MainActivityViewModel.deleteCourse(
                                            this@MainActivity,
                                            courseWithClassTimes
                                        )
                                        val snackBar = Snackbar.make(
                                            rootView,
                                            R.string.activity_Main_DeletedMessage,
                                            Snackbar.LENGTH_LONG
                                        )
                                        snackBar.setAction(R.string.common_undo) {
                                            viewModel.viewModelScope.launch {
                                                MainActivityViewModel.undoDeleteCourse(
                                                    this@MainActivity,
                                                    courseWithClassTimes
                                                )
                                            }
                                        }
                                        snackBar.show()
                                    }
                                } else {
                                    Toast.makeText(
                                        this,
                                        R.string.activity_CourseDetail_DataError,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                dialog.dismiss()
                            }
                        }.create().show()
                }
            }
        }
        return super.onContextItemSelected(item)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.MainActivity_Settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            R.id.MainActivity_ShowTodayOnly -> {
                viewModel.showTodayOnly = !item.isChecked
                bindData(viewModel.courseList.value ?: ArrayList())
                updateActionBarLabel()
            }
            R.id.MainActivity_JumpToCalendar -> {
                Calendar.getInstance().timeInMillis
                val startCalendarIntent = Intent(Intent.ACTION_VIEW).apply {
                    data =
                        Uri.parse("content://com.android.calendar/time/${Calendar.getInstance().timeInMillis}")
                }
                startActivity(startCalendarIntent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        unregisterReceiver(this.databaseChangeReceiver)
        unregisterReceiver(this.dateChangeReceiver)
        super.onDestroy()
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): CurrentTimeMarker {
        return this.currentTimeMarker
    }

    /**
     * 更新列表数据的方法。会判断是否仅显示今天的课程，然后传入正确的列表给[CourseAdapter]。
     *
     * @param courseList 完整的Course列表。
     */
    private fun bindData(courseList: List<CourseWithClassTimes>) {
        progressBar.visibility = View.GONE
        if (viewModel.showTodayOnly) {
            recyclerViewAdapter.submitList(this.currentTimeMarker.getTodayCourseList(courseList))
        } else {
            recyclerViewAdapter.submitList(courseList)
        }
    }

    /**
     * 查询是否只显示今天，提供给Adapter使用。
     *
     * @return 是否只显示今天。
     */
    internal fun isShowTodayOnly(): Boolean = this.viewModel.showTodayOnly

    /**
     * 更新ActionBar的标题。
     */
    private fun updateActionBarLabel() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val option = sharedPreferences.getString("showOnMainActivity", "table")
        val stringBuilder = StringBuilder()
        val courseTable by timeManagerApplication
        when (option) {
            "table" -> stringBuilder.append(courseTable.name)
            "today" -> {

                /**
                 * 用来获取当前是星期几的局部函数。
                 *
                 * @return 表示星期几的字符串，可以直接用于显示。
                 */
                fun dayOfWeek(): String = getString(
                    when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
                        Calendar.MONDAY -> R.string.data_Week1
                        Calendar.TUESDAY -> R.string.data_Week2
                        Calendar.WEDNESDAY -> R.string.data_Week3
                        Calendar.THURSDAY -> R.string.data_Week4
                        Calendar.FRIDAY -> R.string.data_Week5
                        Calendar.SATURDAY -> R.string.data_Week6
                        else -> R.string.data_Week0
                    }
                )

                stringBuilder.append(
                    getString(R.string.view_ClassTimeEdit_WeekItem_ForKotlin)
                        .format(currentTimeMarker.getWeekNumber())
                )
                    .append(" ")
                    .append(dayOfWeek())
                    .append(" ")
                    .append(
                        getString(
                            if (viewModel.showTodayOnly) {
                                R.string.activity_Main_ActionBarLabel_TodayOnly
                            } else {
                                R.string.activity_Main_ActionBarLabel_All
                            }
                        )
                    )
            }
        }
        supportActionBar?.let { actionBar ->
            actionBar.title = stringBuilder.toString()
        }
    }
}