package cn.unscientificjszhai.timemanager

import org.junit.Test
import java.util.*

class CalendarTest {

    @Test
    fun calendarTest() {
        val rightNow = Calendar.getInstance()
        val nextWeek = rightNow.clone() as Calendar
        nextWeek.set(Calendar.WEEK_OF_YEAR, rightNow.get(Calendar.WEEK_OF_YEAR) + 1)
        assert(rightNow.get(Calendar.DAY_OF_WEEK) == nextWeek.get(Calendar.DAY_OF_WEEK))
        assert(rightNow.get(Calendar.DAY_OF_YEAR) == nextWeek.get(Calendar.DAY_OF_YEAR) - 7)
    }
}