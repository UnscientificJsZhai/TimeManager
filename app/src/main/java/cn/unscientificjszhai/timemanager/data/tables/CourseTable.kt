package cn.unscientificjszhai.timemanager.data.tables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.*

/**
 * 数据类，用于封装课程表信息。
 *
 * @param id 课程表的ID。
 * @param name 课程表的名称。
 * @param classesPerDay 每天的课程数。
 * @param maxWeeks 学期教学周数。
 * @param timeTable 学期上课时间安排表。
 * @param startDate 学期开始日。
 */
@Entity(tableName = CourseTable.TABLE_NAME)
@TypeConverters(TimeTableTypeConverter::class)
data class CourseTable(
    @PrimaryKey(autoGenerate = true) val id: Long?,
    var name: String,
    @ColumnInfo(name = "class_per_day") var classesPerDay: Int,
    @ColumnInfo(name = "max_weeks") var maxWeeks: Int,
    @ColumnInfo(name = "time_table") var timeTable: Array<String>,
    @ColumnInfo(name = "start_date") var startDate: Calendar,
    @ColumnInfo(name = "calendar_id") var calendarID: Long?
) {

    companion object {

        /**
         * 表的名字。
         */
        const val TABLE_NAME = "course_table"

        const val MAX_CLASS_PER_DAY = 15

        const val DEFAULT_CLASS_PER_DAY = 13

        const val DEFAULT_MAX_WEEKS = 18

        val DEFAULT_TIME_STRING = arrayOf(
            "08300915",
            "09251010",
            "10301115",
            "11251210",
            "12201305",
            "13051350",
            "14001445",
            "14551540",
            "16001645",
            "16551740",
            "19001945",
            "19552040",
            "20402125",
            "00000000",
            "00000000"
        )
    }

    constructor(name: String) : this(
        null,
        name,
        DEFAULT_CLASS_PER_DAY,
        DEFAULT_MAX_WEEKS,
        DEFAULT_TIME_STRING,
        Calendar.getInstance(),
        null
    )

    /**
     * 获得指定节次的上下课时间的方法。
     *
     * @param which 节次。第一节课即为1。
     */
    @Deprecated("暂时用不到的方法")
    fun getTime(which: Int): FormattedTime {
        if (which > this.classesPerDay || which < 1) {
            throw IndexOutOfBoundsException()
        } else {
            return FormattedTime(timeTable[which - 1])
        }
    }

    override fun toString() = "id: $id\nname: $name"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CourseTable

        if (id != other.id) return false
        if (name != other.name) return false
        if (classesPerDay != other.classesPerDay) return false
        if (!timeTable.contentEquals(other.timeTable)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + name.hashCode()
        result = 31 * result + classesPerDay
        result = 31 * result + timeTable.contentHashCode()
        return result
    }

    fun getStartDateInSunday(): Calendar {
        var startDate = this.startDate.clone() as Calendar
        while (startDate.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            startDate = startDate.yesterday()
        }
        return startDate
    }

    private fun Calendar.yesterday(): Calendar {
        if (this.get(Calendar.DATE) == 1) {
            if (this.get(Calendar.MONTH) == Calendar.JANUARY) {
                this.set(this.get(Calendar.YEAR - 1), Calendar.DECEMBER, 31)
            } else {
                val month = this.get(Calendar.MONTH) - 1
                this.set(Calendar.MONTH, month)
                this.set(
                    Calendar.DATE, when (month) {
                        1, 3, 5, 7, 8, 10, 12 -> 31
                        4, 6, 9, 11 -> 30
                        else -> {
                            val year = this.get(Calendar.YEAR)
                            if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) {
                                29
                            } else {
                                28
                            }
                        }
                    }
                )
            }
        } else {
            this.set(Calendar.DATE, this.get(Calendar.DATE) - 1)
        }
        return this
    }
}