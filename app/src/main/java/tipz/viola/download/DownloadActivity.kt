package tipz.viola.download

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import tipz.viola.R
import tipz.viola.broha.ListInterfaceActivity
import tipz.viola.webviewui.BaseActivity

class DownloadActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ListInterfaceActivity.activityMode = intent.getStringExtra(Intent.EXTRA_TEXT)
        setContentView(R.layout.recycler_data_list_activity)
        title = resources.getString(R.string.toolbar_expandable_downloads)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }
}