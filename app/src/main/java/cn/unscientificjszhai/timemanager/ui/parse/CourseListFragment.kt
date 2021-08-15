package cn.unscientificjszhai.timemanager.ui.parse

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.unscientificjszhai.timemanager.R
import cn.unscientificjszhai.timemanager.TimeManagerApplication
import cn.unscientificjszhai.timemanager.data.course.CourseWithClassTimes
import cn.unscientificjszhai.timemanager.features.parse.ParserTypeConverter
import cn.unscientificjszhai.timemanager.ui.others.ProgressDialog
import com.github.unscientificjszhai.unscientificcourseparser.core.export.CoursesJson.Companion.json
import kotlinx.coroutines.launch
import com.github.unscientificjszhai.unscientificcourseparser.core.data.Course as SourceCourse

/**
 * 展示解析结果的Fragment。
 */
class CourseListFragment : Fragment() {

    /**
     * 用于展示解析到的课程的RecyclerView的Adapter。
     *
     * @param courseList 要展示的数据列表。
     * @param classTimeString 格式化上课时间字符串的模板。
     */
    internal class CourseAdapter(
        private val courseList: List<CourseWithClassTimes>,
        private val classTimeString: String
    ) :
        RecyclerView.Adapter<CourseAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

            val titleText: TextView =
                view.findViewById(R.id.CourseListFragmentRecycler_TitleText)
            val subTitleText: TextView =
                view.findViewById(R.id.CourseListFragmentRecycler_SubTitleText)
            val classTimeText: TextView =
                view.findViewById(R.id.CourseListFragmentRecycler_ClassTimeText)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val rootView = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_course_list_fragment, parent, false)
            val viewHolder = ViewHolder(rootView)

            //TODO 点击查看详情

            return viewHolder
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val courseWithClassTimes = this.courseList[position]
            val course = courseWithClassTimes.course
            holder.titleText.text = course.title
            if (course.remarks.isEmpty()) {
                holder.subTitleText.visibility = View.GONE
            } else {
                holder.subTitleText.visibility = View.VISIBLE
                holder.subTitleText.text = course.remarks
            }
            holder.classTimeText.text =
                this.classTimeString.format(courseWithClassTimes.classTimes.size)
        }

        override fun getItemCount() = this.courseList.size
    }

    companion object {

        private const val RESULT_KEY = "result"

        /**
         * 启动这个Fragment的静态方法。
         *
         * @param result 解析结果。
         */
        @JvmStatic
        fun newInstance(result: List<SourceCourse>) = CourseListFragment().apply {
            arguments = Bundle().apply {
                putString(RESULT_KEY, result.json().toString())
            }
        }
    }

    private lateinit var viewModel: CourseListFragmentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        this.viewModel = ViewModelProvider(this)[CourseListFragmentViewModel::class.java]
        if (savedInstanceState == null) {
            val jsonString = arguments?.getString(RESULT_KEY) ?: ""
            this.viewModel.courseList =
                ParserTypeConverter.fromJson(jsonString).generateConvertedCourse()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_course_list, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.CourseListFragment_RecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = CourseAdapter(
            this.viewModel.courseList,
            getString(R.string.fragment_CourseListFragment_ClassTimeDescription)
        )

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_web_view, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.ParseCourseActivity_Done) {
            val dialog = ProgressDialog(requireActivity())
            viewModel.viewModelScope.launch {
                dialog.show()
                viewModel.save(requireActivity().application as TimeManagerApplication)
                dialog.postDismiss()
            }
            requireActivity().finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}