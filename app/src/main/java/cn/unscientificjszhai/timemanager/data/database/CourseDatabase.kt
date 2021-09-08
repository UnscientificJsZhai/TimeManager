package cn.unscientificjszhai.timemanager.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import cn.unscientificjszhai.timemanager.TimeManagerApplication
import cn.unscientificjszhai.timemanager.data.course.ClassTime
import cn.unscientificjszhai.timemanager.data.course.Course
import cn.unscientificjszhai.timemanager.data.dao.ClassTimeDao
import cn.unscientificjszhai.timemanager.data.dao.CourseDao
import cn.unscientificjszhai.timemanager.data.tables.CourseTable

/**
 * Course对象的数据库。文件名为table1.db，其中1的位置应该为这个数据库文件对应的[CourseTable]对象的id。
 */
@Database(
    entities = [Course::class, ClassTime::class],
    version = TimeManagerApplication.COURSE_DATABASE_VERSION
)
abstract class CourseDatabase : RoomDatabase() {

    abstract fun courseDao(): CourseDao
    abstract fun classTimeDao(): ClassTimeDao
}