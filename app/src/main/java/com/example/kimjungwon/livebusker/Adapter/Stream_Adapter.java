package com.example.kimjungwon.livebusker.Adapter;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.v4.widget.ImageViewCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.kimjungwon.livebusker.Activity.WatchingActivity;
import com.example.kimjungwon.livebusker.Data.Stream;
import com.example.kimjungwon.livebusker.R;

import java.util.ArrayList;

import static com.example.kimjungwon.livebusker.Config.URL.Thumbnail_Addr;
import static com.example.kimjungwon.livebusker.Config.URL.YouTube_Thumnail_URL;

/**
 * Created by kimjungwon on 2017-09-07.
 */

public class Stream_Adapter extends RecyclerView.Adapter<Stream_Adapter.mViewHolder> {

    private static final String TAG = Stream_Adapter.class.getSimpleName();
    private ArrayList<Stream> streams = new ArrayList<>();
    Context context;

    public Stream_Adapter(ArrayList<Stream> streams, Context context){
        this.streams = streams;
        this.context = context;
    }

    @Override
    public mViewHolder onCreateViewHolder(final ViewGroup parent, final int position) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stream, null);
        Log.d(TAG,"viewholder inflate!");
        return new mViewHolder(v);
    }

    @Override
    public void onBindViewHolder(mViewHolder holder, final int position) {
        Log.d(TAG,"onbindviewHolder");
        Stream stream = (Stream) streams.get(position);

        if(stream.getId() == 0){
            Log.d(TAG,"stream id: " + 0);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, WatchingActivity.class);
                    intent.putExtra("Streamkey",streams.get(position).getStream_key());
                    intent.putExtra("Roomname",streams.get(position).getTitle());
//                Toast.makeText(context, "item position: " + position, Toast.LENGTH_SHORT).show();
                    Log.d(TAG,"title" + streams.get(position).getStream_key());
                    context.startActivity(intent);
                }
            });
            Glide.with(context).load(Thumbnail_Addr + stream.getStream_key() + ".jpg").into(holder.ThumnailIV);
            holder.TitleTV.setText(stream.getTitle());
        }else if(stream.getId() == 1){
            Log.d(TAG,"stream id: " + 1);
            Glide.with(context).load(YouTube_Thumnail_URL + stream.getStream_key() + "/hqdefault.jpg").into(holder.ThumnailIV);
            holder.TitleTV.setText(stream.getTitle());
        }

    }

    @Override
    public int getItemCount() {
        return streams.size();
    }


    public class mViewHolder extends RecyclerView.ViewHolder {
        ImageView ThumnailIV,ProfileIV;
        TextView TitleTV;

        public mViewHolder(View v) {
            super(v);
            ThumnailIV = (ImageView) v.findViewById(R.id.stream_thumnail);
            ProfileIV = (ImageView) v.findViewById(R.id.stream_profile);
            TitleTV = (TextView) v.findViewById(R.id.stream_title);
        }
    }
}
