package cn.unscientificjszhai.timemanager.ui.others

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import cn.unscientificjszhai.timemanager.R

/**
 * 通用UI函数集。
 */
object ActivityUtility {

    /**
     * 为目标Activity设置浅色状态栏和导航栏。
     *
     * @param context Activity的上下文。
     */
    fun setSystemUIAppearance(context: Activity) {
        val window = context.window
        if (context.isDarkMode()) {
            //深色模式不进行调整
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = window.insetsController
            val flag = WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS or
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            controller?.setSystemBarsAppearance(flag, flag)
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
    }

    /**
     * 判断当前是否处于深色模式中。
     */
    private fun Context.isDarkMode(): Boolean =
        this.applicationContext.resources.configuration.uiMode == 0x21

    /**
     * 当获得权限时运行。
     *
     * @param permission 要检查的权限。
     * @param permissionDenied 当没有获得权限时运行的代码块。
     * @param block 当获得权限时运行的代码块。
     */
    inline fun <T> Activity.runIfPermissionGranted(
        permission: String,
        permissionDenied: Activity.() -> Unit,
        block: Activity.() -> T
    ): T? {
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            return block()
        } else {
            permissionDenied()
        }
        return null
    }

    /**
     *  跳转到系统设置来请求权限。
     */
    fun Activity.jumpToSystemPermissionSettings() {
        try {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            })
        } catch (e: Exception) {
            Log.e("WelcomeActivity", "onRequestPermissionsResult: \n$e")
            Toast.makeText(
                this,
                R.string.activity_WelcomeActivity_FailToJumpToSettings,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * RecyclerView滚动函数集。
     */
    object RecyclerScrollHelper {

        /**
         * 使参数中的RecyclerView滚动到指定位置。RecyclerView必须使用LinearLayoutManager作为LayoutManager。
         *
         * @param recyclerView 要滚动的RecyclerView。
         * @param position 目标位置。
         */
        private fun scrollToPosition(recyclerView: RecyclerView, position: Int) {
            val manager = recyclerView.layoutManager
            if (manager is LinearLayoutManager) {
                val scroller = LinearSmoothScroller(recyclerView.context)

                scroller.targetPosition = position
                manager.startSmoothScroll(scroller)
            }
        }

        /**
         * 使参数中的RecyclerView滚动到底部。
         *
         * @param recyclerView 要滚动的RecyclerView。
         */
        fun scrollToBottom(recyclerView: RecyclerView) {
            recyclerView.adapter?.let {
                val position = it.itemCount
                scrollToPosition(recyclerView, position)
            }
        }
    }

    /**
     * 为RecyclerView的子元素添加下划线。
     */
    class RecyclerViewDecoration : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.set(0, 0, 0, 1)
        }
    }
}