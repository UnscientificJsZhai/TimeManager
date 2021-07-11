package cn.unscientificjszhai.timemanager.data

import cn.unscientificjszhai.timemanager.data.course.ClassTime
import cn.unscientificjszhai.timemanager.data.course.CourseWithClassTimes
import java.util.*
import kotlin.reflect.KProperty

/**
 * 标注现在所处时间的标记类，也提供一些基于当前时间的实用功能，比如排序。
 *
 * @param startDate 学期开始日，Calendar对象。
 */
class NowTimeTagger(private var startDate: Calendar) {

    /**
     * 为[NowTimeTagger]提供属性委托功能。
     */
    interface Getter {

        operator fun getValue(thisRef: Any?, property: KProperty<*>): NowTimeTagger
    }

    /**
     * 用于排序比较多个已经确认在同一天有效的ClassTime对象谁先谁后的比较用包装类。
     *
     * @param classTime 比较的对象。
     * @param courseWithClassTimes 比较对象对应的Course整体。
     */
    private inner class ClassTimeCompareOperator(
        val classTime: ClassTime,
        val courseWithClassTimes: CourseWithClassTimes
    ) : Comparable<ClassTimeCompareOperator> {

        override operator fun compareTo(other: ClassTimeCompareOperator): Int {
            val thisNumerical = classTime.start * 100 + classTime.end
            val otherNumerical = other.classTime.start * 100 + other.classTime.end
            return thisNumerical - otherNumerical
        }
    }

    /**
     * 计算当前日期是学期的第几周。
     * 会获取当前时间作为第二个参数进行计算。
     *
     * @return 如果一个星期的星期二被设定为这个学期的开始日，那么这个星期的星期一到星期六都返回1.
     */
    fun getWeekNumber(): Int {
        val nowDate = Calendar.getInstance()
        return 1 + (getDateDistance(
            this.startDate,
            nowDate
        ) + startDate.get(Calendar.DAY_OF_WEEK) - 1) / 7
    }

    /**
     * 计算两天的日期差距。
     *
     * @param from 开始日期。
     * @param to 结束日期。
     * @return 日期差，整数。
     */
    private fun getDateDistance(from: Calendar, to: Calendar): Int {
        val day1 = from.get(Calendar.DAY_OF_YEAR)
        val day2 = to.get(Calendar.DAY_OF_YEAR)

        val year1 = from.get(Calendar.YEAR)
        val year2 = to.get(Calendar.YEAR)

        return if (year1 == year2) {
            day2 - day1
        } else {
            var timeDistance = 0
            for (i in year1 until year2) {
                timeDistance += if (i % 4 == 0 && i % 100 != 0 || i % 400 == 0) {
                    366
                } else {
                    365
                }
            }
            timeDistance + day2 - day1
        }
    }

    /**
     * 获取今天有课的课程列表。
     *
     * @param originalList 完整的列表。
     * @return 新的列表，只包括今天上的课程。
     */
    fun getTodayCourseList(originalList: List<CourseWithClassTimes>): ArrayList<CourseWithClassTimes> {
        val weekNumber = this.getWeekNumber()
        val nowDate = Calendar.getInstance()
        val classTimes = ArrayList<ClassTimeCompareOperator>()
        val newList = ArrayList<CourseWithClassTimes>()

        for (courseWithClassTimes in originalList) {
            for (classTime in courseWithClassTimes.classTimes) {
                if (classTime.getWeekData(weekNumber) &&
                    (classTime.whichDay + 1) == nowDate.get(Calendar.DAY_OF_WEEK)
                ) {
                    classTimes.add(ClassTimeCompareOperator(classTime, courseWithClassTimes))
                }
            }
        }

        classTimes.sort()
        classTimes.forEach {
            if (!newList.contains(it.courseWithClassTimes)) {
                newList.add(it.courseWithClassTimes)
            }
        }

        return newList
    }
}