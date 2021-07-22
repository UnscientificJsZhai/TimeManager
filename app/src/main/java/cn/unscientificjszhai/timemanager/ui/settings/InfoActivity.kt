package cn.unscientificjszhai.timemanager.ui.settings

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.unscientificjszhai.timemanager.R
import cn.unscientificjszhai.timemanager.ui.ActivityUtility

class InfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        ActivityUtility.setSystemUIAppearance(this)

        Toast.makeText(this, "施工中", Toast.LENGTH_SHORT).show()
        finish()
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