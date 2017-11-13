package com.example.kimjungwon.livebusker.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kimjungwon.livebusker.CustomClass.Chat;
import com.example.kimjungwon.livebusker.R;

import java.util.ArrayList;

/**
 * Created by kimjungwon on 2017-11-10.
 */

public class Chat_Adapter extends RecyclerView.Adapter<Chat_Adapter.mViewHolder> {

    private static final String TAG = "Chat_Adapter";
    Context context;
    public ArrayList<Chat> Chats = new ArrayList<>();

    public Chat_Adapter(ArrayList<Chat> chats, Context context){
        this.context = context;
        this.Chats = chats;
    }

    @Override
    public mViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, null);
        Log.d(TAG,"view holder inflate");
        return new mViewHolder(v);
    }

    @Override
    public void onBindViewHolder(Chat_Adapter.mViewHolder holder, int position) {
        Log.d(TAG, "on Bind View Holder");
        Chat chat = (Chat) Chats.get(position);

        holder.IdTV.setText(chat.getUser_id());
        holder.ChatTV.setText(chat.getUser_chat());
    }

    @Override
    public int getItemCount() {
        return Chats.size();
    }

    public class mViewHolder extends RecyclerView.ViewHolder {

        TextView IdTV,ChatTV;

        public mViewHolder(View v) {
            super(v);
            IdTV = (TextView) v.findViewById(R.id.user_id);
            ChatTV = (TextView) v.findViewById(R.id.user_chat);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                IdTV.setBackground(ContextCompat.getDrawable(context,R.drawable.name_background));
                ChatTV.setBackground(ContextCompat.getDrawable(context,R.drawable.round_background));
            }

        }
    }
}
