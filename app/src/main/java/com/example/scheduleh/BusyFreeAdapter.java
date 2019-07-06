package com.example.scheduleh;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class BusyFreeAdapter extends RecyclerView.Adapter<BusyFreeAdapter.BusyFreeViewHolder> {
    private ArrayList<String> usersList;

    public BusyFreeAdapter(ArrayList<String> usersList) {
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public BusyFreeViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.friend_list_item, viewGroup, false);
        return new BusyFreeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final BusyFreeViewHolder busyFreeViewHolder, int i) {
        String userId = usersList.get(i);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(userId)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    busyFreeViewHolder.userDisplayNameTextView.setText(task.getResult().get("displayName").toString());
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return this.usersList.size();
    }

    class BusyFreeViewHolder extends RecyclerView.ViewHolder {

        TextView userDisplayNameTextView;
        ImageButton friendProfilePic;

        public BusyFreeViewHolder(@NonNull View itemView) {
            super(itemView);

            userDisplayNameTextView = itemView.findViewById(R.id.friendListItem_displayName_textView);
            friendProfilePic = itemView.findViewById(R.id.friendListItem_profilePhoto_imageButton);
        }
    }
}
