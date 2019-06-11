package com.example.scheduleh;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class FriendRequestAdapter extends FirestoreRecyclerAdapter<User, FriendRequestAdapter.FriendRequestHolder> {

    public FriendRequestAdapter(@NonNull FirestoreRecyclerOptions<User> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull final FriendRequestHolder holder, int position, @NonNull final User model) {
        holder.userDisplayNameTextView.setText(model.getDisplayName());

    }

    @NonNull
    @Override
    public FriendRequestHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.friend_request_item, viewGroup,false);
        return new FriendRequestHolder(v);
    }

    class FriendRequestHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView userDisplayNameTextView, confirmTextView, deleteTextView;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        public FriendRequestHolder(@NonNull View itemView) {
            super(itemView);
            userDisplayNameTextView = itemView.findViewById(R.id.friendRequestItem_displayName_textView);
            confirmTextView = itemView.findViewById(R.id.friendRequestItem_confirm_textView);
            deleteTextView = itemView.findViewById(R.id.friendRequestItem_delete_textView);

            confirmTextView.setOnClickListener(this);
            deleteTextView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // This document snapshot refers to the user object of the user who send the friend request
            DocumentSnapshot friendRequestSender = getSnapshots().getSnapshot(getAdapterPosition());
            switch (v.getId()) {
                case R.id.friendRequestItem_confirm_textView:
                    addToFriend(friendRequestSender);
                    deleteFriendRequest(friendRequestSender);
                    break;
                case R.id.friendRequestItem_delete_textView:
                    deleteFriendRequest(friendRequestSender);
                    break;
            }

        }

        // Adds the given document snapshot user to the current user's friend list and vice versa
        private void addToFriend(DocumentSnapshot documentSnapshot) {
            FirebaseUser currentUser = mAuth.getCurrentUser();

            //Adding the given document snapshot user to the current user's friend list
            db.collection("users").document(currentUser.getUid())
                            .collection("friend list")
                            .add(new User(documentSnapshot.get("id").toString(), documentSnapshot.get("displayName").toString()));

            // Adding the current user to the document snapshot user's friend list
            db.collection("users").document(documentSnapshot.get("id").toString())
                    .collection("friend list").add(new User(currentUser.getUid(), currentUser.getDisplayName()));

            Log.i(getClass().getName(), documentSnapshot.get("displayName") + " added to friends list");
        }

        // deletes the friend request from the user's friend request collection
        private void deleteFriendRequest(final DocumentSnapshot documentSnapshot) {
            db.collection("users").document(mAuth.getCurrentUser().getUid())
                    .collection("friend requests").document(documentSnapshot.getId())
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i(getClass().getName(), documentSnapshot.get("displayName") + " added to friend list");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i(getClass().getName(), documentSnapshot.get("displayName") + " not added to friend list " + e);
                        }
                    });

        }
    }
}
