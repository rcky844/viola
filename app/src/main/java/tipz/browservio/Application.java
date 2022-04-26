package tipz.browservio;

import tipz.browservio.broha.BrohaClient;
import tipz.browservio.broha.BrohaDao;
import tipz.browservio.broha.icons.IconHashClient;

public class Application extends android.app.Application {
    public BrohaDao historyBroha;
    public BrohaDao favBroha;
    public IconHashClient iconHashClient;

    @Override
    public void onCreate() {
        super.onCreate();
        historyBroha = new BrohaClient(this, "history").getDao();
        favBroha = new BrohaClient(this, "bookmarks").getDao();
        iconHashClient = new IconHashClient(this);
    }
}
