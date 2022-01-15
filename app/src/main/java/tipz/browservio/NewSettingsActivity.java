package tipz.browservio;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

public class NewSettingsActivity extends AppCompatActivity {

    public final Intent needLoad = new Intent();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_settings);

        Toolbar _toolbar = findViewById(R.id._toolbar);
        setSupportActionBar(_toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        _toolbar.setNavigationOnClickListener(_v -> onBackPressed());

        NewSettings fragment = new NewSettings(this);
        getSupportFragmentManager().beginTransaction().replace(R.id.list_container, fragment).commit();
    }

    @Override
    public void onBackPressed() {
        if (NewSettings.getNeedReload()) {
            needLoad.putExtra("needLoadUrl", getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_suffix_reload)));
            setResult(0, needLoad);
        }
        finish();
    }
}
