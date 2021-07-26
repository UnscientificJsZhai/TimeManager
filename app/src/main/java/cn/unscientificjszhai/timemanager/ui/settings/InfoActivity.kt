package cn.unscientificjszhai.timemanager.ui.settings

import android.content.*
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.unscientificjszhai.timemanager.R
import cn.unscientificjszhai.timemanager.ui.ActivityUtility

/**
 * 显示App简介的Activity。
 */
class InfoActivity : AppCompatActivity() {

    companion object {

        private const val MAIL_ADDRESS = "unscientificjszhai@163.com"

        private const val LOGO_DESIGNER_MAIL_ADDRESS = "2358072658@qq.com"
    }

    private lateinit var githubButton: Button
    private lateinit var bilibiliButton: Button
    private lateinit var mailButton: Button
    private lateinit var coolApkButton: Button

    private lateinit var designerMailButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        ActivityUtility.setSystemUIAppearance(this)

        this.githubButton = findViewById(R.id.InfoActivity_GitHubButton)
        this.bilibiliButton = findViewById(R.id.InfoActivity_BilibiliButton)
        this.mailButton = findViewById(R.id.InfoActivity_MailButton)
        this.coolApkButton = findViewById(R.id.InfoActivity_CoolApkButton)

        this.designerMailButton = findViewById(R.id.InfoActivity_DesignerMailButton)

        githubButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://github.com/UnscientificJsZhai/TimeManager")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        bilibiliButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://space.bilibili.com/13054331")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        mailButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.putExtra(Intent.EXTRA_EMAIL, MAIL_ADDRESS)
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, R.string.info_UnableToSendEmail, Toast.LENGTH_SHORT).show()
            }
        }

        mailButton.setOnLongClickListener {
            Toast.makeText(this, MAIL_ADDRESS, Toast.LENGTH_LONG).show()
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboardManager.setPrimaryClip(ClipData.newPlainText(null,MAIL_ADDRESS))
            true
        }

        coolApkButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("http://www.coolapk.com/u/675535")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        designerMailButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.putExtra(Intent.EXTRA_EMAIL, LOGO_DESIGNER_MAIL_ADDRESS)
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, R.string.info_UnableToSendEmail, Toast.LENGTH_SHORT).show()
            }
        }

        designerMailButton.setOnLongClickListener {
            Toast.makeText(this, MAIL_ADDRESS,Toast.LENGTH_LONG).show()
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboardManager.setPrimaryClip(ClipData.newPlainText(null, LOGO_DESIGNER_MAIL_ADDRESS))
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}