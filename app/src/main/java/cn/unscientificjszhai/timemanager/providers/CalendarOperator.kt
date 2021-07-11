package cn.unscientificjszhai.timemanager.providers

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.provider.CalendarContract
import androidx.annotation.WorkerThread
import cn.unscientificjszhai.timemanager.data.tables.CourseTable
import java.util.*
import kotlin.random.Random

/**
 * 日历操作工具对象。所有对日历的操作（表的层面上）都在这里完成。日历将被写入系统日历提供程序中，并和此应用的账户关联。
 * 这样可以使本应用被卸载时，系统自动删除本应用关联的所有日历。
 *
 * @see EmptyAuthenticator
 * @see EventsOperator
 */
object CalendarOperator {

    //测试用方法
    fun createCalendar(context: Context): Long {
        val timeZone = TimeZone.getDefault()
        val value = ContentValues()
        value.put(CalendarContract.Calendars.NAME, "Test")
        value.put(CalendarContract.Calendars.ACCOUNT_NAME, EmptyAuthenticator.ACCOUNT_NAME)
        value.put(
            CalendarContract.Calendars.ACCOUNT_TYPE,
            EmptyAuthenticator.ACCOUNT_TYPE
        )
        value.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, "myDisplayName")
        value.put(CalendarContract.Calendars.VISIBLE, 1)
        value.put(CalendarContract.Calendars.CALENDAR_COLOR, Color.BLUE)
        value.put(CalendarContract.Calendars.SYNC_EVENTS, 1)
        value.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, timeZone.id)
        value.put(CalendarContract.Calendars.OWNER_ACCOUNT, EmptyAuthenticator.ACCOUNT_NAME)
        value.put(CalendarContract.Calendars.CAN_ORGANIZER_RESPOND, 0)

        val uri = context.contentResolver.insert(
            CalendarContract.Calendars.CONTENT_URI.asSyncAdapter(
                "TimeManager",
                "cn.unscientificjszhai.timemanager.calendar"
            ), value
        )

        return if (uri == null) {
            -1
        } else {
            ContentUris.parseId(uri)
        }
    }

    /**
     * 为目标课程表创建一个日历表。同时会给CourseTable的成员变量赋值，但不会保存到数据库。
     * 应该异步调用此方法。
     * 调用时需要注意，此方法还会修改CourseTable中的数据且未保存。需要手动保存。
     *
     * @param context 插入操作的上下文。
     * @param courseTable 要创建日历表的CourseTable
     * @return 插入后的ID,插入失败则返回空。
     */
    @WorkerThread
    fun createCalendarTable(context: Context, courseTable: CourseTable): Long? {
        val timeZone = TimeZone.getDefault()
        val value = ContentValues()
        value.put(CalendarContract.Calendars.NAME, courseTable.getCalendarTableName())
        value.put(CalendarContract.Calendars.ACCOUNT_NAME, EmptyAuthenticator.ACCOUNT_NAME)
        value.put(
            CalendarContract.Calendars.ACCOUNT_TYPE,
            EmptyAuthenticator.ACCOUNT_TYPE
        )
        value.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, courseTable.name)
        value.put(CalendarContract.Calendars.VISIBLE, 1)
        value.put(CalendarContract.Calendars.CALENDAR_COLOR, getRandomColor())
        value.put(CalendarContract.Calendars.SYNC_EVENTS, 1)
        value.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, timeZone.id)
        value.put(CalendarContract.Calendars.OWNER_ACCOUNT, EmptyAuthenticator.ACCOUNT_NAME)
        value.put(CalendarContract.Calendars.CAN_ORGANIZER_RESPOND, 0)

        val uri = context.contentResolver.insert(
            CalendarContract.Calendars.CONTENT_URI.asSyncAdapter(),
            value
        )

        val id = if (uri == null) {
            null
        } else {
            ContentUris.parseId(uri)
        }
        courseTable.calendarID = id//更新CalendarID
        return id
    }

    /**
     * 更新日历表的名称。
     * 更改例如上课时间之类的直接影响事件的属性时，应该调用[EventsOperator.updateAllEvents]方法更新。
     *
     * @param context 插入操作的上下文。
     * @param courseTable 要创建日历表的CourseTable。
     * @param updateTimeZone 是否更新时区设置。
     * @return 更改是否成功。
     */
    @WorkerThread
    fun updateCalendarTable(
        context: Context,
        courseTable: CourseTable,
        updateTimeZone: Boolean = true
    ): Boolean {
        val calendarID = courseTable.calendarID
        return if (calendarID == null) {
            false
        } else {
            val values = ContentValues().apply {
                if (updateTimeZone) {
                    val timeZone = TimeZone.getDefault()
                    put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, timeZone.id)
                }
                put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, courseTable.name)
            }
            val updateUri =
                ContentUris.withAppendedId(CalendarContract.Calendars.CONTENT_URI, calendarID)
            context.contentResolver
                .update(updateUri.asSyncAdapter(), values, null, null) != -1
        }
    }

    /**
     * 删除一个日历表。
     *
     * @param context 操作的上下文。
     * @param courseTable 要删除的日历表。
     * @return 是否删除成功。
     */
    @WorkerThread
    fun deleteCalendarTable(context: Context, courseTable: CourseTable): Boolean {
        val calendarID = courseTable.calendarID
        return if (calendarID == null) {
            false
        } else {
            val deleteUri =
                ContentUris.withAppendedId(CalendarContract.Calendars.CONTENT_URI, calendarID)
            context.contentResolver
                .delete(deleteUri.asSyncAdapter(), null, null) != -1
        }
    }

    /**
     * 删除全部日历表。
     * 一般用于清除全部数据后首次启动。
     *
     * @param context 操作的上下文。
     */
    @WorkerThread
    fun deleteAllTables(context: Context) {
        val eventProjection = arrayOf(CalendarContract.Calendars._ID)

        val uri = CalendarContract.Calendars.CONTENT_URI
        val selection = "((${CalendarContract.Calendars.ACCOUNT_NAME} = ?) AND (" +
                "${CalendarContract.Calendars.ACCOUNT_TYPE} = ?) AND (" +
                "${CalendarContract.Calendars.OWNER_ACCOUNT} = ?))"
        val selectionArgs = arrayOf(
            EmptyAuthenticator.ACCOUNT_NAME,
            EmptyAuthenticator.ACCOUNT_TYPE,
            EmptyAuthenticator.ACCOUNT_NAME
        )
        val cursor =
            context.contentResolver.query(uri, eventProjection, selection, selectionArgs, null)
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val calendarID = cursor.getLong(0)

                //删除日历表
                val deleteUri =
                    ContentUris.withAppendedId(CalendarContract.Calendars.CONTENT_URI, calendarID)
                context.contentResolver
                    .delete(deleteUri.asSyncAdapter(), null, null) != -1
            }
        }
        cursor?.close()
    }

    /**
     * CourseTable的固定获取CalendarID的方法。
     *
     * @return 用于在创建日历表的过程的名称，对应字段为[CalendarContract.Calendars.NAME]。
     */
    private fun CourseTable.getCalendarTableName() = "TimeManager${this.id}"

    /**
     * 将Uri包转成以SyncAdapter的形式访问日历提供程序，以获取更多权限。
     *
     * @param account 访问时使用的账号。
     * @param accountType 访问时使用的账号类型。
     * @return 包装好的Uri对象。
     */
    private fun Uri.asSyncAdapter(account: String, accountType: String) = this.buildUpon()
        .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
        .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, account)
        .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, accountType)
        .build()

    /**
     * 将Uri包转成以SyncAdapter的形式访问日历提供程序，以获取更多权限。使用应用唯一账户和账户类型包装。
     *
     * @return 包装好的Uri对象。
     */
    private fun Uri.asSyncAdapter() =
        this.asSyncAdapter(EmptyAuthenticator.ACCOUNT_NAME, EmptyAuthenticator.ACCOUNT_TYPE)

    /**
     * 为日历对象随机选取一个颜色。
     * 会从[Color.RED]，[Color.BLUE]，[Color.YELLOW]中选择一个。
     *
     * @return 随机选取的颜色
     */
    private fun getRandomColor() = when (Random.nextInt(2)) {
        0 -> Color.RED
        1 -> Color.BLUE
        else -> Color.YELLOW
    }
}