package com.example.keeptrack;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.keeptrack.Models.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class InboxFragment extends Fragment {

    private View RequestsFragmentView;
    private RecyclerView myRequestsList;

    private DatabaseReference RequestsRef, UserRef, DevicesRef;
    private FirebaseAuth mAuth;
    private String currentUserID;


    public InboxFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RequestsFragmentView = inflater.inflate(R.layout.fragment_inbox, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        RequestsRef = FirebaseDatabase.getInstance().getReference().child("Requests");
        DevicesRef = FirebaseDatabase.getInstance().getReference().child("Devices");


        myRequestsList = (RecyclerView) RequestsFragmentView.findViewById(R.id.requests_list);
        myRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));


        return RequestsFragmentView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<User> options =
                new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(RequestsRef.child(currentUserID), User.class)
                .build();


        FirebaseRecyclerAdapter<User, RequestsViewHolder> adapter =
                new FirebaseRecyclerAdapter<User, RequestsViewHolder>(options) {

                    @Override
                    protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, int position, @NonNull User model) {

                        final String list_user_id = getRef(position).getKey();

                        DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists() ){

                                    String type = dataSnapshot.getValue().toString();

                                    if (type.equals("received")&& list_user_id!=null){

                                        holder.itemView.findViewById(R.id.btn_request_accept).setVisibility(View.VISIBLE);
                                        holder.itemView.findViewById(R.id.btn_request_decline).setVisibility(View.VISIBLE);

                                        UserRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                if (dataSnapshot.hasChild("image")){
                                                    //final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                                    //final String requestUserEmail = dataSnapshot.child("email").getValue().toString();
                                                    final String requestProfileImage = dataSnapshot.child("image").getValue().toString();

                                                    //holder.userName.setText(requestUserName);
                                                    //holder.userEmail.setText(requestUserEmail);
                                                    Picasso.get().load(requestProfileImage).into(holder.profileImage);

                                                }
                                                //else{
                                                    final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                                    final String requestUserEmail = dataSnapshot.child("email").getValue().toString();

                                                    holder.userName.setText(requestUserName);
                                                    holder.userEmail.setText(requestUserEmail);
                                                //}

                                                holder.AcceptBtn.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {

                                                        CharSequence options[] = new CharSequence[]{

                                                              "Yes",
                                                                "No"

                                                        };

                                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                        builder.setTitle("Do you really want to accept request from " + requestUserName + "?");

                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                if(i == 0){

                                                                                DevicesRef.child(list_user_id).child(currentUserID).child("Device")
                                                                                        .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                                        if (task.isSuccessful()){

                                                                                            DevicesRef.child(currentUserID).child(list_user_id).child("Keeper")
                                                                                                    .setValue("Keep").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                                    if (task.isSuccessful()) {

                                                                                                        RequestsRef.child(currentUserID).child(list_user_id)
                                                                                                                .removeValue()
                                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                    @Override
                                                                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                                                                        if (task.isSuccessful()) {

                                                                                                                            RequestsRef.child(list_user_id).child(currentUserID)
                                                                                                                                    .removeValue()
                                                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                        @Override
                                                                                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                                                                                            if (task.isSuccessful()) {

                                                                                                                                                Toast.makeText(getContext(), "Request accepted. New device added", Toast.LENGTH_SHORT).show();
                                                                                                                                            }

                                                                                                                                        }
                                                                                                                                    });


                                                                                                                        }

                                                                                                                    }
                                                                                                                });
                                                                                                    }
                                                                                                }

                                                                                            });

                                                                                        }

                                                                                    }
                                                                                });

                                                                }if(i == 1){

                                                                    dialogInterface.dismiss();
                                                                }

                                                            }
                                                        });
                                                        builder.show();
                                                    }
                                                });

                                                holder.DeclineBtn.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {

                                                        CharSequence options[] = new CharSequence[]{

                                                                "Yes",
                                                                "No"

                                                        };

                                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                        builder.setTitle("Do you really want to decline request from " + requestUserName + "?");

                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                if(i == 0){

                                                                    RequestsRef.child(currentUserID).child(list_user_id)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    if (task.isSuccessful()){

                                                                                        RequestsRef.child(list_user_id).child(currentUserID)
                                                                                                .removeValue()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                                                        if (task.isSuccessful()){

                                                                                                            Toast.makeText(getContext(),"Request declined", Toast.LENGTH_SHORT).show();
                                                                                                        }

                                                                                                    }
                                                                                                });
                                                                                    }

                                                                                }
                                                                            });

                                                                }if(i == 1){

                                                                    dialogInterface.dismiss();

                                                                }

                                                            }
                                                        });
                                                        builder.show();
                                                    }
                                                });

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_user, viewGroup, false);
                        RequestsViewHolder holder = new RequestsViewHolder(view);
                        return holder;
                    }
                };

        myRequestsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder{

        TextView userName, userEmail;
        CircleImageView profileImage;
        Button AcceptBtn, DeclineBtn;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_name);
            userEmail = itemView.findViewById(R.id.user_email);
            profileImage = itemView.findViewById(R.id.user_ic);
            AcceptBtn = itemView.findViewById(R.id.btn_request_accept);
            DeclineBtn = itemView.findViewById(R.id.btn_request_decline);
        }
    }
}
