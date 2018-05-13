package cqf.hn.rotatemsgview;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import hn.cqf.com.lib.CustomAdapter;
import hn.cqf.com.lib.RotateMsgView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private RotateMsgView rotateMsgView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rotateMsgView = (RotateMsgView) findViewById(R.id.rotateMsgView);
        Button btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(this);
        findViewById(R.id.button1).setOnClickListener(this);
        //rotateMsgView.setFirstViewIndex(3);//测试
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
            public View getView(final int position, ViewGroup parent) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.text, null);
                TextView textView = (TextView) view.findViewById(R.id.textView);
                textView.setText(position + "");
                view.setBackgroundColor(Color.parseColor("#0000FF") + position * 100);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this,position + "", Toast.LENGTH_SHORT).show();
                    }
                });
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
        rotateMsgView.setMode(RotateMsgView.Mode.MODE_TRANSLATION_UP);
        rotateMsgView.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                rotateMsgView.setMode(rotateMsgView.getMode() == RotateMsgView.Mode.MODE_TRANSLATION_UP ?
                        RotateMsgView.Mode.MODE_TRANSLATION_DOWN : RotateMsgView.Mode.MODE_TRANSLATION_UP);
                break;
            case R.id.button1:
                startActivity(new Intent(this,NextActivity.class));
                break;
        }
    }
}
