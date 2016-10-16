package com.example.tpz.echo;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by jamiepatel on 22/09/2015.
 */
public class MessageAdapter extends BaseAdapter {

    Context messageContext;
    List<Message> messageList;
    static boolean user;

    public MessageAdapter(Context context, List<Message> messages){
        messageList = messages;
        messageContext = context;
    }

    public static void IsUser(boolean isUser){
        user = isUser;
    }

    @Override
    public int getCount() {
        return messageList.size();
    }

    @Override
    public Object getItem(int position) {
        return messageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MessageViewHolder holder;
        if (convertView == null){
            LayoutInflater messageInflater = (LayoutInflater) messageContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = messageInflater.inflate(R.layout.message, null);
            holder = new MessageViewHolder();
            if(user){
                holder.bodyView = (TextView) convertView.findViewById(R.id.message_user);
            }else{
                holder.bodyView = (TextView) convertView.findViewById(R.id.message_reply);
            }
//            holder.thumbnailImageView = (ImageView) convertView.findViewById(R.id.img_thumbnail);
//            holder.senderView = (TextView) convertView.findViewById(R.id.message_sender);

            convertView.setTag(holder);
        } else {
            holder = (MessageViewHolder) convertView.getTag();
        }
        Message message = (Message) getItem(position);

        holder.bodyView.setText(message.text);
//        holder.senderView.setText(message.name);

//        Picasso.with(messageContext).
//                load("https://twitter.com/"+message.name+"/profile_image?size=original").
//                placeholder(R.mipmap.ic_launcher).
//                into(holder.thumbnailImageView);
        return convertView;
    }

    public void add(Message message){
        messageList.add(message);
        notifyDataSetChanged();
    }

    private static class MessageViewHolder {
        public ImageView thumbnailImageView;
        public TextView senderView;
        public TextView bodyView;
    }
}
