package cn.unscientificjszhai.timemanager.data.course

import androidx.room.Embedded
import androidx.room.Relation
import java.io.Serializable

/**
 * [Course]和[ClassTime]之间的一对多关系。
 *
 * @param course Course对象。
 * @param classTimes Course对象关联的ClassTime的列表。
 */
data class CourseWithClassTimes(
    @Embedded val course: Course,
    @Relation(parentColumn = "id", entityColumn = "course_id")
    var classTimes: List<ClassTime>
) : Serializable