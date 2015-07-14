package mx.com.audioweb.indigo.Chat;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.List;

import mx.com.audioweb.indigo.R;


/**
 * Created by pramos on 2/18/15.
 */
public class ChatListAdapter extends ArrayAdapter<Message> {
    private String mUserId;

    public ChatListAdapter(Context context, String userId, List<Message> messages) {
        super(context, 0, messages);
        this.mUserId = userId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).
                    inflate(R.layout.chat_item, parent, false);
            final ViewHolder holder = new ViewHolder();
            holder.imageLeft = (ImageView)convertView.findViewById(R.id.ivProfileLeft);
            holder.imageRight = (ImageView)convertView.findViewById(R.id.ivProfileRight);
            holder.body = (TextView)convertView.findViewById(R.id.tvBody);
            convertView.setTag(holder);
        }
        final Message message = (Message)getItem(position);
        final ViewHolder holder = (ViewHolder)convertView.getTag();
        final boolean isMe = message.getUserId().equals(mUserId);
        // Show-hide image based on the logged-in user.
        // Display the profile image to the right for our user, left for other users.
        if (isMe) {
            holder.imageRight.setVisibility(View.VISIBLE);
            holder.imageLeft.setVisibility(View.GONE);
            holder.body.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        } else {
            holder.imageLeft.setVisibility(View.VISIBLE);

            holder.imageRight.setVisibility(View.GONE);
            holder.body.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        }

        /*Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));*/
        ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
        int color2 = generator.getColor(message.getUserId());


        final ImageView profileView = isMe ? holder.imageRight : holder.imageLeft;
        TextDrawable drawable = TextDrawable.builder()
                .buildRound(message.getInitial(),color2);
        /*Picasso.with(getContext())
                .load(getProfileUrl(message.getUserId()))
                .into(profileView);*/
        if (isMe) {
            profileView.setImageDrawable(drawable);
            holder.body.setText(message.getBody());
            holder.body.setBackgroundResource(R.drawable.ic_chat_2);
            holder.body.setTextColor(Color.WHITE);
            holder.body.setGravity(Gravity.RIGHT);
        }
        else{
            profileView.setImageDrawable(drawable);
            holder.body.setText(message.getBody());
            holder.body.setTextColor(Color.WHITE);
            holder.body.setBackgroundResource(R.drawable.ic_chat_1);
            holder.body.setGravity(Gravity.LEFT);

        }

        return convertView;
    }


    // Create a gravatar image based on the hash value obtained from userId
    private static String getProfileUrl(final String userId) {
        String hex = "";
        try {
            final MessageDigest digest = MessageDigest.getInstance("MD5");
            final byte[] hash = digest.digest(userId.getBytes());
            final BigInteger bigInt = new BigInteger(hash);
            hex = bigInt.abs().toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //return "http://www.gravatar.com/avatar/" + hex + "?d=identicon";
        return userId;
    }

    public static String stripNonDigits(
            final CharSequence input /* inspired by seh's comment */){
        final StringBuilder sb = new StringBuilder(
                input.length() /* also inspired by seh's comment */);
        for(int i = 0; i < input.length(); i++){
            final char c = input.charAt(i);
            if(c > 47 && c < 58){
                sb.append(c);
            }
        }
        return sb.toString();
    }

    final class ViewHolder {
        public ImageView imageLeft;
        public ImageView imageRight;
        public TextView body;
    }

}