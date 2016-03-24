/*
* Copyright (C) 2014 The Android Open Source Project
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

package com.ricardotrujillo.prueba.viewmodel.adapter;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ricardotrujillo.prueba.App;
import com.ricardotrujillo.prueba.R;
import com.ricardotrujillo.prueba.databinding.StoreRowBinding;
import com.ricardotrujillo.prueba.model.EntryViewModel;
import com.ricardotrujillo.prueba.model.Store;
import com.ricardotrujillo.prueba.model.StoreManager;
import com.ricardotrujillo.prueba.view.LoadingFeedItemView;
import com.ricardotrujillo.prueba.viewmodel.Constants;
import com.ricardotrujillo.prueba.viewmodel.activity.EntryActivity;
import com.ricardotrujillo.prueba.viewmodel.interfaces.CustomCallback;
import com.ricardotrujillo.prueba.viewmodel.worker.AnimWorker;
import com.ricardotrujillo.prueba.viewmodel.worker.BusWorker;
import com.ricardotrujillo.prueba.viewmodel.worker.LogWorker;
import com.ricardotrujillo.prueba.viewmodel.worker.NetWorker;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import javax.inject.Inject;
/**
 * Created by Ricardo on 18/03/2016
 */
public class StoreRecyclerViewAdapter extends RecyclerView.Adapter<StoreRecyclerViewAdapter.BindingHolder> {

    public static final int VIEW_TYPE_DEFAULT = 1;
    public static final int VIEW_TYPE_LOADER = 2;
    static Activity activity;

    @Inject
    LogWorker logWorker;
    @Inject
    BusWorker busWorker;
    @Inject
    AnimWorker animWorker;
    @Inject
    StoreManager storeManager;

    private boolean showLoadingView = false;

    private int lastPosition = -1;

    public StoreRecyclerViewAdapter(Activity act) {

        activity = act;

        inject();
    }

    void inject() {

        ((App) activity.getApplication()).getAppComponent().inject(this);
    }

    @Override
    public BindingHolder onCreateViewHolder(ViewGroup parent, int type) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        StoreRowBinding binding = StoreRowBinding.inflate(inflater, parent, false);

        return new BindingHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(final BindingHolder holder, final int position) {

        holder.binding.setEntry(storeManager.getStore().feed.entry.get(position));

        holder.binding.setClick(new StoreClickHandler() {

            @Override
            public void onClick(View view) {

                onClickButton(view, holder);
            }
        });

        if (!storeManager.getStore().feed.entry.get(position).imageLoaded) {

            holder.binding.ivContainer.setAlpha(0f);
        }

        if (storeManager.getColorDrawable(storeManager.getStore().feed.entry.get(position).name.label) == null) {

            loadImage(holder.binding.ivFeedCenter, position, new CustomCallback() {
                @Override
                public void onSuccess() {

                    Bitmap myBitmap = ((BitmapDrawable) holder.binding.ivFeedCenter.getDrawable()).getBitmap();

                    if (myBitmap != null && !myBitmap.isRecycled()) {

                        Palette.from(myBitmap).generate(new Palette.PaletteAsyncListener() {

                            public void onGenerated(Palette palette) {

                                if (position < storeManager.getStore().feed.entry.size()) {

                                    if (!storeManager.getStore().feed.entry.get(position).imageLoaded) {

                                        holder.binding.ivContainer.animate().setDuration(500).alpha(1f);

                                        storeManager.getStore().feed.entry.get(position).imageLoaded = true; //First insert animation
                                    }

                                    storeManager.getStore().feed.entry.get(position).paletteColor = animWorker.getDarkColorDrawable(palette).getColor();

                                    holder.binding.ivContainer.setBackgroundDrawable(animWorker.getDarkColorDrawable(palette)); // min supported API is 14

                                    storeManager.addDrawables(position, animWorker.getDarkColorDrawable(palette));
                                }
                            }
                        });
                    }
                }

                @Override
                public void onError() {

                }
            });

        } else {

            loadImage(holder.binding.ivFeedCenter, position, new CustomCallback() {
                @Override
                public void onSuccess() {

                    holder.binding.ivContainer.setBackgroundDrawable(storeManager.getColorDrawable(storeManager.getStore().feed.entry.get(position).name.label)); // min supported API is 14

                    holder.binding.ivContainer.setAlpha(1f);
                }

                @Override
                public void onError() {

                }
            });
        }

        holder.getBinding(position).executePendingBindings();

        setAnimation(holder.binding.cardView, position);
    }

