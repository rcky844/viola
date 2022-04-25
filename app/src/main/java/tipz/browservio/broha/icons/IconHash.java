package tipz.browservio.broha.icons;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class IconHash {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo
    private String iconHash;

    public IconHash(int id, String iconHash) {
        this.id = id;
        this.iconHash = iconHash;
    }

    @Ignore
    public IconHash(String iconHash) {
        this.iconHash = iconHash;
    }

    public int getId() {
        return id;
    }

    public String getIconHash() {
        return iconHash;
    }
}
