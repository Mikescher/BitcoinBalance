package samdev.de.bitcoinbalance;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class AddressListAdapter extends BaseAdapter implements ListAdapter {
    private ArrayList<String> list = new ArrayList<String>();
    private Context context;

    public AddressListAdapter(ArrayList<String> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int pos) {
        return list.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.address_list_layout, null);
        }

        TextView listItemText = (TextView)view.findViewById(R.id.list_item_string);
        String address = list.get(position);
        listItemText.setText(address.substring(0, 8) + " ... " + address.substring(address.length() - 8));

        ImageButton deleteBtn = (ImageButton)view.findViewById(R.id.delete_btn);
        ImageButton topBtn = (ImageButton)view.findViewById(R.id.top_btn);

        deleteBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                list.remove(position);

                notifyDataSetChanged();
            }
        });
        topBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String element = list.get(position);
                list.remove(position);
                list.add(0, element);

                notifyDataSetChanged();
            }
        });

        return view;
    }
}