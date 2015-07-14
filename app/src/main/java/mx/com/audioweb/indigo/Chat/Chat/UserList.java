package mx.com.audioweb.indigo.Chat.Chat;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import mx.com.audioweb.indigo.R;

/**
 * Created by jcal on 24/03/15.
 */
public class UserList extends CustomActivity {
    public static ParseUser user;
    private ArrayList<ParseUser> uList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_list);
        getActionBar().setDisplayHomeAsUpEnabled(false);
        getActionBar().setHomeButtonEnabled(false);
        updateUserStatus(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateUserStatus(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserList();
    }

    private void updateUserStatus(boolean online) {
        user.put("online", online);
        user.saveEventually();
    }

    private void loadUserList() {
        final ProgressDialog dialog = ProgressDialog.show(this, null, getString(R.string.alert_loading));
        ParseUser.getQuery().whereNotEqualTo("username", user.getUsername()).findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> parseUsers, ParseException e) {
                dialog.dismiss();
                if (parseUsers != null) {
                    if (parseUsers.size() == 0) {
                        Toast.makeText(UserList.this, R.string.msg_no_user_found, Toast.LENGTH_SHORT).show();
                    }

                    uList = new ArrayList<ParseUser>(parseUsers);
                    ListView list = (ListView) findViewById(R.id.List);
                    list.setAdapter(new UserAdapter());
                    list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            startActivity(new Intent(UserList.this, Chat.class).putExtra(getResources().getString(R.string.intent_data), uList.get(position).getUsername()).putExtra(getResources().getString(R.string.parse_name_user), uList.get(position).get("Name").toString()));
                        }
                    });
                } else {
                    Utils.showDialog(UserList.this, getString(R.string.err_users) + " " + e.getMessage());
                    e.printStackTrace();
                }

            }
        });
    }

    private class UserAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return uList.size();
        }

        public ParseUser getItem(int arg0) {
            return uList.get(arg0);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.chat_item, null);
            }
            ParseUser c = getItem(position);
            TextView lbl = (TextView) convertView.findViewById(R.id.chat_item);
            lbl.setText(c.getString("Name"));
            lbl.setCompoundDrawablesRelativeWithIntrinsicBounds(c.getBoolean("online") ? R.drawable.ic_online : R.drawable.ic_offline, 0, R.drawable.arrow, 0);
            return convertView;
        }
    }


}


