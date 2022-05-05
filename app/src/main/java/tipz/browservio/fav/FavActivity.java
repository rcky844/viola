package tipz.browservio.fav;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;

import tipz.browservio.Application;
import tipz.browservio.R;
import tipz.browservio.broha.Broha;
import tipz.browservio.broha.icons.IconHashClient;
import tipz.browservio.utils.CommonUtils;

public class FavActivity extends AppCompatActivity {
    private static List<Broha> listData;

    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.recycler_list_item_activity);
        initialize();
        setTitle(getResources().getString(R.string.fav));
    }

    /**
     * Initialize function
     */
    private void initialize() {

        Toolbar _toolbar = findViewById(R.id._toolbar);
        setSupportActionBar(_toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        _toolbar.setNavigationOnClickListener(_v -> onBackPressed());
        FloatingActionButton _fab = findViewById(R.id._fab);

        _fab.setOnClickListener(_view -> new MaterialAlertDialogBuilder(this)
                .setTitle(getResources().getString(R.string.delete_all_entries))
                .setMessage(getResources().getString(R.string.delete_fav_message))
                .setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
                    FavUtils.clear(this);
                    CommonUtils.showMessage(this, getResources().getString(R.string.wiped_success));
                    finish();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create().show());
    }

    @Override
    public void onStart() {
        super.onStart();
        RecyclerView favList = findViewById(R.id.recyclerView);
        listData = FavApi.favBroha(this).getAll();
        favList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        favList.setAdapter(new ItemsAdapter(this, ((Application) getApplicationContext()).iconHashClient));
    }

    void isEmptyCheck() {
        // Placed here for old data migration
        if (FavUtils.isEmptyCheck(this)) {
            CommonUtils.showMessage(this, getResources().getString(R.string.fav_list_empty));
            finish();
        }
    }

    public static class ItemsAdapter extends RecyclerView.Adapter<FavActivity.ItemsAdapter.ViewHolder> {
        private final WeakReference<FavActivity> mFavActivity;
        private final WeakReference<IconHashClient> mIconHashClient;

        static class ViewHolder extends RecyclerView.ViewHolder {
            private final ConstraintLayout back;
            private final AppCompatImageView icon;
            private final AppCompatTextView title;
            private final AppCompatTextView url;

            public ViewHolder(View view) {
                super(view);
                back = view.findViewById(R.id.bg);
                icon = view.findViewById(R.id.icon);
                title = view.findViewById(R.id.title);
                url = view.findViewById(R.id.url);
            }
        }

        public ItemsAdapter(FavActivity favActivity, IconHashClient iconHashClient) {
            mFavActivity = new WeakReference<>(favActivity);
            mIconHashClient = new WeakReference<>(iconHashClient);
        }

        @NonNull
        @Override
        public FavActivity.ItemsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_list_broha, parent, false);

            return new FavActivity.ItemsAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FavActivity.ItemsAdapter.ViewHolder holder, int position) {
            final FavActivity favActivity = mFavActivity.get();
            Broha data = listData.get(position);
            String title = data.getTitle();
            String url = data.getUrl();

            holder.title.setText(title == null ? url : title);
            holder.url.setText(Uri.parse(url).getHost());

            holder.back.setOnClickListener(view -> {
                Intent needLoad = new Intent();
                needLoad.putExtra("needLoadUrl", url);
                favActivity.setResult(0, needLoad);
                favActivity.finish();
            });

            holder.back.setOnLongClickListener(view -> {
                PopupMenu popup1 = new PopupMenu(favActivity, view);
                Menu menu1 = popup1.getMenu();
                menu1.add(favActivity.getResources().getString(R.string.favMenuEdit));
                menu1.add(favActivity.getResources().getString(R.string.delete));
                menu1.add(favActivity.getResources().getString(android.R.string.copyUrl));
                popup1.setOnMenuItemClickListener(item -> {
                    if (item.getTitle().toString().equals(favActivity.getResources().getString(R.string.delete))) {
                        FavUtils.deleteById(favActivity, data.getId());
                        listData.remove(position);
                        notifyItemRangeRemoved(position, 1);
                        favActivity.isEmptyCheck();
                        return true;
                    } else if (item.getTitle().toString().equals(favActivity.getResources().getString(android.R.string.copyUrl))) {
                        CommonUtils.copyClipboard(favActivity, url);
                        return true;
                    } else if (item.getTitle().toString().equals(favActivity.getResources().getString(R.string.favMenuEdit))) {
                        final LayoutInflater layoutInflater = LayoutInflater.from(favActivity);
                        @SuppressLint("InflateParams") final View root = layoutInflater.inflate(R.layout.dialog_fav_edit, null);
                        final AppCompatEditText titleEditText = root.findViewById(R.id.titleEditText);
                        final AppCompatEditText urlEditText = root.findViewById(R.id.urlEditText);
                        titleEditText.setText(title);
                        urlEditText.setText(url);
                        new MaterialAlertDialogBuilder(favActivity)
                                .setTitle(favActivity.getResources().getString(R.string.favMenuEdit))
                                .setView(root)
                                .setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
                                    if (!Objects.requireNonNull(titleEditText.getText()).toString().equals(title)
                                            || !Objects.requireNonNull(urlEditText.getText()).toString().equals(url)) {
                                        data.setTitle(Objects.requireNonNull(titleEditText.getText()).toString());
                                        data.setUrl(Objects.requireNonNull(urlEditText.getText()).toString());
                                        data.setTimestamp();
                                        FavApi.favBroha(favActivity).updateBroha(data);
                                        listData = FavApi.favBroha(favActivity).getAll();
                                        notifyItemRangeRemoved(position, 1);
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, null)
                                .setIcon(holder.icon.getDrawable())
                                .create().show();
                        return true;
                    }
                    return false;
                });
                popup1.show();
                return true;
            });

            if (data.getIconHash() != null) {
                Bitmap icon = mIconHashClient.get().read(data.getIconHash());
                if (icon != null)
                    holder.icon.setImageBitmap(icon);
                else
                    holder.icon.setImageResource(R.drawable.default_favicon);
            } else {
                holder.icon.setImageResource(R.drawable.default_favicon);
            }
        }

        @Override
        public int getItemCount() {
            return listData.size();
        }
    }
}
