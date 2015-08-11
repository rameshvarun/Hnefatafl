package net.varunramesh.hnefatafl;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by florentchampigny on 24/04/15.
 */
public class SavedGameViewAdapter extends RecyclerView.Adapter<SavedGameViewAdapter.ViewHolder> {

    List<SavedGame> contents;

    static final int TYPE_HEADER = 0;
    static final int TYPE_CELL = 1;

    public SavedGameViewAdapter(List<SavedGame> contents) {
        this.contents = contents;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView lastmove;
        public ViewHolder(View v) {
            super(v);
            lastmove = (TextView)v.findViewById(R.id.lastmove);
        }
    }

    @Override
    public int getItemCount() {
        return contents.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_card_big, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SavedGame game = contents.get(position);

        holder.lastmove.setText("Last Move: " + DateUtils.getRelativeTimeSpanString(game.getLastMoveDate().getTime()));
    }
}