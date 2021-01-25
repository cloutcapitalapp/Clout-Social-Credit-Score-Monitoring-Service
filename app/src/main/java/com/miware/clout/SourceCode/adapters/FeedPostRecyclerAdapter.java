package com.miware.clout.SourceCode.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.miware.clout.R;
import com.miware.clout.SourceCode.datamodels.postFeedDataModle;

public class FeedPostRecyclerAdapter extends
        RecyclerView.Adapter<FeedPostRecyclerAdapter.PostViewHolder> {

    public FeedPostRecyclerAdapter(
            @NonNull FirebaseRecyclerOptions<postFeedDataModle> options)
    {
        super();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view_list_item, parent, false);

        return new FeedPostRecyclerAdapter.PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        holder.post_TextView.setText(postFeedDataModle.getPostString());
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {

        TextView post_TextView;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            post_TextView = itemView.findViewById(R.id.post_TextView);
        }
    }
}
