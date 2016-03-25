package com.ricardotrujillo.prueba.viewmodel.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ricardotrujillo.prueba.App;
import com.ricardotrujillo.prueba.R;
import com.ricardotrujillo.prueba.databinding.StoreFragmentBinding;
import com.ricardotrujillo.prueba.model.Store;
import com.ricardotrujillo.prueba.model.StoreManager;
import com.ricardotrujillo.prueba.viewmodel.Constants;
import com.ricardotrujillo.prueba.viewmodel.adapter.StoreRecyclerViewAdapter;
import com.ricardotrujillo.prueba.viewmodel.event.FetchedStoreDataEvent;
import com.ricardotrujillo.prueba.viewmodel.event.RecyclerCellEvent;
import com.ricardotrujillo.prueba.viewmodel.event.SplashDoneEvent;
import com.ricardotrujillo.prueba.viewmodel.worker.BusWorker;
import com.ricardotrujillo.prueba.viewmodel.worker.LogWorker;
import com.ricardotrujillo.prueba.viewmodel.worker.NetWorker;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import javax.inject.Inject;

public class StoreFragment extends Fragment {

    public static StoreRecyclerViewAdapter adapter;
    protected final int SPAN_COUNT = Constants.SPAN_COUNT;
    protected final String KEY_LAYOUT_MANAGER = Constants.LAYOUT_MANAGER;
    private final int DATASET_COUNT = 15;
    protected LayoutManagerType mCurrentLayoutManagerType;
    protected RecyclerView.LayoutManager mLayoutManager;

    @Inject
    BusWorker busWorker;
    @Inject
    LogWorker logWorker;
    @Inject
    NetWorker netWorker;
    @Inject
    StoreManager storeManager;

    StoreFragmentBinding binding;

    public StoreFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inject();
    }

    @Override
    public void onPause() {
        super.onPause();

        busWorker.unRegister(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        busWorker.register(this);
    }

    void inject() {

        ((App) getActivity().getApplication()).getAppComponent().inject(this);
    }

    @Subscribe
    public void recievedMessage(FetchedStoreDataEvent event) {

        //adapter.notifyDataSetChanged();
    }

    @Subscribe
    public void recievedMessage(SplashDoneEvent event) {

        adapter.notifyDataSetChanged();
    }

    @Subscribe
    public void recievedMessage(RecyclerCellEvent event) {

        filterBy(event.getString());
    }

    public void filterBy(String query) {

        final ArrayList<Store.Feed.Entry> filteredModelList = filter(storeManager.getStore().feed.originalEntry, query);

        animateTo(filteredModelList);

        binding.storeRecyclerView.scrollToPosition(0);
    }

    private ArrayList<Store.Feed.Entry> filter(ArrayList<Store.Feed.Entry> entries, String query) {

        if (query.equals(getString(R.string.all_apps))) {

            return entries;

        } else {

            query = query.toLowerCase();

            final ArrayList<Store.Feed.Entry> filteredModelList = new ArrayList<>();

            for (Store.Feed.Entry entry : entries) {

                final String text = entry.category.attributes.label.toLowerCase();

                if (text.contains(query)) {

                    filteredModelList.add(entry);
                }
            }

            return filteredModelList;
        }
    }

    public void animateTo(ArrayList<Store.Feed.Entry> entries) {

        applyAndAnimateRemovals(entries);
        applyAndAnimateAdditions(entries);
        applyAndAnimateMovedItems(entries);
    }

    private void applyAndAnimateRemovals(ArrayList<Store.Feed.Entry> newModels) {

        for (int i = storeManager.getStore().feed.entry.size() - 1; i >= 0; i--) {

            final Store.Feed.Entry model = storeManager.getStore().feed.entry.get(i);

            if (!newModels.contains(model)) {

                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(ArrayList<Store.Feed.Entry> newModels) {

        for (int i = 0, count = newModels.size(); i < count; i++) {

            final Store.Feed.Entry model = newModels.get(i);

            if (!storeManager.getStore().feed.entry.contains(model)) {

                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(ArrayList<Store.Feed.Entry> newModels) {

        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {

            final Store.Feed.Entry model = newModels.get(toPosition);

            final int fromPosition = storeManager.getStore().feed.entry.indexOf(model);

            if (fromPosition >= 0 && fromPosition != toPosition) {

                moveItem(fromPosition, toPosition);
            }
        }
    }

    public Store.Feed.Entry removeItem(int position) {

        final Store.Feed.Entry entry = storeManager.getStore().feed.entry.remove(position);

        adapter.notifyItemRemoved(position);

        return entry;
    }

    public void addItem(int position, Store.Feed.Entry model) {

        storeManager.getStore().feed.entry.add(position, model);

        adapter.notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {

        final Store.Feed.Entry model = storeManager.getStore().feed.entry.remove(fromPosition);

        storeManager.getStore().feed.entry.add(toPosition, model);

        adapter.notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.store_fragment, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        initRecyclerView(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        savedInstanceState.putSerializable(KEY_LAYOUT_MANAGER, mCurrentLayoutManagerType); // Save currently selected layout manager.

        super.onSaveInstanceState(savedInstanceState);
    }

    void initRecyclerView(Bundle savedInstanceState) {

        mLayoutManager = new LinearLayoutManager(getActivity());

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

            mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;

        } else {

            mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
        }

        setRecyclerViewLayoutManager(mCurrentLayoutManagerType);

        adapter = new StoreRecyclerViewAdapter(getActivity());

        binding.storeRecyclerView.setAdapter(adapter);
    }

    public void setRecyclerViewLayoutManager(LayoutManagerType layoutManagerType) {

        int scrollPosition = 0;

        if (binding.storeRecyclerView.getLayoutManager() != null) {

            scrollPosition = ((LinearLayoutManager) binding.storeRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        switch (layoutManagerType) {

            case GRID_LAYOUT_MANAGER:

                mLayoutManager = new GridLayoutManager(getActivity(), SPAN_COUNT);
                mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
                break;

            case LINEAR_LAYOUT_MANAGER:

                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
                break;

            default:

                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        }

        binding.storeRecyclerView.setLayoutManager(mLayoutManager);
        binding.storeRecyclerView.scrollToPosition(scrollPosition);
    }

    private enum LayoutManagerType {

        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }
}
