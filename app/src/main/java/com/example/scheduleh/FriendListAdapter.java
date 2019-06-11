package com.example.scheduleh;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class FriendListAdapter extends FirestoreRecyclerAdapter<User, FriendListAdapter.FriendListHolder> {

    public FriendListAdapter(@NonNull FirestoreRecyclerOptions<User> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull FriendListHolder holder, int position, @NonNull User model) {
        holder.userDisplayNameTextView.setText(model.getDisplayName());
    }

    @NonNull
    @Override
    public FriendListHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.friend_list_item, viewGroup,false);
        return new FriendListHolder(v);
    }

    class FriendListHolder extends RecyclerView.ViewHolder {
        TextView userDisplayNameTextView;
        ImageButton friendProfilePic;

        public FriendListHolder(@NonNull View itemView) {
            super(itemView);
            userDisplayNameTextView = itemView.findViewById(R.id.friendListItem_displayName_textView);
            friendProfilePic = itemView.findViewById(R.id.friendListItem_profilePhoto_imageButton);
        }
    }
}
