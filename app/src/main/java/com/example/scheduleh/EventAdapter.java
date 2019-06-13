package com.example.scheduleh;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
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

        if (model.getPriority() == 1) {
            holder.textViewPriority.setText("Low Priority");
            holder.textViewPriority.setTextColor(Color.parseColor("#00cf78"));
        } else if (model.getPriority() == 2) {
            holder.textViewPriority.setText("Medium Priority");
            holder.textViewPriority.setTextColor(Color.parseColor("#FFFF00"));
        } else if (model.getPriority() == 3) {
            holder.textViewPriority.setText("High Priority");
            holder.textViewPriority.setTextColor(Color.parseColor("#d11a2a"));
        }

        if (!FirebaseAuth.getInstance().getCurrentUser().getUid().equals(model.getUserId())) {
            holder.textViewOpenJioStatus.setText("OpenJio from " + model.getDisplayName());
        }
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
        TextView textViewPriority;
        TextView textViewOpenJioStatus;

        public EventHolder(@NonNull View itemView) {
            super(itemView);
            textViewStartTime = itemView.findViewById(R.id.event_start_time);
            textViewEndTime = itemView.findViewById(R.id.event_end_time);
            textViewEventName = itemView.findViewById(R.id.event_name);
            textViewEventColor = itemView.findViewById(R.id.event_color);
            textViewPriority = itemView.findViewById(R.id.event_priority);
            textViewOpenJioStatus = itemView.findViewById(R.id.event_openJio_status);

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
