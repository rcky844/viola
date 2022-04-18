package tipz.browservio.broha;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Broha {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo
    private String iconHash;

    @ColumnInfo
    private String title;

    @ColumnInfo
    private String url;

    @ColumnInfo
    private long timestamp;

    public Broha(int id, String iconHash, String title, String url, long timestamp) {
        this.id = id;
        this.iconHash = iconHash;
        this.title = title;
        this.url = url;
        this.timestamp = timestamp;
    }

    @Ignore
    public Broha(String iconHash, String title, String url) {
        this.iconHash = iconHash;
        this.title = title;
        this.url = url;
        this.timestamp = System.currentTimeMillis() / 1000L;
    }

    @Ignore
    public Broha(String title, String url) {
        this.iconHash = null;
        this.title = title;
        this.url = url;
        this.timestamp = System.currentTimeMillis() / 1000L;
    }

    @Ignore
    public Broha(String url) {
        this.iconHash = null;
        this.title = null;
        this.url = url;
        this.timestamp = System.currentTimeMillis() / 1000L;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTimestamp() {
        this.timestamp = System.currentTimeMillis() / 1000L;
    }

    public int getId() {
        return this.id;
    }

    public String getIconHash() {
        return this.iconHash;
    }

    public String getTitle() {
        return this.title;
    }

    public String getUrl() {
        return this.url;
    }

    public long getTimestamp() {
        return this.timestamp;
    }
}
