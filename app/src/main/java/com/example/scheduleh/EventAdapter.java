package com.example.scheduleh;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

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

        // if the user id of the event is not the same as the current user's, means it is an openjio event from a friend.
        // Thus, set the openjio status in the event item layout
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

            // When the event's openjio status is clicked, a popup menu with 2 options will appear:
            // See who is joining for that jio
            // Leave the jio
            textViewOpenJioStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    final FirebaseFirestore db = FirebaseFirestore.getInstance();
                    final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    final DocumentSnapshot eventSnapshot = getSnapshots().getSnapshot(getAdapterPosition());
                    final String eventUserId = eventSnapshot.get("userId").toString();
                    final Context context = v.getContext();

                    PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                    popupMenu.inflate(R.menu.openjio_popup_menu);

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.seeWhoComing_openJio_popupMenu:

                                    Intent intent = new Intent(context, FriendsJoiningActivity.class);

                                    intent.putExtra("eventId", eventSnapshot.getId());
                                    intent.putExtra("userId", eventUserId);
                                    context.startActivity(intent);
                                    return true;

                                case R.id.removeJio_openJio_popupMenu:

                                    // removing the current user from the friend's friends joining list for this event
                                    final DocumentReference userData = db.collection("users").document(eventUserId)
                                            .collection("user jios").document(eventSnapshot.getId())
                                            .collection("friends joined").document(currentUser.getUid());

                                    userData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.getResult().exists()) {
                                                userData.delete();
                                            }
                                        }
                                    });

                                    // Add this event back to the current user's collection of open jios from friends
                                    Map<String, Object> eventData = eventSnapshot.getData();
                                    db.collection("users").document(currentUser.getUid())
                                            .collection("friend jios").document(eventSnapshot.getId())
                                            .set(eventData);

                                    // Deleting the event form the current user's events collection
                                    db.collection("users").document(currentUser.getUid())
                                            .collection("events").document(eventSnapshot.getId())
                                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.getResult().exists()) {
                                                eventSnapshot.getReference().delete();
                                            }
                                        }
                                    });

                                    Toast.makeText(context, "Left jio!", Toast.LENGTH_SHORT).show();
                                    return true;

                                default:
                                    return false;

                            }
                        }
                    });

                    popupMenu.show();

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
