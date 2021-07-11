package cn.unscientificjszhai.timemanager.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cn.unscientificjszhai.timemanager.R
import cn.unscientificjszhai.timemanager.data.course.Course
import cn.unscientificjszhai.timemanager.data.course.CourseWithClassTimes

/**
 * 供主界面的RecyclerView使用的适配器。
 *
 * @see MainActivity
 */
internal class CourseAdapter(activity: MainActivity) :
    ListAdapter<CourseWithClassTimes, CourseAdapter.ViewHolder>(CourseDiffCallback) {

    /**
     * 当前时间的标记对象，委托给宿主Activity。
     */
    private val timeTagger by activity

    /**
     * 获取宿主Activity的状态，是否只显示今天。
     *
     * @return 是否仅显示今天
     */
    private val showTodayOnly: () -> Boolean = activity.getToadyOnlyGetterMethod()

    /**
     * ListAdapter用于对比数据变化的方法集合，用于CourseAdapter类。
     */
    private object CourseDiffCallback : DiffUtil.ItemCallback<CourseWithClassTimes>() {
        override fun areItemsTheSame(
            oldItem: CourseWithClassTimes,
            newItem: CourseWithClassTimes
        ) = oldItem.course.id == newItem.course.id

        override fun areContentsTheSame(
            oldItem: CourseWithClassTimes,
            newItem: CourseWithClassTimes
        ) = oldItem == newItem
    }

    inner class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
        val titleText: TextView = rootView.findViewById(R.id.CourseWidget_TitleText)
        val informationText: TextView = rootView.findViewById(R.id.CourseWidget_Information)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_course, parent, false)
        val holder = ViewHolder(view)

        view.setOnClickListener {
            val course = getItem(holder.bindingAdapterPosition)

            CourseDetailActivity.startThisActivity(view.context, course)
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val course = this.getItem(position).course
            holder.titleText.text = course.title
            holder.informationText.text = generateInformation(course)
        } catch (e: NullPointerException) {
            return
        }
    }

    private fun generateInformation(course: Course): String {
        //TODO 格式化信息文本
        return "${course.credit}学分"
    }
}