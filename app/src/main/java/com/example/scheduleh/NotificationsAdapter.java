package com.example.scheduleh;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class NotificationsAdapter extends FirestoreRecyclerAdapter<Notification, NotificationsAdapter.NotificationHolder> {

    public NotificationsAdapter(@NonNull FirestoreRecyclerOptions<Notification> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull NotificationHolder holder, int position, @NonNull Notification model) {
        String header = "";

        if (model.getNotificationType().equals("joinOpenJio")) {
            header = "Someone joined your open jio";
        } else if (model.getNotificationType().equals("leaveOpenJio")) {
            header = "Someone left your open jio";
        } else if (model.getNotificationType().equals("sendFriendRequest")) {
            header = "New Friend Request";
        } else if (model.getNotificationType().equals("acceptFriendRequest")) {
            header = "Your Friend Request was accepted";
        }

        holder.notificationHeader.setText(header);
        holder.notificationMessage.setText(model.getMessage());

    }

    @NonNull
    @Override
    public NotificationHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.notification_item, viewGroup, false);
        return new NotificationHolder(v);
    }

    class NotificationHolder extends RecyclerView.ViewHolder {
        TextView notificationHeader;
        TextView notificationMessage;


        public NotificationHolder(@NonNull View itemView) {
            super(itemView);

            notificationHeader = itemView.findViewById(R.id.notification_item_header_textView);
            notificationMessage = itemView.findViewById(R.id.notification_item_message_textView);
        }
    }
}
