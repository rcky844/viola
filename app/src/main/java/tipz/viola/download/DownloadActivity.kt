package tipz.viola.download

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tipz.viola.Application
import tipz.viola.R
import tipz.viola.broha.ListInterfaceActivity
import tipz.viola.webviewui.BaseActivity
import java.lang.ref.WeakReference

class DownloadActivity : BaseActivity() {
    private lateinit var downloadClient: DownloadClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        downloadClient = (applicationContext as Application).downloadClient

        ListInterfaceActivity.activityMode = intent.getStringExtra(Intent.EXTRA_TEXT)
        setContentView(R.layout.recycler_data_list_activity)
        title = resources.getString(R.string.toolbar_expandable_downloads)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    override fun onStart() {
        super.onStart()
        val downloadList = findViewById<RecyclerView>(R.id.recyclerView)

        CoroutineScope(Dispatchers.IO).launch {
            listData = downloadClient.downloadQueue.value
        }


        val layoutManager = downloadList.layoutManager as LinearLayoutManager
        downloadList.adapter = ItemsAdapter(this)
    }

    class ItemsAdapter(downloadActivity: DownloadActivity)
        : RecyclerView.Adapter<ItemsAdapter.ViewHolder>() {
        private val mDownloadActivity: WeakReference<DownloadActivity>

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val back: ConstraintLayout
            val icon: AppCompatImageView
            val title: AppCompatTextView
            val url: AppCompatTextView
            val time: AppCompatTextView

            init {
                back = view.findViewById(R.id.bg)
                icon = view.findViewById(R.id.icon)
                title = view.findViewById(R.id.title)
                url = view.findViewById(R.id.url)
                time = view.findViewById(R.id.time)
            }
        }

        init {
            mDownloadActivity = WeakReference(downloadActivity)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_data_list_item, parent, false)
            return ViewHolder(view)
        }

        @SuppressLint("SimpleDateFormat")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val downloadActivity = mDownloadActivity.get()
            val data = listData!![position]

            holder.title.text = data.uriString
        }

        override fun getItemCount(): Int {
            if (listData == null) return 0
            return listData!!.size
        }
    }

    companion object {
        private var listData: MutableList<DownloadObject>? = null
    }
}