    private void setAnimation(View viewToAnimate, int position) {

        if (position > lastPosition) {

            Animation animation = AnimationUtils.loadAnimation(activity, R.anim.slide_in_bottom);

            viewToAnimate.startAnimation(animation);

            lastPosition = position;

        }
    }

    void loadImage(ImageView view, final int position, final CustomCallback callback) {

        Picasso.with(view.getContext())
                .load(storeManager.getStore().feed.entry.get(position).image[2].label)
                .networkPolicy(
                        NetWorker.isConnected(activity) ?
                                NetworkPolicy.NO_CACHE : NetworkPolicy.OFFLINE)
                .noFade()
                .into(view, new Callback() {
                    @Override
                    public void onSuccess() {

                        callback.onSuccess();
                    }

                    @Override
                    public void onError() {

                    }
                });
    }

    public void onClickButton(View view, BindingHolder holder) {

        switch (view.getId()) {

            case R.id.ivFeedCenter:

                openEntryActivity(holder);

                break;

            case R.id.ivContainerParent:

                openEntryActivity(holder);

                break;

            default:

                break;
        }
    }

    @Override
    public void onViewDetachedFromWindow(BindingHolder holder) {

        holder.clearAnimation();
    }

    @Override
    public int getItemCount() {

        if (storeManager.getStore() != null) {

            return storeManager.getStore().feed.entry.size();

        } else {

            return 0;
        }
    }

    void openEntryActivity(BindingHolder holder) {

        Intent intent = new Intent(activity, EntryActivity.class);

        intent.putExtra(Constants.POSITION, holder.getLayoutPosition());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            holder.binding.ivFeedCenter.setTransitionName(activity.getString(R.string.entry_transition_name));
            holder.binding.tvName.setTransitionName(activity.getString(R.string.entry_transition_name));

            Pair<View, String> p1 = Pair.create((View) holder.binding.vImageRoot, activity.getString(R.string.entry_transition_thumb));

            @SuppressWarnings("unchecked")
            ActivityOptionsCompat options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(activity, p1);

            activity.startActivity(intent, options.toBundle());

        } else {

            activity.startActivity(intent);
        }
    }

    public void updateList(ArrayList<Store.Feed.Entry> data) {

        storeManager.getStore().feed.entry = new ArrayList<>();

        storeManager.getStore().feed.entry.addAll(data);

        notifyDataSetChanged();
    }

    public interface StoreClickHandler {

        void onClick(View view);
    }

    public class BindingHolder extends RecyclerView.ViewHolder {

        StoreRowBinding binding;

        public BindingHolder(View v) {

            super(v);

            binding = DataBindingUtil.bind(v);

            binding.setViewModel(new EntryViewModel(activity));
        }

        public StoreRowBinding getBinding(int position) {

            int bottomMargin;

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) binding.cardView.getLayoutParams();

            if (position == storeManager.getStore().feed.entry.size() - 1) {

                bottomMargin = animWorker.dpToPx(8);

                params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, bottomMargin);

            } else {

                bottomMargin = animWorker.dpToPx(0);

                params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, bottomMargin);
            }

            binding.cardView.setLayoutParams(params);

            return binding;
        }

        public void clearAnimation() {

            binding.getRoot().clearAnimation();
        }
    }
}
