package com.example.listviewver;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import static androidx.core.content.ContextCompat.startActivity;

public class ContactsAdapter extends ArrayAdapter<Contact> {

    private ArrayList<Contact> items;

    public ContactsAdapter(Context context, int textViewResourceId, ArrayList<Contact> items) {
        super(context, textViewResourceId, items);
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;


        if ( v == null ) {

            // vi(layoutInflater)는 Layout Inflater를 사용해 만든다.
            LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.phonebook, null);
        }
        // 현재의 position을 가지고 item을 가져온다. item은 이름과 전화번호가 들어있다.
        Contact p = items.get(position);

        if ( p != null )
        {
            // 2개의 텍스트뷰를 셋팅해준다.
            TextView tt = (TextView)v.findViewById(R.id.toptext);
            TextView bt = (TextView)v.findViewById(R.id.bottomtext);

            // 셋팅한 텍스트뷰의 텍스트에 이름과 전화번호를 넣어준다.
            tt.setText(p.getName());
            bt.setText("  " + p.getNumber());
        }

        // imagebutton 셋팅
        ImageView ib_call = (ImageView) v.findViewById(R.id.button_call);

        // 현재의 태그 입력, 이미지 버튼 클릭시 사용하기 위해 저장

        ib_call.setTag(position);

        return v;
    }
}
