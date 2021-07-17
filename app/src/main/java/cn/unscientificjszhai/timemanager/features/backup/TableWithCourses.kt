package cn.unscientificjszhai.timemanager.features.backup

import cn.unscientificjszhai.timemanager.data.course.CourseWithClassTimes
import cn.unscientificjszhai.timemanager.data.tables.CourseTable
import java.io.*
import java.nio.charset.StandardCharsets

/**
 * 用于备份和恢复功能的可序列化类。
 *
 * @see CourseTable
 *
 * @see CourseWithClassTimes
 */
class TableWithCourses(val courseTable: CourseTable, val courses: List<CourseWithClassTimes>) :
    Serializable {

    /**
     * 返回序列化此对象的字符串。
     *
     * @return 序列化字符串。
     * @throws IOException 出现IO异常。
     */
    @Throws(IOException::class)
    fun serializeObject(): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val out = ObjectOutputStream(byteArrayOutputStream)
        out.writeObject(this)
        val objectString = byteArrayOutputStream.toString("ISO-8859-1")
        out.close()
        byteArrayOutputStream.close()
        return objectString
    }

    companion object {
        /**
         * 用字符串反序列化生成对象。
         *
         * @param input 输入字符串
         * @return 生成的对象。如果生成的对象不为[TableWithCourses]的话，就会返回null。
         * @throws IOException 出现IO异常。
         */
        @Throws(IOException::class)
        fun stringSerializeObject(input: String): TableWithCourses? {
            val byteArrayInputStream =
                ByteArrayInputStream(input.toByteArray(StandardCharsets.ISO_8859_1))
            val objectInputStream = ObjectInputStream(byteArrayInputStream)
            val serializableObject: Any = try {
                objectInputStream.readObject()
            } catch (e: ClassNotFoundException) {
                return null
            }
            objectInputStream.close()
            byteArrayInputStream.close()
            return if (serializableObject !is TableWithCourses) {
                null
            } else {
                serializableObject
            }
        }
    }
}