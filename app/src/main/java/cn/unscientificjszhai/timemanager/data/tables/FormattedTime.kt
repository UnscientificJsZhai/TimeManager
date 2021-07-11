package cn.unscientificjszhai.timemanager.data.tables

/**
 * 时间封装类型。
 *
 * @param string 输入的字符串，长度至少为8。每一位都必须是数字。
 * @throws NumberFormatException 传入的字符串不是纯数字时抛出此异常。
 * @throws IndexOutOfBoundsException 传入的字符串长度小于8时抛出此异常。
 */
class FormattedTime(string: String) {

    companion object {

        /**
         * 获取两节课之间的时间间隔，单位：分钟。
         *
         * @param start 开始。
         * @param end 结束。
         * @return 以分钟为单位的时间间隔。
         */
        @JvmStatic
        fun duration(start: FormattedTime, end: FormattedTime) =
            (end.endH - start.startH) * 60 + end.endM - start.startM

    }

    var startH: Int = String(charArrayOf(string[0], string[1])).toInt()
        set(value) {
            if (value in 0..23) {
                field = value
            }
        }
    var startM: Int = String(charArrayOf(string[2], string[3])).toInt()
        set(value) {
            if (value in 0..59) {
                field = value
            }
        }
    var endH: Int = String(charArrayOf(string[4], string[5])).toInt()
        set(value) {
            if (value in 0..23) {
                field = value
            }
        }
    var endM: Int = String(charArrayOf(string[6], string[7])).toInt()
        set(value) {
            if (value in 0..59) {
                field = value
            }
        }

    init {
        if (startH !in 0..23 || startM !in 0..59 || endH !in 0..23 || endM !in 0..59) {
            throw RuntimeException()
        }
    }

    /**
     * 获取持续时间。
     *
     * @return 结束时间减开始时间，单位分钟。
     */
    fun duration() = if (startH > endH) {
        0
    } else if (startH == endH && startM > endM) {
        0
    } else {
        60 * (endH - startH) + endM - startM
    }

    /**
     * 根据初始时间和间隔设置结束时间。
     *
     * @param duration 间隔，单位分钟。
     */
    fun autoSetEndTime(duration: Int) {
        var hour = this.endH
        var min = this.endM
        min += duration
        while (min >= 60) {
            min -= 60
            hour += 1
        }
        while (hour >= 24) {
            hour -= 24
        }
        this.endH = hour
        this.endM = min
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()
        arrayOf(this.startH, this.startM, this.endH, this.endM).forEach { number ->
            if (number < 10) {
                stringBuilder.append(0)
            }
            stringBuilder.append(number)
        }
        return stringBuilder.toString()
    }

    override fun equals(other: Any?) = if (other is FormattedTime) {
        this.toString() == other.toString()
    } else {
        false
    }

    override fun hashCode() = this.toString().hashCode()
}