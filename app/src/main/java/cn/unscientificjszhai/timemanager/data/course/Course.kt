package cn.unscientificjszhai.timemanager.data.course

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.io.Serializable
import java.lang.ref.WeakReference

/**
 * 课程。
 *
 * @param id 主键。
 * @param title 课程名。
 * @param credit 学分。
 * @param remarks 备注。
 * @param specificClassTime 不在数据库中的列。用来在仅显示今天的情况下，判断上课时间。
 */
@Entity(tableName = Course.TABLE_NAME)
@TypeConverters(CourseEventsConverter::class)
data class Course(
    @PrimaryKey(autoGenerate = true) var id: Long?,
    var title: String,
    var credit: Double,
    var remarks: String,
    var associatedEventsId: ArrayList<Long>,
    @Ignore @Transient var specificClassTime: WeakReference<ClassTime>? = null
) : Serializable {

    companion object {
        /**
         * 表的名字。
         */
        const val TABLE_NAME = "course"

        /**
         * 检查对象是否为Course数据对象，以及其合法性。
         *
         * @param serializable 可序列化对象。
         * @return 若目标对象合法则其必须满足：标题不为空，学分大于0，关联的[ClassTime]对象全部合法。
         */
        fun checkLegitimacy(serializable: Serializable?): Boolean {
            if (serializable is CourseWithClassTimes) {
                val course = serializable.course
                when {
                    course.title.isEmpty() -> return false
                    course.credit < 0 -> return false
                    serializable.classTimes.isEmpty() -> return false
                    else -> {
                        for (classTime: ClassTime in serializable.classTimes) {
                            if (!classTime.isLegitimacy()) return false
                        }
                        return true
                    }
                }
            } else {
                return false
            }
        }
    }

    /**
     * 创建一个新的对象的方法。
     */
    constructor() : this(null, "", 0.0, "", ArrayList<Long>())
}