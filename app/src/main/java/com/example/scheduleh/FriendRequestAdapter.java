package com.example.scheduleh;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendRequestAdapter extends FirestoreRecyclerAdapter<User, FriendRequestAdapter.FriendRequestHolder> {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public FriendRequestAdapter(@NonNull FirestoreRecyclerOptions<User> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull final FriendRequestHolder holder, int position, @NonNull final User model) {
        holder.userDisplayNameTextView.setText(model.getDisplayName());
        DocumentReference Ref = db.collection("users").document(model.getId());
        Ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> taskPhoto) {
                if(taskPhoto.isSuccessful()){
                    DocumentSnapshot Document = taskPhoto.getResult();
                    if(Document.exists()){

                        if (Document.getString("photoUrl") != null) {
                            //if not null, we will add the photo to the imageview of the profile screen
                            Glide.with(holder.friendProfilePic.getContext())
                                    .load(Document.getString("photoUrl"))
                                    .into(holder.friendProfilePic);
                        } else {
                            holder.friendProfilePic.setImageResource(R.drawable.ic_home_default_profile_pic);
                        }

                    }
                }
            }
        });

    }

    @NonNull
    @Override
    public FriendRequestHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.friend_request_item, viewGroup,false);
        return new FriendRequestHolder(v);
    }

    class FriendRequestHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView userDisplayNameTextView, confirmTextView, deleteTextView;
        CircleImageView friendProfilePic;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        public FriendRequestHolder(@NonNull View itemView) {
            super(itemView);
            userDisplayNameTextView = itemView.findViewById(R.id.friendRequestItem_displayName_textView);
            confirmTextView = itemView.findViewById(R.id.friendRequestItem_confirm_textView);
            deleteTextView = itemView.findViewById(R.id.friendRequestItem_delete_textView);
            friendProfilePic = itemView.findViewById(R.id.friendRequestItem_profilePhoto_imageButton);

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
        private void addToFriend(final DocumentSnapshot documentSnapshot) {
            final FirebaseUser currentUser = mAuth.getCurrentUser();

            //Adding the given document snapshot user to the current user's friend list
            Map<String, Object> addFriendToUser = new HashMap<>();
            addFriendToUser.put("id", documentSnapshot.getId());
            addFriendToUser.put("displayName", documentSnapshot.get("displayName").toString());
            db.collection("users").document(currentUser.getUid())
                            .collection("friend list")
                            .document(documentSnapshot.getId())
                            .set(addFriendToUser);

            // Adding the current user to the document snapshot user's friend list
            Map<String, Object> addUserToFriend = new HashMap<>();
            addUserToFriend.put("id", currentUser.getUid());
            addUserToFriend.put("displayName", currentUser.getDisplayName());
            db.collection("users").document(documentSnapshot.getId())
                    .collection("friend list")
                    .document(currentUser.getUid())
                    .set(addUserToFriend);

            // Adding the document snapshot user's openjio events to current user's friend jios
            db.collection("users").document(documentSnapshot.getId())
                    .collection("user jios").get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (!task.getResult().isEmpty()) {
                                for (QueryDocumentSnapshot jioEvent: task.getResult()) {
                                    db.collection("users").document(currentUser.getUid())
                                            .collection("friend jios").document(jioEvent.getId())
                                            .set(jioEvent.getData());
                                }
                            }
                        }
                    });

            // Adding the current user's openjio events to document snapshot user's friend jios
            db.collection("users").document(currentUser.getUid())
                    .collection("user jios").get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (!task.getResult().isEmpty()) {
                                for (QueryDocumentSnapshot jioEvent: task.getResult()) {
                                    db.collection("users").document(documentSnapshot.getId())
                                            .collection("friend jios").document(jioEvent.getId())
                                            .set(jioEvent.getData());
                                }
                            }
                        }
                    });

            // sending notification
            String message = mAuth.getCurrentUser().getDisplayName() + " and you are now friends";
            Notification notification = new Notification(message, mAuth.getCurrentUser().getUid(), "acceptFriendRequest");
            db.collection("users").document(documentSnapshot.getId())
                    .collection("notifications").add(notification);

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
