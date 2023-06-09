/*
 * Copyright (C) 2020-2023 Tipz Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tipz.browservio.broha;

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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import tipz.browservio.Application;
import tipz.browservio.BrowservioActivity;
import tipz.browservio.R;
import tipz.browservio.broha.api.FavApi;
import tipz.browservio.broha.api.FavUtils;
import tipz.browservio.broha.api.HistoryApi;
import tipz.browservio.broha.api.HistoryUtils;
import tipz.browservio.broha.database.Broha;
import tipz.browservio.broha.database.icons.IconHashClient;
import tipz.browservio.utils.CommonUtils;

public class BrohaListInterfaceActivity extends BrowservioActivity {
    private static List<Broha> listData;

    /* Activity mode */
    public static String activityMode;
    public static final String mode_history = "HISTORY";
    public static final String mode_favorites = "FAVORITES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMode = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        if (!activityMode.equals(mode_history) && !activityMode.equals(mode_favorites))
            finish();

        setContentView(R.layout.recycler_broha_list_activity);
        initialize();
        setTitle(getResources().getString(activityMode.equals(mode_history) ? R.string.hist : R.string.fav));
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
                .setMessage(getResources().getString(activityMode.equals(mode_history) ? R.string.del_hist_message : R.string.delete_fav_message))
                .setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
                    if (activityMode.equals(mode_history))
                        HistoryUtils.clear(this);
                    else if (activityMode.equals(mode_favorites))
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
        isEmptyCheck();
        RecyclerView brohaList = findViewById(R.id.recyclerView);
        listData = activityMode.equals(mode_history) ?
                HistoryApi.historyBroha(this).getAll() : FavApi.favBroha(this).getAll();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, activityMode.equals(mode_history));
        if (activityMode.equals(mode_history))
            layoutManager.setStackFromEnd(true);
        brohaList.setLayoutManager(layoutManager);
        brohaList.setAdapter(new BrohaListInterfaceActivity.ItemsAdapter(this, ((Application) getApplicationContext()).iconHashClient));
    }

    void isEmptyCheck() {
        if (activityMode.equals(mode_history) ? HistoryUtils.isEmptyCheck(this) : FavUtils.isEmptyCheck(this)) {
            CommonUtils.showMessage(this, getResources().getString(
                    activityMode.equals(mode_history) ? R.string.hist_empty : R.string.fav_list_empty));
            finish();
        }
    }

    public static class ItemsAdapter extends RecyclerView.Adapter<BrohaListInterfaceActivity.ItemsAdapter.ViewHolder> {
        private final WeakReference<BrohaListInterfaceActivity> mBrohaListInterfaceActivity;
        private final WeakReference<IconHashClient> mIconHashClient;

        static class ViewHolder extends RecyclerView.ViewHolder {
            private final ConstraintLayout back;
            private final AppCompatImageView icon;
            private final AppCompatTextView title;
            private final AppCompatTextView url;
            private final AppCompatTextView time;

            public ViewHolder(View view) {
                super(view);
                back = view.findViewById(R.id.bg);
                icon = view.findViewById(R.id.icon);
                title = view.findViewById(R.id.title);
                url = view.findViewById(R.id.url);
                time = view.findViewById(R.id.time);
            }
        }

        public ItemsAdapter(BrohaListInterfaceActivity brohaListInterfaceActivity, IconHashClient iconHashClient) {
            mBrohaListInterfaceActivity = new WeakReference<>(brohaListInterfaceActivity);
            mIconHashClient = new WeakReference<>(iconHashClient);
        }

        @NonNull
        @Override
        public BrohaListInterfaceActivity.ItemsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_broha_list_item, parent, false);

            return new BrohaListInterfaceActivity.ItemsAdapter.ViewHolder(view);
        }

        @SuppressLint("SimpleDateFormat")
        @Override
        public void onBindViewHolder(@NonNull BrohaListInterfaceActivity.ItemsAdapter.ViewHolder holder, int position) {
            final BrohaListInterfaceActivity brohaListInterfaceActivity = mBrohaListInterfaceActivity.get();
            final IconHashClient iconHashClient = mIconHashClient.get();
            Broha data = listData.get(position);
            String title = data.getTitle();
            String url = data.getUrl();
            Bitmap icon = iconHashClient.read(data.getIconHash());

            holder.title.setText(title == null ? url : title);
            holder.url.setText(Uri.parse(url).getHost());
            if (activityMode.equals(mode_history)) {
                Calendar date = Calendar.getInstance();
                date.setTimeInMillis(data.getTimestamp() * 1000L);
                holder.time.setText(new SimpleDateFormat("dd/MM\nHH:ss").format(date.getTime()));
            }

            holder.back.setOnClickListener(view -> {
                Intent needLoad = new Intent();
                needLoad.putExtra("needLoadUrl", url);
                brohaListInterfaceActivity.setResult(0, needLoad);
                brohaListInterfaceActivity.finish();
            });

            holder.back.setOnLongClickListener(view -> {
                PopupMenu popup1 = new PopupMenu(brohaListInterfaceActivity, view);
                Menu menu1 = popup1.getMenu();
                if (activityMode.equals(mode_history)) {
                    menu1.add(brohaListInterfaceActivity.getResources().getString(R.string.delete));
                    menu1.add(brohaListInterfaceActivity.getResources().getString(R.string.copy_url));
                    menu1.add(brohaListInterfaceActivity.getResources().getString(R.string.add_to_fav));
                } else if (activityMode.equals(mode_favorites)) {
                    menu1.add(brohaListInterfaceActivity.getResources().getString(R.string.favMenuEdit));
                    menu1.add(brohaListInterfaceActivity.getResources().getString(R.string.copy_url));
                    menu1.add(brohaListInterfaceActivity.getResources().getString(R.string.delete));
                }
                popup1.setOnMenuItemClickListener(item -> {
                    if (item.getTitle().toString().equals(brohaListInterfaceActivity.getResources().getString(R.string.delete))) {
                        if (activityMode.equals(mode_history))
                            HistoryUtils.deleteById(brohaListInterfaceActivity, data.getId());
                        else if (activityMode.equals(mode_favorites))
                            FavUtils.deleteById(brohaListInterfaceActivity, data.getId());
                        listData.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeRemoved(position, getItemCount() - position);
                        brohaListInterfaceActivity.isEmptyCheck();
                    } else if (item.getTitle().toString().equals(brohaListInterfaceActivity.getResources().getString(R.string.copy_url))) {
                        CommonUtils.copyClipboard(brohaListInterfaceActivity, url);
                    } else if (item.getTitle().toString().equals(brohaListInterfaceActivity.getResources().getString(R.string.favMenuEdit))) {
                        final LayoutInflater layoutInflater = LayoutInflater.from(brohaListInterfaceActivity);
                        @SuppressLint("InflateParams") final View root = layoutInflater.inflate(R.layout.dialog_fav_edit, null);
                        final AppCompatEditText titleEditText = root.findViewById(R.id.titleEditText);
                        final AppCompatEditText urlEditText = root.findViewById(R.id.urlEditText);
                        titleEditText.setText(title);
                        urlEditText.setText(url);
                        new MaterialAlertDialogBuilder(brohaListInterfaceActivity)
                                .setTitle(brohaListInterfaceActivity.getResources().getString(R.string.favMenuEdit))
                                .setView(root)
                                .setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
                                    if (!Objects.requireNonNull(titleEditText.getText()).toString().equals(title)
                                            || !Objects.requireNonNull(urlEditText.getText()).toString().equals(url)) {
                                        data.setTitle(Objects.requireNonNull(titleEditText.getText()).toString());
                                        data.setUrl(Objects.requireNonNull(urlEditText.getText()).toString());
                                        data.setTimestamp();
                                        FavApi.favBroha(brohaListInterfaceActivity).updateBroha(data);
                                        listData = FavApi.favBroha(brohaListInterfaceActivity).getAll();
                                        notifyItemRangeRemoved(position, 1);
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, null)
                                .setIcon(holder.icon.getDrawable())
                                .create().show();
                    } else if (item.getTitle().toString().equals(brohaListInterfaceActivity.getResources().getString(R.string.add_to_fav))) {
                        FavUtils.appendData(brohaListInterfaceActivity, iconHashClient, title, url, icon);
                        CommonUtils.showMessage(brohaListInterfaceActivity, brohaListInterfaceActivity.getResources().getString(R.string.save_successful));
                    } else {
                        return false;
                    }
                    return true;
                });
                popup1.show();
                return true;
            });

            if (data.getIconHash() != null && icon != null)
                holder.icon.setImageBitmap(icon);
            else
                holder.icon.setImageResource(R.drawable.default_favicon);
        }

        @Override
        public int getItemCount() {
            return listData.size();
        }
    }
}
