package com.miware.clout.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.miware.clout.R;
import java.util.ArrayList;

public class MainFeedRecyclerViewAdapter extends RecyclerView.Adapter<MainFeedRecyclerViewAdapter.MyViewHolder> {

    private final Context   context;
    private ArrayList<String>          Transacting_Users[];
    private ArrayList<String>          Location[];
    private ArrayList<String>          Date[];
    private ArrayList<String>          Current_Date[];
    private ArrayList<String>          amount[];

    public MainFeedRecyclerViewAdapter(Context ct,
                                       ArrayList<String> transacting_Users[],
                                       ArrayList<String> location[],
                                       ArrayList<String> date[],
                                       ArrayList<String> Current_Date[],
                                       ArrayList<String> rate[]){

        context             = ct;
        Transacting_Users   = transacting_Users;
        Location            = location;
        Date                = date;
        Current_Date        = Current_Date;
        amount              = rate;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.card_view_list_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.getTransacting_Users.setText((CharSequence) Transacting_Users[position]);
        holder.getCurrent_date.setText((CharSequence) Current_Date[position]);
        holder.getDate.setText((CharSequence) Date[position]);
        holder.getAmount.setText((CharSequence) amount[position]);
        holder.getLocation.setText((CharSequence) Location[position]);
    }

    @Override
    public int getItemCount() {
        return 5;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView getTransacting_Users, getLocation, getDate, getAmount, getCurrent_date;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            getTransacting_Users = itemView.findViewById(R.id.Transacting_Users);
            getLocation     = itemView.findViewById(R.id.location);
            getDate         = itemView.findViewById(R.id.date);
            getCurrent_date  = itemView.findViewById(R.id.current_date);
            getAmount         = itemView.findViewById(R.id.amount);
        }
    }
}
