package cn.unscientificjszhai.timemanager.data.course

import androidx.room.TypeConverter

class CourseEventsConverter {

    @TypeConverter
    fun getIDs(value: String): ArrayList<Long> {
        val list = ArrayList<Long>()
        if (value.isNotBlank()) {
            for (s in value.split(',')) {
                list.add(s.toLong())
            }
        }
        return list
    }

    @TypeConverter
    fun setIDs(value: ArrayList<Long>): String {
        val str = StringBuilder()
        for (index in value.indices) {
            if (index == 0) {
                str.append(value[index])
            } else {
                str.append(",").append(value[index])
            }
        }
        return str.toString()
    }
}