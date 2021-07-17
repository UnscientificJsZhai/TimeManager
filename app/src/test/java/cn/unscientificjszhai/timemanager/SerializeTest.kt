package cn.unscientificjszhai.timemanager

import cn.unscientificjszhai.timemanager.data.course.ClassTime
import cn.unscientificjszhai.timemanager.data.course.Course
import cn.unscientificjszhai.timemanager.data.course.CourseWithClassTimes
import cn.unscientificjszhai.timemanager.data.tables.CourseTable
import cn.unscientificjszhai.timemanager.features.backup.TableWithCourses
import org.junit.Test
import java.util.*

class SerializeTest {

    @Test
    fun serializeTest() {
        val courseTable = CourseTable("测试")
        val course = Course(1, "课程标题", 1.0, "备注", ArrayList(), null)
        val classTime = ClassTime(null, 1, 1, 2, 1, 4, "教师姓名", "上课教室")

        val tableWithCourses =
            TableWithCourses(
                courseTable,
                arrayListOf(CourseWithClassTimes(course, arrayListOf(classTime)))
            )
        val beforeHash =
            arrayListOf(courseTable.hashCode(), course.hashCode(), classTime.hashCode())

        val objString = tableWithCourses.serializeObject()
        System.err.println(objString)
        val newObject = TableWithCourses.stringSerializeObject(objString)
        if (newObject == null) {
            assert(false)
        } else {
            assert(newObject.courseTable.hashCode() == beforeHash[0])
            assert(newObject.courses[0].course.hashCode() == beforeHash[1])
            assert(newObject.courses[0].classTimes[0].hashCode() == beforeHash[2])
        }
    }
}