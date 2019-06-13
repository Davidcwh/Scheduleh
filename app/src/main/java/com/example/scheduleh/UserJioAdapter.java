package com.example.scheduleh;

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

public class UserJioAdapter extends FirestoreRecyclerAdapter<Event, UserJioAdapter.UserJioHolder> {

    public UserJioAdapter(@NonNull FirestoreRecyclerOptions<Event> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull UserJioHolder holder, int position, @NonNull Event model) {
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
