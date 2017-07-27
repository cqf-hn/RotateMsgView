package cqf.hn.rotatemsgview;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RotateMsgView rotateMsgView = (RotateMsgView) findViewById(R.id.rotateMsgView);
        //rotateMsgView.setFirstVisibleIndex(3);
        rotateMsgView.setMode(RotateMsgView.Mode.MODE_ALPHA);
        rotateMsgView.setAdapter(new CustomAdapter() {
            @Override
            public int getCount() {
                return 5;
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public View getView(int position, ViewGroup parent) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.text, null);
                TextView textView = (TextView) view.findViewById(R.id.textView);
                textView.setText(position+"");
                view.setBackgroundColor(Color.parseColor("#0000FF")+position*100);
                return view;
            }

            @Override
            public int getItemViewType(int position) {
                return 0;
            }

            @Override
            public int getViewTypeCount() {
                return 0;
            }
        });
        rotateMsgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,ListViewActivity.class));
            }
        });
        rotateMsgView.setMode(RotateMsgView.Mode.MODE_TRANSLATION_UP);
        rotateMsgView.start();
    }
}
