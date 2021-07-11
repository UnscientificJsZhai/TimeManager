package cn.unscientificjszhai.timemanager.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import cn.unscientificjszhai.timemanager.TimeManagerApplication
import cn.unscientificjszhai.timemanager.data.tables.CourseTable
import cn.unscientificjszhai.timemanager.data.dao.CourseTableDao

/**
 * CourseTable的数据库对象，文件名为database.db。
 */
@Database(
    entities = [CourseTable::class],
    version = TimeManagerApplication.DATABASE_VERSION
)
abstract class CourseTableDatabase : RoomDatabase() {
    abstract fun courseTableDao(): CourseTableDao
}