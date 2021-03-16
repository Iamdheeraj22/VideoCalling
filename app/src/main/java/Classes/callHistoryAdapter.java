package Classes;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.videocalling.MainActivity;
import com.example.videocalling.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class callHistoryAdapter extends RecyclerView.Adapter<callHistoryAdapter.MyViewHolder>
{
    private final Context context;
    private Activity activity;
    private ArrayList callById,userName,fullName,id,date;
    DatabaseReference databaseReference;

    public callHistoryAdapter(Context context, Activity activity, ArrayList callById, ArrayList userName, ArrayList fullName,ArrayList id, ArrayList date)
    {
        this.context = context;
        this.activity = activity;
        this.callById=callById;
        this.userName=userName;
        this.fullName=fullName;
        this.id=id;
        this.date=date;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.call_histroy, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.callBy.setText(String.valueOf(callById.get(position)));
        holder.UserName.setText(String.valueOf(userName.get(position)));
        holder.FullName.setText(String.valueOf(fullName.get(position)));
        holder.Date.setText(String.valueOf(date.get(position)));
        String callerID= String.valueOf(id.get(position));
        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference.child(callerID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String imageUrl=snapshot.child("imageurl").getValue().toString();
                    if(imageUrl.equals("default")){
                        holder.circleImageView.setImageResource(R.drawable.person);
                    }
                    if(!imageUrl.equals("default")){
                        Glide.with(context).load(imageUrl).into(holder.circleImageView);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return callById.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView callBy,UserName,FullName,Date;
        CircleImageView circleImageView;
        MyViewHolder(@NonNull View itemView)
        {
            super(itemView);
            callBy=itemView.findViewById(R.id.call_history_item_callBy);
            UserName=itemView.findViewById(R.id.call_history_item_username);
            FullName=itemView.findViewById(R.id.call_history_item_name);
            Date=itemView.findViewById(R.id.call_history_item_date);
            circleImageView=itemView.findViewById(R.id.call_history_image);
        }
   }
}
