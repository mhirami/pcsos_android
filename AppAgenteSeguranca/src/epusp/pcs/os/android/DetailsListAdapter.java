package epusp.pcs.os.android;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DetailsListAdapter extends ArrayAdapter<Item> {

	private final Context context;
	private LayoutInflater inflater = null; 
	private ArrayList<Item> list;

	public DetailsListAdapter(Context context, ArrayList<Item> itemsArrayList) {
		super(context, R.layout.item_details_list, itemsArrayList);

		this.context = context;
		this.list = itemsArrayList;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override  
    public int getCount() {  
         return list.size();  
    }  
    @Override  
    public Item getItem(int pos) {  
         return list.get(pos);  
    }  
    @Override  
    public long getItemId(int position) {  
         return position;  
    }  
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;  
		DetailsListViewHolder viewHolder;  
        if (convertView == null) {  
             LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
             v = li.inflate(R.layout.item_details_list, null);  
             viewHolder = new DetailsListViewHolder(v);  
             v.setTag(viewHolder);  
        } else {  
             viewHolder = (DetailsListViewHolder) v.getTag();  
        }  
        viewHolder.label.setText(list.get(position).getLabel()); 
        viewHolder.value.setText(list.get(position).getValue());
        
        return v; 
	}
	
	public void updateList(ArrayList<Item> list) {
        this.list = list;
        notifyDataSetChanged();
     }
 
}

class DetailsListViewHolder {  
    public TextView label;
    public TextView value;
    
    public DetailsListViewHolder(View base) {  
         label = (TextView) base.findViewById(R.id.label);
         value = (TextView) base.findViewById(R.id.value);
    }  
}  