package cn.unscientificjszhai.timemanager.ui.settings

import android.Manifest
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.unscientificjszhai.timemanager.R
import cn.unscientificjszhai.timemanager.TimeManagerApplication
import cn.unscientificjszhai.timemanager.data.tables.CourseTable
import cn.unscientificjszhai.timemanager.features.calendar.CalendarOperator
import cn.unscientificjszhai.timemanager.ui.ActivityUtility
import cn.unscientificjszhai.timemanager.ui.ActivityUtility.runIfPermissionGranted
import cn.unscientificjszhai.timemanager.ui.CalendarOperatorActivity
import cn.unscientificjszhai.timemanager.ui.RecyclerViewWithContextMenu
import com.google.android.material.textfield.TextInputEditText
import kotlin.concurrent.thread

/**
 * 当前课程表的选择器。用于添加和选择当前的课程表。
 */
class CurrentTableSelectorActivity : CalendarOperatorActivity() {

    private lateinit var timeManagerApplication: TimeManagerApplication

    private lateinit var viewModel: CurrentTableSelectorActivityViewModel

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CurrentTableSelectorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_table_selector)
        ActivityUtility.setSystemUIAppearance(this)
        this.timeManagerApplication = application as TimeManagerApplication

        this.viewModel =
            ViewModelProvider(
                this,
                CurrentTableSelectorActivityViewModel.Factory(
                    timeManagerApplication.getCourseTableDatabase().courseTableDao()
                )
            ).get(CurrentTableSelectorActivityViewModel::class.java)

        this.recyclerView = findViewById(R.id.SettingsActivity_RecyclerView)
        registerForContextMenu(recyclerView)

        //初始化RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        this.adapter = CurrentTableSelectorAdapter(timeManagerApplication.nowTableID, ::setTable)
        recyclerView.adapter = this.adapter

        //注册监听器
        this.viewModel.tableList.observe(this) {
            this.adapter.submitList(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_current_table_selector_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.CurrentTableSelector_Add -> {
                val root: LinearLayout =
                    View.inflate(this, R.layout.dialog_new_table, null) as LinearLayout
                val editText = root.findViewById<TextInputEditText>(R.id.NewTableDialog_EditText)
                val checkBox = root.findViewById<CheckBox>(R.id.NewTableDialog_CheckBox)
                editText.setHint(R.string.activity_Welcome_PromptText)
                editText.imeOptions = EditorInfo.IME_ACTION_DONE
                checkBox.isChecked = true

                AlertDialog.Builder(this)
                    .setTitle(R.string.activity_CurrentTableSelector_AddTableDialogTitle)
                    .setView(root)
                    .setNegativeButton(R.string.common_cancel) { dialog, _ ->
                        dialog?.dismiss()
                    }.setPositiveButton(R.string.common_confirm) { dialog, _ ->
                        //从当前选中的表中复制上课时间信息
                        val currentTable by timeManagerApplication

                        val courseTable =
                            if (checkBox.isChecked) {
                                CourseTable(editText.text.toString(), currentTable.timeTable)
                            } else {
                                CourseTable(editText.text.toString())
                            }
                        if (courseTable.name.isBlank()) {
                            //为空时填入默认课程表名
                            courseTable.name = getString(R.string.activity_Welcome_EditTextHint)
                        }

                        runIfPermissionGranted(Manifest.permission.WRITE_CALENDAR, {
                            showPermissionDeniedDialog()
                            dialog.dismiss()
                        }) {
                            thread(start = true) {
                                CalendarOperator.createCalendarTable(this, courseTable)

                                val dao =
                                    timeManagerApplication.getCourseTableDatabase().courseTableDao()
                                val id = dao.insertCourseTable(courseTable)
                                timeManagerApplication.updateTableID(id)

                                runOnUiThread {
                                    dialog.dismiss()
                                    this.finish()
                                }
                            }
                        }
                    }.show()
                return true
            }
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.context_current_table_selector_long_press, menu)
        if (v is RecyclerView && menuInfo is RecyclerViewWithContextMenu.PositionMenuInfo) {
            val courseTable = viewModel.tableList.value!![menuInfo.position]
            menu?.setHeaderTitle(courseTable.name)
        } else {
            menu?.close()
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo
        val nowTableId = timeManagerApplication.nowTableID
        return if (info is RecyclerViewWithContextMenu.PositionMenuInfo) {
            val courseTable = viewModel.tableList.value!![info.position]
            when (item.itemId) {
                R.id.CurrentTableSelector_RenameTable -> {
                    val root: FrameLayout =
                        View.inflate(this, R.layout.dialog_input, null) as FrameLayout
                    val editText = root.findViewById<TextInputEditText>(R.id.InputDialog_EditText)
                    editText.setHint(R.string.activity_Welcome_PromptText)
                    editText.setText(courseTable.name)

                    AlertDialog.Builder(this)
                        .setTitle(R.string.activity_CurrentTableSelector_ContextMenu_Edit)
                        .setView(root)
                        .setNegativeButton(R.string.common_cancel) { dialog, _ ->
                            dialog?.dismiss()
                        }.setPositiveButton(R.string.common_confirm) { dialog, _ ->

                            courseTable.name = editText.text.toString()

                            runIfPermissionGranted(Manifest.permission.WRITE_CALENDAR, {
                                showPermissionDeniedDialog()
                            }) {
                                thread(start = true) {
                                    CalendarOperator.updateCalendarTable(this, courseTable)

                                    val dao =
                                        timeManagerApplication.getCourseTableDatabase()
                                            .courseTableDao()
                                    dao.updateCourseTable(courseTable)
                                    courseTable.id?.let {
                                        timeManagerApplication.updateTableID(it)
                                    }
                                    runOnUiThread {
                                        this@CurrentTableSelectorActivity.adapter.submitList(
                                            viewModel.tableList.value!!
                                        )
                                    }
                                }
                            }
                            dialog.dismiss()
                        }.show()
                    true
                }

                R.id.CurrentTableSelector_DeleteTable -> {
                    //只有当要删除的课程表未被选中的时候才能删除。
                    if (courseTable.id != nowTableId) {
                        //弹出删除提示
                        AlertDialog.Builder(this)
                            .setTitle(R.string.activity_CurrentTableSelector_DeletingTableDialogTitle)
                            .setNegativeButton(R.string.common_cancel) { dialog, _ ->
                                dialog?.dismiss()
                            }.setPositiveButton(R.string.common_confirm) { dialog, _ ->
                                adapter.setTable(courseTable, true)
                                dialog.dismiss()
                            }.create().show()
                    } else {
                        Toast.makeText(
                            this,
                            R.string.activity_CurrentTableSelector_DeletingCurrentTableToast,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    true
                }

                else -> super.onContextItemSelected(item)
            }
        } else {
            super.onContextItemSelected(item)
        }
    }

    /**
     * 展示权限被拒绝的提示信息。
     */
    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.activity_WelcomeActivity_AskPermissionTitle)
            .setMessage(R.string.activity_CurrentTableSelector_PermissionDeniedDialogMessage)
            .setPositiveButton(R.string.common_confirm) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    /**
     * 设置课程表的方法。
     *
     * @param courseTable 要操作的课程表。
     * @param isDelete True则删除此表，False则将此表设置为当前选中的表。
     */
    private fun setTable(courseTable: CourseTable, isDelete: Boolean) {
        if (isDelete) {
            //删除CourseTable的逻辑。
            if (courseTable.id != this.timeManagerApplication.nowTableID) {
                runIfPermissionGranted(Manifest.permission.WRITE_CALENDAR, {
                    showPermissionDeniedDialog()
                }) {
                    thread(start = true) {
                        CalendarOperator.deleteCalendarTable(this, courseTable)

                        val dao =
                            timeManagerApplication.getCourseTableDatabase()
                                .courseTableDao()
                        dao.deleteCourseTable(courseTable)
                        deleteDatabase("table${courseTable.id}.db")
                    }
                }
            }
        } else {
            //选中CourseTable的逻辑。
            try {
                val id: Long = courseTable.id!!
                if (id != timeManagerApplication.nowTableID) {
                    timeManagerApplication.updateTableID(id)
                } else {
                    return
                }
            } catch (e: NullPointerException) {
                Toast.makeText(
                    this,
                    R.string.activity_CurrentTableSelector_TableNotFound,
                    Toast.LENGTH_SHORT
                ).show()
                return
            } finally {
                finish()
            }
        }
    }
}