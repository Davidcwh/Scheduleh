package com.example.scheduleh;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

public class EventAdapter extends FirestoreRecyclerAdapter<Event, EventAdapter.EventHolder> {
    private OnLongItemClickListener listener;

    public EventAdapter(@NonNull FirestoreRecyclerOptions<Event> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull EventHolder holder, int position, @NonNull Event model) {
        String AMPM = "AM";

        if (model.getStartTime() >= 12) {
            AMPM = "PM";
        }
        holder.textViewStartTime.setText(model.getStartTime() + " " + AMPM);

        if (model.getEndTime() >= 12) {
            AMPM = "PM";
        }
        holder.textViewEndTime.setText(model.getEndTime() + " " + AMPM);

        holder.textViewEventName.setText(model.getEventName());
    }

    @NonNull
    @Override
    public EventHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.event_item, viewGroup, false);
        return new EventHolder(v);
    }

    class EventHolder extends RecyclerView.ViewHolder {
        TextView textViewStartTime;
        TextView textViewEndTime;
        TextView textViewEventName;
        TextView textViewEventColor;


        public EventHolder(@NonNull View itemView) {
            super(itemView);
            textViewStartTime = itemView.findViewById(R.id.event_start_time);
            textViewEndTime = itemView.findViewById(R.id.event_end_time);
            textViewEventName = itemView.findViewById(R.id.event_name);
            textViewEventColor = itemView.findViewById(R.id.event_color);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.OnLongItemClick(getSnapshots().getSnapshot(position), position);
                    }
                    return true;
                }
            });
        }
    }

    public interface OnLongItemClickListener {
        // To send any other info to next activity upon click, can add it as a parameter in this method.
        void OnLongItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnLongItemClickListener(OnLongItemClickListener listener) {
        this.listener = listener;
    }

}
