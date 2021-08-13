package cn.unscientificjszhai.timemanager.features.parse

import cn.unscientificjszhai.timemanager.data.course.ClassTime
import cn.unscientificjszhai.timemanager.data.course.Course
import cn.unscientificjszhai.timemanager.data.course.CourseWithClassTimes
import com.github.unscientificjszhai.unscientificcourseparser.bean.data.ClassTime as SourceClassTime
import com.github.unscientificjszhai.unscientificcourseparser.bean.data.Course as SourceCourse

class ParserTypeConverter(private val coursesSource: List<SourceCourse>) :
    List<SourceCourse> by coursesSource {

    companion object {

        /**
         *
         */
        @JvmStatic
        fun List<SourceCourse>.toOwnType() = ParserTypeConverter(this).generateConvertedCourse()
    }

    /**
     * 生成转换
     */
    fun generateConvertedCourse(): List<CourseWithClassTimes> {
        val courseWithClassTimes = ArrayList<CourseWithClassTimes>()
        for (sourceCourse in this.coursesSource) {
            val course = Course()
            course.title = sourceCourse.title
            course.remarks = sourceCourse.remark
            course.credit = sourceCourse.credit
            val classTimes = ArrayList<ClassTime>()

            for (sourceClassTime in sourceCourse.classTimes) {
                val classTime = ClassTime()
                classTime.start = sourceClassTime.from
                classTime.end = sourceClassTime.to
                classTime.location = sourceClassTime.location
                classTime.teacherName = sourceClassTime.teacher
                classTime.whichDay = sourceClassTime.day
                for (week in sourceClassTime.startWeek..sourceClassTime.endWeek) {
                    if (week % 2 == 0 && sourceClassTime.scheduleMode != SourceClassTime.SCHEDULE_MODE_ODD) {
                        classTime.setWeekData(week, true)
                    } else if (week % 2 == 1 && sourceClassTime.scheduleMode != SourceClassTime.SCHEDULE_MODE_EVEN) {
                        classTime.setWeekData(week, true)
                    }
                }
                classTimes.add(classTime)
            }
            courseWithClassTimes.add(CourseWithClassTimes(course, classTimes))
        }

        return courseWithClassTimes
    }
}