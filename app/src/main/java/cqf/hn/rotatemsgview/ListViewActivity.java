package cqf.hn.rotatemsgview;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @desc ${TODD}
 */

public class ListViewActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return 5;
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.text, null);
                TextView textView = (TextView) view.findViewById(R.id.textView);
                textView.setText(position+"");
                view.setBackgroundColor(Color.parseColor("#0000FF")+position*100);
                return view;
            }
        });
    }
}
