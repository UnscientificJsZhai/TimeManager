package cn.unscientificjszhai.timemanager

import cn.unscientificjszhai.timemanager.data.course.ClassTime
import org.junit.Test
import java.util.*

class WeekDataTest {

    @Test
    fun changeTest() {
        val classTime = ClassTime()
        val random = Random()
        for (count in 0..999) {
            val week = random.nextInt(30) + 1
            val set = random.nextBoolean()
            classTime.setWeekData(week, set)
            assert(classTime.getWeekData(week) == set)
            println("passed:$week,$set")
        }
    }

    @Test
    fun test() {
        val classTime = ClassTime()
        for (index in 1..30) {
            classTime.setWeekData(index, true)
            assert(classTime.getWeekData(index))
            println("passed:$index")
        }
    }
}