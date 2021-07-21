package cn.unscientificjszhai.timemanager.ui.settings

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import cn.unscientificjszhai.timemanager.R
import cn.unscientificjszhai.timemanager.TimeManagerApplication
import cn.unscientificjszhai.timemanager.data.tables.CourseTable
import cn.unscientificjszhai.timemanager.features.calendar.CalendarOperator
import cn.unscientificjszhai.timemanager.features.calendar.EventsOperator
import cn.unscientificjszhai.timemanager.ui.ProgressDialog
import java.util.*
import kotlin.concurrent.thread

/**
 * 供设置Activity使用的Fragment。只能用于设置Activity。
 *
 * @see SettingsActivity
 */
internal class SettingsFragment(private val dataStore: SettingsDataStore) :
    PreferenceFragmentCompat(),
    Preference.SummaryProvider<Preference> {

    companion object {

        /**
         * Preference当前课程表的Key。
         */
        const val CURRENT_TABLE_KEY = "current"

        /**
         * Preference总周数的Key。
         */
        const val MAX_WEEK_KEY = "weeks"

        /**
         * Preference每日上课节数的Key。
         */
        const val CLASSES_PER_DAY_KEY = "classesPerDay"

        /**
         * Preference学期开始日的Key。
         */
        const val START_DATE_KEY = "startDate"

        const val UPDATE_CALENDAR_KEY = "createCalendar"

        const val CALENDAR_COLOR_KEY = "calendarColor"
    }

    private var currentTablePreference: Preference? = null
    private var howManyWeeksPreference: EditTextPreference? = null
    private var classesPerDayPreference: EditTextPreference? = null
    private var startDatePreference: Preference? = null

    private var updateCalendarPreference: Preference? = null
    private var calendarColorPreference: ListPreference? = null

    private var saveBackupPreference: Preference? = null
    private var importBackupPreference: Preference? = null
    private var exportIcsPreference:Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey)

        //当前课程表的设置项
        this.currentTablePreference = findPreference(CURRENT_TABLE_KEY)
        currentTablePreference?.summaryProvider = this

        //学期周数的设置项
        this.howManyWeeksPreference = findPreference(MAX_WEEK_KEY)
        howManyWeeksPreference?.preferenceDataStore = dataStore
        howManyWeeksPreference?.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER
            val content = dataStore.nowCourseTable.maxWeeks.toString()
            editText.setText(content)
            editText.setSelection(content.length)
        }
        howManyWeeksPreference?.summaryProvider = this

        //每日上课节数的设置项
        this.classesPerDayPreference = findPreference(CLASSES_PER_DAY_KEY)
        classesPerDayPreference?.preferenceDataStore = dataStore
        classesPerDayPreference?.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER
            val content = dataStore.nowCourseTable.classesPerDay.toString()
            editText.setText(content)
            editText.setSelection(content.length)
        }
        classesPerDayPreference?.summaryProvider = this

        //学期开始日的设置项
        this.startDatePreference = findPreference(START_DATE_KEY)
        startDatePreference?.setOnPreferenceClickListener {
            val parentActivity = activity
            if (parentActivity is SettingsActivity) {
                DatePickerDialog(
                    requireActivity(),
                    { _, year, month, dayOfMonth ->
                        dataStore.nowCourseTable.startDate.set(year, month, dayOfMonth)
                        dataStore.updateCourseTable()
                    },
                    dataStore.nowCourseTable.startDate.get(Calendar.YEAR),
                    dataStore.nowCourseTable.startDate.get(Calendar.MONTH),
                    dataStore.nowCourseTable.startDate.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
            true
        }
        startDatePreference?.summaryProvider = this

        this.updateCalendarPreference = findPreference(UPDATE_CALENDAR_KEY)
        updateCalendarPreference?.setOnPreferenceClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.preferences_UpdateCalendar_DialogTitle)
                .setMessage(R.string.preferences_UpdateCalendar_DialogMessage)
                .setNegativeButton(R.string.common_cancel) { dialog, _ ->
                    dialog.dismiss()
                }.setPositiveButton(R.string.common_confirm) { dialog, _ ->
                    val progressDialog = ProgressDialog(requireActivity())
                    progressDialog.show()
                    thread(start = true) {
                        //删除全部日历并重新创建。
                        val courseTable = dataStore.nowCourseTable
                        CalendarOperator.deleteCalendarTable(requireContext(), courseTable)
                        CalendarOperator.createCalendarTable(requireContext(), courseTable)
                        val application =
                            requireContext().applicationContext as TimeManagerApplication
                        val tableDao =
                            application.getCourseTableDatabase().courseTableDao()
                        tableDao.updateCourseTable(courseTable)
                        val courseDao = application.getCourseDatabase().courseDao()
                        courseDao.getCourses().run {
                            for (courseWithClassTimes in this) {
                                EventsOperator.addEvent(
                                    requireContext(),
                                    courseTable,
                                    courseWithClassTimes
                                )
                                courseDao.updateCourse(courseWithClassTimes.course)
                            }
                        }

                        //完成后关闭ProgressDialog。
                        progressDialog.postDismiss()
                    }
                    Toast.makeText(
                        requireContext(),
                        R.string.preferences_UpdateCalendar_Complete,
                        Toast.LENGTH_SHORT
                    ).show()
                    dialog.dismiss()
                }.show()
            true
        }

        this.calendarColorPreference = findPreference(CALENDAR_COLOR_KEY)
        calendarColorPreference?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue is String) {
                val color = newValue.toInt()
                thread(start = true) {
                    CalendarOperator.updateCalendarColor(requireContext(), color)
                }
            }
            true
        }

        this.saveBackupPreference = findPreference("backup")
        saveBackupPreference?.setOnPreferenceClickListener {
            (requireActivity() as SettingsActivity).saveBackup()
        }

        this.importBackupPreference = findPreference("import")
        importBackupPreference?.setOnPreferenceClickListener {
            (requireActivity() as SettingsActivity).importBackup()
        }

        this.exportIcsPreference = findPreference("ics")
        exportIcsPreference?.setOnPreferenceClickListener {
            (requireActivity() as SettingsActivity).exportIcs()
        }
    }

    /**
     * 宿主Activity用于通知CourseTable更新的方法。
     *
     * @param courseTable 更新后的CourseTable。
     */
    internal fun updateCourseTable(courseTable: CourseTable) {
        dataStore.nowCourseTable = courseTable
        //通过重设summaryProvider的方法更新Summary
        this.currentTablePreference?.summaryProvider = this
        this.howManyWeeksPreference?.summaryProvider = this
        this.classesPerDayPreference?.summaryProvider = this
        this.startDatePreference?.summaryProvider = this
    }

    override fun provideSummary(preference: Preference?) = when (preference) {
        this.currentTablePreference -> dataStore.nowCourseTable.name
        this.howManyWeeksPreference -> dataStore.nowCourseTable.maxWeeks.toString() +
                getString(R.string.preferences_CurrentTable_MaxWeeksSummary)
        this.classesPerDayPreference -> dataStore.nowCourseTable.classesPerDay.toString() +
                getString(R.string.preferences_CurrentTable_ClassesPerDaySummary)
        this.startDatePreference -> getString(R.string.preferences_CurrentTable_StartDateSummary).format(
            dataStore.nowCourseTable.startDate.get(Calendar.YEAR),
            dataStore.nowCourseTable.startDate.get(Calendar.MONTH) + 1,
            dataStore.nowCourseTable.startDate.get(Calendar.DATE)
        )
        else -> ""
    }
}