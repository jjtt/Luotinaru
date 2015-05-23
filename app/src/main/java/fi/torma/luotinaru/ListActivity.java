package fi.torma.luotinaru;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class ListActivity extends android.app.ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        List<Map<String, String>> list = new LinkedList<>();

        Map<String, String> m = new HashMap<>();

        m.put("title", "Latest");
        list.add(m);
        
        ArrayAdapter<Map<String, String>> adapter = new ArrayAdapter<Map<String, String>>(this, android.R.layout.simple_list_item_2, android.R.id.text1, list) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                text1.setText(getItem(position).get("title"));
                text2.setText(getItem(position).get("description"));
                return view;
            }
        };

        setListAdapter(adapter);

        new FilesTask(this, adapter).execute();
    }

}
