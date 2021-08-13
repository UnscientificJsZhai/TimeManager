package cn.unscientificjszhai.timemanager.ui.parse

import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import cn.unscientificjszhai.timemanager.R

class WebViewFragment : Fragment() {

    companion object {

        private const val BEAN_NAME_KEY = "beanName"

        /**
         * 启动这个Fragment的静态方法。
         *
         * @param beanName 要加载的解析器的beanName。
         */
        @JvmStatic
        fun newInstance(beanName: String) = WebViewFragment().apply {
            arguments = Bundle().apply {
                putString(BEAN_NAME_KEY, beanName)
            }
        }
    }

    private lateinit var textView: TextView
    private lateinit var viewModel: WebViewFragmentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        this.viewModel = ViewModelProvider(this)[WebViewFragmentViewModel::class.java]
        if (savedInstanceState == null) {
            val activity = requireActivity() as ParseCourseActivity
            arguments?.let {
                val beanName = it.getString(BEAN_NAME_KEY)
                val factory by activity
                viewModel.parser = factory[beanName!!]
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_web_view, container, false)

        this.textView = view.findViewById(R.id.WebViewFragment_TextView)
        textView.text = this.viewModel.parser.url
        Toast.makeText(
            container?.context,
            R.string.activity_ParseCourseActivity_HelpToast,
            Toast.LENGTH_LONG
        ).show()

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_web_view, menu)
    }
}