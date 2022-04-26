package tipz.browservio.broha.icons;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.room.Room;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class IconHashClient {
    private final IconHashDatabase appDatabase;
    private final String fileDir;

    public IconHashClient(Context context) {
        //appDatabase = Room.databaseBuilder(context, IconHashDatabase.class, "iconHash").build();
        /* FIXME: Don't run on main thread */
        appDatabase = Room.databaseBuilder(context, IconHashDatabase.class, "iconHash").allowMainThreadQueries().build();
        fileDir = context.getFilesDir().getPath().concat("/favicon");
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

    public String save(Bitmap icon) {
        ByteBuffer buffer = ByteBuffer.allocate(icon.getByteCount());
        icon.copyPixelsToBuffer(buffer);
        String hash = Integer.toString(Arrays.hashCode(buffer.array()));

        File dirFile = new File(fileDir);
        if (dirFile.exists() || dirFile.mkdirs()) {
            File path = new File(fileDir, hash.concat(".jpg"));

            if (path.exists())
                return Integer.toString(getIconHashByHash(hash).getId());

            try {
                FileOutputStream out = new FileOutputStream(path);
                icon.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                return null;
            }

            getDao().insertAll(new IconHash(hash));
            return Integer.toString(getDao().lastIcon().getId());
        }
        return null;
    }

    public Bitmap read(String iconId) {
        if (iconId == null)
            return null;
        IconHash data = getIconHashById(Integer.parseInt(iconId));
        if (data == null)
            return null;
        File imgFile = new File(fileDir, data.getIconHash().concat(".jpg"));
        if (imgFile.exists())
            return BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        return null;
    }
}
