package tipz.browservio;

import android.content.SharedPreferences;
import android.os.Build;

import com.google.android.material.color.DynamicColors;

import tipz.browservio.broha.api.FavApi;
import tipz.browservio.broha.api.HistoryApi;
import tipz.browservio.broha.database.BrohaClient;
import tipz.browservio.broha.database.BrohaDao;
import tipz.browservio.broha.database.icons.IconHashClient;
import tipz.browservio.settings.SettingsInit;

public class Application extends android.app.Application {
    public BrohaDao historyBroha;
    public BrohaDao favBroha;
    public IconHashClient iconHashClient;
    public SharedPreferences pref;

    @Override
    public void onCreate() {
        super.onCreate();

        pref = new SettingsInit(this).pref; /* Init settings check */
        HistoryApi.doApiInitCheck(this);
        FavApi.doApiInitCheck(this);

        historyBroha = new BrohaClient(this, "history").getDao();
        favBroha = new BrohaClient(this, "bookmarks").getDao();
        iconHashClient = new IconHashClient(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            DynamicColors.applyToActivitiesIfAvailable(this);
    }
}
