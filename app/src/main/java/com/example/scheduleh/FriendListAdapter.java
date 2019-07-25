package com.example.scheduleh;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendListAdapter extends FirestoreRecyclerAdapter<User, FriendListAdapter.FriendListHolder> {
    private OnItemClickListener listener;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public FriendListAdapter(@NonNull FirestoreRecyclerOptions<User> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull final FriendListHolder holder, int position, @NonNull User model) {
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
    public FriendListHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.friend_list_item, viewGroup,false);
        return new FriendListHolder(v);
    }

    class FriendListHolder extends RecyclerView.ViewHolder {
        TextView userDisplayNameTextView;
        CircleImageView friendProfilePic;

        public FriendListHolder(@NonNull View itemView) {
            super(itemView);
            userDisplayNameTextView = itemView.findViewById(R.id.friendListItem_displayName_textView);
            friendProfilePic = itemView.findViewById(R.id.friendListItem_profilePhoto_imageButton);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.OnItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }

    }

    // used for selecting friends in friend list to sync schedules
    public interface OnItemClickListener {
        void OnItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
