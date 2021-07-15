package cn.unscientificjszhai.timemanager.ui

import android.app.Activity
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AlertDialog
import cn.unscientificjszhai.timemanager.R

/**
 * 自定义ProgressDialog。
 *
 * @param context 显示Dialog的Activity。
 */
class ProgressDialog(private val context: Activity) {

    private val builder = AlertDialog.Builder(context)
    private val messageView: TextView

    private var dialog: AlertDialog? = null

    init {
        val view: View = View.inflate(context, R.layout.dialog_progress, null)
        messageView = view.findViewById(R.id.ProgressDialog_Message)
        builder.setView(view)
    }

    /**
     * 设定加载球右侧的文本内容。默认显示预设内容。
     *
     * @param resID 文本资源ID。
     */
    @UiThread
    @Suppress("Unused")
    fun setMessage(@StringRes resID: Int): ProgressDialog {
        messageView.setText(resID)
        return this
    }

    /**
     * 设定加载球右侧的文本内容。默认显示预设内容。
     *
     * @param message 文本内容。
     */
    @UiThread
    @Suppress("Unused")
    fun setMessage(message: CharSequence?): ProgressDialog {
        messageView.text = message
        return this
    }

    /**
     * 设定标题。不设置将不会显示。
     *
     * @param resID 文本资源ID。
     */
    @UiThread
    fun setTitle(@StringRes resID: Int): ProgressDialog {
        builder.setTitle(resID)
        return this
    }

    /**
     * 设定标题。不设置将不会显示。
     *
     * @param title 文本内容。
     */
    @UiThread
    fun setTitle(title: CharSequence?): ProgressDialog {
        builder.setTitle(title)
        return this
    }

    /**
     * 显示Dialog。
     */
    @UiThread
    fun show(): AlertDialog = builder.show().apply {
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        dialog = this
    }

    /**
     * 使得对话框消失。
     */
    @WorkerThread
    fun postDismiss() {
        context.runOnUiThread {
            dialog?.dismiss()
        }
    }
}