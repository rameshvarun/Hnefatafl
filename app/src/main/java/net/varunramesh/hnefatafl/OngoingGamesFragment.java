package net.varunramesh.hnefatafl;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.florent37.materialviewpager.adapter.RecyclerViewMaterialAdapter;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;

import java.util.List;

import io.realm.Realm;

/**
 * Created by Varun on 8/15/2015.
 */
public class OngoingGamesFragment extends Fragment {
    private ObservableScrollView scrollView;
    private Button openInbox;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ongoing_games_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        scrollView = (ObservableScrollView) view.findViewById(R.id.scrollView);
        MaterialViewPagerHelper.registerScrollView(getActivity(), scrollView, null);

        openInbox = (Button) view.findViewById(R.id.show_inbox);
        openInbox.setOnClickListener((View v) -> {
            MainActivity activity = (MainActivity) getActivity();
            activity.openInbox();
        });
    }
}
