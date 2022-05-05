package tipz.browservio.broha.icons;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

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

    public IconHash getIconHashByHash(int hash) {
        return appDatabase.iconHashDao().findByHash(hash);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public String save(Bitmap icon) {
        ByteBuffer buffer = ByteBuffer.allocate(icon.getByteCount());
        icon.copyPixelsToBuffer(buffer);
        int hashInt = Arrays.hashCode(buffer.array());
        String hash = Integer.toString(hashInt);

        File dirFile = new File(fileDir);
        if (dirFile.exists() || dirFile.mkdirs()) {
            boolean wasJpg = false;
            File path = new File(fileDir, hash.concat(".jpg"));
            if (path.exists()) {
                path.delete(); /* Delete old JPEG files */
                wasJpg = true;
            }

            path = new File(fileDir, hash.concat(".webp"));
            if (path.exists())
                return Integer.toString(getIconHashByHash(hashInt).getId());

            try {
                FileOutputStream out = new FileOutputStream(path);
                icon.compress(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ?
                        Bitmap.CompressFormat.WEBP_LOSSY : Bitmap.CompressFormat.WEBP, 75, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                return null;
            }

            if (wasJpg) {
                return Integer.toString(getIconHashByHash(hashInt).getId());
            } else {
                getDao().insertAll(new IconHash(hashInt));
                return Integer.toString(getDao().lastIcon().getId());
            }
        }
        return null;
    }

    public Bitmap read(String iconId) {
        if (iconId == null)
            return null;
        IconHash data = getIconHashById(Integer.parseInt(iconId));
        if (data == null)
            return null;
        String hash = String.valueOf(data.getIconHash());
        File imgFile = new File(fileDir, hash.concat(".webp"));
        if (imgFile.exists())
            return BitmapFactory.decodeFile(imgFile.getAbsolutePath());

        imgFile = new File(fileDir, hash.concat(".jpg"));
        if (imgFile.exists())
            return BitmapFactory.decodeFile(imgFile.getAbsolutePath());

        return null;
    }
}
