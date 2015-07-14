package mx.com.audioweb.indigo.Chat.Chat;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.format.DateUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mx.com.audioweb.indigo.R;

/**
 * Created by jcal on 24/03/15.
 */
public class Chat extends CustomActivity {

    public static Handler handler;
    private ArrayList<Conversation> convList;
    private ChatAdapter adp;
    private EditText txt;
    private String buddy, buddyName;
    private Date lastMsgDate;
    private boolean isRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);
        buddy = getIntent().getStringExtra(getResources().getString(R.string.intent_data));
        buddyName = getIntent().getStringExtra(getResources().getString(R.string.parse_name_user));
        ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
        int color2 = generator.getColor(buddy);
        TextDrawable initial = TextDrawable.builder().buildRound("IN", color2);
        //getActionBar().setLogo(initial);


        convList = new ArrayList<Conversation>();
        ListView list = (ListView) findViewById(R.id.list);
        adp = new ChatAdapter();
        list.setAdapter(adp);
        list.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        list.setStackFromBottom(true);

        txt = (EditText) findViewById(R.id.txt);
        txt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        setTouchNClick(R.id.btnSend);

        getActionBar().setTitle(buddyName);

        handler = new Handler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isRunning = true;
        loadConversationList();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isRunning = false;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.btnSend) {
            sendMessage();
        }
    }

    private void sendMessage() {
        if (txt.length() == 0) {
            return;
        }

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txt.getWindowToken(), 0);

        String s = txt.getText().toString();
        final Conversation c = new Conversation(s, new Date(), UserList.user.getUsername());
        c.setStatus(Conversation.STATUS_SENDING);
        convList.add(c);
        adp.notifyDataSetChanged();
        txt.setText(null);

        ParseObject parseObject = new ParseObject("Chat");
        parseObject.put("sender", UserList.user.getUsername());
        parseObject.put("receiver", buddy);
        parseObject.put("message", s);
        parseObject.saveEventually(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    c.setStatus(Conversation.STATUS_SENT);
                } else {
                    c.setStatus(Conversation.STATUS_FAILED);
                }
                adp.notifyDataSetChanged();
            }
        });

    }

    private void loadConversationList() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Chat");
        if (convList.size() == 0) {
            ArrayList<String> arrayList = new ArrayList<String>();
            arrayList.add(buddy);
            arrayList.add(UserList.user.getUsername());
            query.whereContainedIn("sender", arrayList);
            query.whereContainedIn("receiver", arrayList);
        } else {
            if (lastMsgDate != null) {
                query.whereGreaterThan("createdAt", lastMsgDate);
            }
            query.whereEqualTo("sender", buddy);
            query.whereEqualTo("receiver", UserList.user.getUsername());
        }
        query.orderByDescending("createdAt");
        query.setLimit(30);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (parseObjects != null && parseObjects.size() > 0) {
                    for (int i = parseObjects.size() - 1; i >= 0; i--) {
                        ParseObject parseObject = parseObjects.get(i);
                        Conversation c = new Conversation(parseObject.getString("message"), parseObject.getCreatedAt(), parseObject.getString("sender"));
                        convList.add(c);
                        if (lastMsgDate == null || lastMsgDate.before(c.getDate())) {
                            lastMsgDate = c.getDate();
                        }
                        adp.notifyDataSetChanged();
                    }
                }

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isRunning) {
                            loadConversationList();
                        }
                    }
                }, 1000);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private class ChatAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return convList.size();
        }

        public Conversation getItem(int arg0) {
            return convList.get(arg0);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Conversation c = getItem(position);
            if (c.isSent()) {
                convertView = getLayoutInflater().inflate(R.layout.chat_item_send, null);
            } else {
                convertView = getLayoutInflater().inflate(R.layout.chat_item_rcv, null);
            }
            TextView lbl = (TextView) convertView.findViewById(R.id.lbl1);
            lbl.setText(DateUtils.getRelativeDateTimeString(Chat.this, c.getDate().getTime(), DateUtils.SECOND_IN_MILLIS, DateUtils.DAY_IN_MILLIS, 0));

            lbl = (TextView) convertView.findViewById(R.id.lbl2);
            lbl.setText(c.getMsg());

            lbl = (TextView) convertView.findViewById(R.id.lbl3);
            if (c.isSent()) {
                if (c.getStatus() == Conversation.STATUS_SENT) {
                    lbl.setText("✓✓");
                } else if (c.getStatus() == Conversation.STATUS_SENDING) {
                    lbl.setText("--");
                } else {
                    lbl.setText("xx");
                }
            } else {
                lbl.setText("");
            }
            return convertView;
        }
    }
}
