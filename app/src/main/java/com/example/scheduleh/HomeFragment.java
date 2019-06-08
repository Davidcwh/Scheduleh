package com.example.scheduleh;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private TextView userDisplayName;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, null);

        view.findViewById(R.id.homeSettings).setOnClickListener(this);
        view.findViewById(R.id.homeFriends).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.homeSettings:
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                break;

            case R.id.homeFriends:
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        mAuth = FirebaseAuth.getInstance();
        userDisplayName = getView().findViewById(R.id.homeUserDisplayName);
        userDisplayName.setText(mAuth.getCurrentUser().getDisplayName());

    }
}
