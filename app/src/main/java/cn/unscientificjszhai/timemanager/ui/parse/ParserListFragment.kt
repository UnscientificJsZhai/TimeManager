package cn.unscientificjszhai.timemanager.ui.parse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.unscientificjszhai.timemanager.R
import com.github.unscientificjszhai.unscientificcourseparser.bean.factory.ParserFactory

/**
 * 展示解析器列表的Fragment。
 *
 * @see ParseCourseActivity
 */
class ParserListFragment : Fragment() {

    /**
     * 用于解析器列表的RecyclerView的适配器。
     */
    internal class ParserAdapter(
        factory: ParserFactory,
        private val setWebViewFragment: (String) -> Unit
    ) :
        RecyclerView.Adapter<ParserAdapter.ViewHolder>() {

        private val parserList = factory.parserList().keys.toList()
        private val parserMap = factory.parserList()

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

            val titleTextView: TextView = view.findViewById(R.id.ParserListRecycler_Title)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_parser_list, parent, false)
            val viewHolder = ViewHolder(view)

            view.setOnClickListener {
                val beanName = parserMap[parserList[viewHolder.bindingAdapterPosition]]
                setWebViewFragment(beanName!!)
            }

            return viewHolder
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.titleTextView.text = parserList[position]
        }

        override fun getItemCount() = this.parserList.size
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_parser_list, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.ParserListFragment_RecyclerView)
        val activity = requireActivity() as ParseCourseActivity
        val factory by activity
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = ParserAdapter(factory) { beanName ->
            activity.startWebViewFragment(beanName)
        }

        return view
    }
}