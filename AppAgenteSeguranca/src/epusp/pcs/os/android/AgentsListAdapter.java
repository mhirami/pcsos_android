package epusp.pcs.os.android;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AgentsListAdapter extends ArrayAdapter<String> {

	private final Activity context;
	private String[] list = new String[] {};
	private Integer[] imageId = new Integer[] {};

	public AgentsListAdapter(Activity context,
			String[] list, Integer[] imageId) {
		super(context, R.layout.item_agents_list, list);
		this.context = context;
		this.list = list;
		this.imageId = imageId;
	}
	
	@Override  
    public int getCount() {  
         return list.length;  
    } 
	
	public int getIndex(String name) {
		ArrayList<String> array = new ArrayList<String>();
		for(String item : list)
			array.add(item);
		return array.indexOf(name);
	}
	
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		LayoutInflater inflater = context.getLayoutInflater();
		View rowView= inflater.inflate(R.layout.item_agents_list, null, true);
		TextView txtTitle = (TextView) rowView.findViewById(R.id.name);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.image);
		txtTitle.setText(list[position]);
		if(imageId[position] != null)
			imageView.setImageResource(imageId[position]);
		else
			imageView.setImageResource(R.drawable.screenshot);
		return rowView;
	}
	
	public void updateList(String[] list, Integer[] imageId) {
		this.list = list;
		this.imageId = imageId;
		notifyDataSetChanged();
	}

}