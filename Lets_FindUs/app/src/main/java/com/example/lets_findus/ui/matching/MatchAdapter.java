package com.example.lets_findus.ui.matching;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lets_findus.R;
import com.example.lets_findus.utilities.MeetingPerson;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
//adapter per la recyclerView della lista dei meeting nel fragment meeting
public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.MyViewHolder>{

    private List<MeetingPerson> dataSet; //dataset contenente i meeting

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textViewNickname;
        TextView textViewData;
        TextView textViewTime;
        ImageView imageViewProfileImage;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.textViewNickname = itemView.findViewById(R.id.card_nickname);
            this.textViewData = itemView.findViewById(R.id.card_data_label);
            this.textViewTime = itemView.findViewById(R.id.card_time_label);
            this.imageViewProfileImage = itemView.findViewById(R.id.card_circular_profile_image);
        }
    }

    public MatchAdapter(List<MeetingPerson> data) {
        this.dataSet = data;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cards_layout, parent, false);

        view.setOnClickListener(MatchingFragment.myOnClickListener);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        TextView textViewNickname = holder.textViewNickname;
        TextView textViewData = holder.textViewData;
        TextView textViewTime = holder.textViewTime;
        ImageView imageView = holder.imageViewProfileImage;

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        DateFormat timeFormat = new SimpleDateFormat("HH:mm");

        textViewNickname.setText(dataSet.get(position).person.nickname);
        textViewData.setText(dateFormat.format(dataSet.get(position).meeting.date));
        textViewTime.setText(timeFormat.format(dataSet.get(position).meeting.date));
        imageView.setImageURI(Uri.parse(dataSet.get(position).person.profilePath));
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
