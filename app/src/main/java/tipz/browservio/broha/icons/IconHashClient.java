package tipz.browservio.broha.icons;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.room.Room;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class IconHashClient {
    private final IconHashDatabase appDatabase;

    public IconHashClient(Context context) {
        //appDatabase = Room.databaseBuilder(context, IconHashDatabase.class, "iconHash").build();
        /* FIXME: Don't run on main thread */
        appDatabase = Room.databaseBuilder(context, IconHashDatabase.class, "iconHash").allowMainThreadQueries().build();
    }

    public IconHashDatabase getDatabase() {
        return appDatabase;
    }

    public IconHashDao getDao() {
        return appDatabase.iconHashDao();
    }

    public IconHash getIconHashById(int id) {
        return appDatabase.iconHashDao().findById(id);
    }

    public IconHash getIconHashByHash(String hash) {
        return appDatabase.iconHashDao().findByHash(hash);
    }

    public String save(Context context, Bitmap icon) {
        ByteBuffer buffer = ByteBuffer.allocate(icon.getByteCount());
        icon.copyPixelsToBuffer(buffer);
        String hash = Integer.toString(Arrays.hashCode(buffer.array()));

        String fileDir = context.getFilesDir().getPath().concat("/favicon");
        File dirFile = new File(fileDir);
        if (dirFile.exists() || dirFile.mkdirs()) {
            File path = new File(fileDir, hash.concat(".jpg"));

            if (path.exists())
                return Integer.toString(getDao().findByHash(hash).getId());

            try {
                FileOutputStream out = new FileOutputStream(path);
                icon.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                return null;
            }

            IconHash iconHash = new IconHash(hash);
            getDao().insertAll(iconHash);
            return Integer.toString(iconHash.getId());
        }
        return null;
    }
}
