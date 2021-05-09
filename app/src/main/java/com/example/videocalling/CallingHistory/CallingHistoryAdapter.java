package com.example.videocalling.CallingHistory;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.videocalling.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class CallingHistoryAdapter extends RecyclerView.Adapter<CallingHistoryAdapter.CallingHistoryHolder>
{

     Context mCtx;
     List<CallHistory> callHistoryList;
     DatabaseReference databaseReference;

    public CallingHistoryAdapter(Context mCtx, List<CallHistory> callHistoryList) {
        this.mCtx = mCtx;
        this.callHistoryList = callHistoryList;
    }

    @NonNull
    @Override
    public CallingHistoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.call_histroy, parent, false);
        return new CallingHistoryHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CallingHistoryHolder holder, int position)
    {
        //Sender Information
        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users");
        CallHistory callHistory=callHistoryList.get(position);
        holder.textViewCallDate.setText(callHistory.getDate());
        databaseReference.child(callHistory.getSenderId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String firstName=snapshot.child("firstname").getValue().toString();
                    String lastname=snapshot.child("lastname").getValue().toString();
                    holder.textViewSenderName.setText(firstName+" "+lastname);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(mCtx,error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

        //Receiver Information
        databaseReference.child(callHistory.getReceiverid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String receiverImageUrl=snapshot.child("imageurl").getValue().toString();
                    String firstName=snapshot.child("firstname").getValue().toString();
                    String lastname=snapshot.child("lastname").getValue().toString();
                    if(receiverImageUrl.equals("default")){
                        Glide.with(mCtx).load(receiverImageUrl).into(holder.circleImageView);
                    }else {
                        holder.circleImageView.setImageResource(R.drawable.person);
                    }
                    holder.textViewReceiverName.setText(firstName+" "+lastname);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mCtx, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return callHistoryList.size();
    }

    static class CallingHistoryHolder extends RecyclerView.ViewHolder
    {
        TextView textViewSenderName, textViewReceiverName,textViewCallDate;
        CircleImageView circleImageView;

        public CallingHistoryHolder(View itemView) {
            super(itemView);
            textViewSenderName=itemView.findViewById(R.id.callSenderName);
            textViewReceiverName=itemView.findViewById(R.id.callReceiverName);
            textViewCallDate=itemView.findViewById(R.id.call_history_item_date);
            circleImageView=itemView.findViewById(R.id.call_history_image);
        }
    }
}
