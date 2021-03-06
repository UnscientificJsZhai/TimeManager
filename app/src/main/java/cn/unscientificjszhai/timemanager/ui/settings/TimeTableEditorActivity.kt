package cn.unscientificjszhai.timemanager.ui.settings

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.unscientificjszhai.timemanager.R
import cn.unscientificjszhai.timemanager.TimeManagerApplication
import cn.unscientificjszhai.timemanager.data.tables.FormattedTime
import cn.unscientificjszhai.timemanager.util.setSystemUIAppearance
import kotlinx.coroutines.launch

/**
 * 修改上课时间的Activity。只会修改当前的CourseTable的属性。
 *
 * @author UnscientificJsZhai
 */
class TimeTableEditorActivity : AppCompatActivity() {

    private lateinit var timeManagerApplication: TimeManagerApplication

    private lateinit var viewModel: TimeTableEditorActivityViewModel

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TimeTableEditorAdapter
    private lateinit var headerAdapter: TimeTableHeaderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_table_editor)

        setSystemUIAppearance(this)

        this.viewModel = ViewModelProvider(this)[TimeTableEditorActivityViewModel::class.java]

        this.timeManagerApplication = application as TimeManagerApplication

        try {
            val courseTable = this.timeManagerApplication.courseTable!!
            viewModel.courseTable = courseTable
            this.viewModel.originTimeTable = courseTable.timeTable.typeConvert()
        } catch (e: NullPointerException) {
            Toast.makeText(this, R.string.activity_EditCourse_DataError, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        //初始化ViewModel中的数据
        viewModel.duration = FormattedTime(viewModel.courseTable.timeTable[0]).duration()

        this.adapter = TimeTableEditorAdapter(this.viewModel)
        this.headerAdapter = TimeTableHeaderAdapter(this.viewModel)
        this.recyclerView = findViewById(R.id.TimeTableEditorActivity_RecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ConcatAdapter(headerAdapter, adapter)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_time_table_editor_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        } else if (item.itemId == R.id.TimeTableEditorActivity_Done) {
            val id = viewModel.courseTable.id
            if (id != null && viewModel.courseTable.timeTable.typeConvert() != this.viewModel.originTimeTable) {
                viewModel.viewModelScope.launch {
                    viewModel.save(this@TimeTableEditorActivity, timeManagerApplication.useCalendar)
                    finish()
                }
            } else if (viewModel.courseTable.timeTable.typeConvert() != this.viewModel.originTimeTable) {
                Toast.makeText(this, R.string.activity_EditCourse_DataError, Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
        return true
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this).setTitle(R.string.activity_EditCourse_UnsavedAlertTitle)
            .setPositiveButton(R.string.common_confirm) { dialog, _ ->
                dialog?.dismiss()
                finish()
            }
            .setNegativeButton(R.string.common_cancel) { dialog, _ ->
                dialog?.dismiss()
            }.show()
    }

    /**
     * 将时间表转换成字符串形式，来帮助确定是否修改过时间表。
     *
     * @return 转换成的字符串，会保存在ViewModel中。
     */
    private fun Array<String>.typeConvert(): String {
        val converter by viewModel
        return converter.setTimeTable(this)
    }
}