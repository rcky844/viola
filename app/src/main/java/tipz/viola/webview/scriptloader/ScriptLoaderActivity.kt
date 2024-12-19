package tipz.viola.webview.scriptloader

import android.content.DialogInterface
import android.os.Bundle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tipz.viola.R
import tipz.viola.databinding.ActivityRecyclerDataListBinding
import tipz.viola.databinding.DialogEdittextBinding
import tipz.viola.webview.activity.BaseActivity

class ScriptLoaderActivity : BaseActivity() {
    private lateinit var binding: ActivityRecyclerDataListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerDataListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Setup toolbar
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        setTitle(R.string.scriptloader)

        // Setup fab
        val fab = binding.fab
        fab.setImageResource(R.drawable.add)
        fab.setOnClickListener {

        }

        // Show warning dialog
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.caution_capital)
            .setMessage(R.string.scriptloader_warning)
            .setPositiveButton(android.R.string.ok, null)
            .create().show()
    }

    fun createAddDialog() {
        val binding: DialogEdittextBinding = DialogEdittextBinding.inflate(layoutInflater)
        val view = binding.root

        val script = binding.edittext
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.scriptloader_add)
            .setView(view)
            .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                if (script.text.toString().trim().isNotEmpty()) {

                }
                true
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create().show()
    }
}