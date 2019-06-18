package com.example.scheduleh;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class UserJioAdapter extends FirestoreRecyclerAdapter<Event, UserJioAdapter.UserJioHolder> {

    public UserJioAdapter(@NonNull FirestoreRecyclerOptions<Event> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull UserJioHolder holder, int position, @NonNull Event model) {
        holder.textViewStartTime.setText(model.getStartTime());
        holder.textViewEndTime.setText(model.getEndTime());
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

        holder.textViewDate.setText(model.getDay() + " " + convertIntToMonth(model.getMonth()) + ", " + model.getYear());
    }

    @NonNull
    @Override
    public UserJioHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.userjio_item, viewGroup, false);
        return new UserJioHolder(v);
    }

    class UserJioHolder extends RecyclerView.ViewHolder {
        TextView textViewStartTime;
        TextView textViewEndTime;
        TextView textViewEventName;
        TextView textViewEventColor;
        TextView textViewPriority;

        TextView textViewDate;
        Button buttonFriendsJoining;
        Button buttonRemoveJio;


        public UserJioHolder(@NonNull View itemView) {
            super(itemView);

            textViewStartTime = itemView.findViewById(R.id.userJio_start_time);
            textViewEndTime = itemView.findViewById(R.id.userJio_end_time);
            textViewEventName = itemView.findViewById(R.id.userJio_name);
            textViewEventColor = itemView.findViewById(R.id.userJio_color);
            textViewPriority = itemView.findViewById(R.id.userJio_priority);

            textViewDate = itemView.findViewById(R.id.userJio_date_textView);
            buttonFriendsJoining = itemView.findViewById(R.id.userJio_friendsJoining_button);
            buttonRemoveJio = itemView.findViewById(R.id.userJio_removeJio_button);

            buttonFriendsJoining.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, FriendsJoiningActivity.class);
                    intent.putExtra("eventId", getSnapshots().getSnapshot(getAdapterPosition()).getId());
                    intent.putExtra("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    context.startActivity(intent);
                }
            });

            // Removes user's event from openjio
            buttonRemoveJio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final FirebaseFirestore db = FirebaseFirestore.getInstance();
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    final DocumentSnapshot documentSnapshot = getSnapshots().getSnapshot(getAdapterPosition());

                    //Delete the event in the user's user jios collection
                    db.collection("users").document(currentUser.getUid())
                            .collection("user jios").document(documentSnapshot.getId())
                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.getResult().exists()) {
                                task.getResult().getReference().delete();
                            }
                        }
                    });

                    CollectionReference allFriends = db.collection("users").document(currentUser.getUid())
                            .collection("friend list");
                    allFriends.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot friend: task.getResult()) {
                                    // Delete user's jio from each friend's friend jios collection, if it is inside it.
                                    db.collection("users").document(friend.getId())
                                            .collection("friend jios").document(documentSnapshot.getId())
                                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.getResult().exists()) {
                                                task.getResult().getReference().delete();
                                            }
                                        }
                                    });

                                    // If user's jio is not in friend's friend jios collection, means the friend already joined
                                    // the jio and the event is saved in the friend's events collection.
                                    // Thus, delete user's jio from each friend's friend jios collection, if it is inside it.
                                    db.collection("users").document(friend.getId())
                                            .collection("events").document(documentSnapshot.getId())
                                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.getResult().exists()) {
                                                task.getResult().getReference().delete();
                                            }
                                        }
                                    });


                                }
                            }
                        }
                    });


                }
            });

        }
    }

    // For displaying the selected date
    private String convertIntToMonth(int num) {
        switch (num) {
            default:
                return null;
            case 1:
                return "January";
            case 2:
                return "February";
            case 3:
                return "March";
            case 4:
                return "April";
            case 5:
                return "May";
            case 6:
                return "June";
            case 7:
                return "July";
            case 8:
                return "August";
            case 9:
                return "September";
            case 10:
                return "October";
            case 11:
                return "November";
            case 12:
                return "December";

        }
    }
}
