package epusp.pcs.os.android;

import java.util.ArrayList;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

public class DetailsListFragment extends Fragment {
	ImageView imageView;
	ListView listView;

	DetailsListAdapter adapter;
	ArrayList<Item> list;
	String[] labels = new String[]{};
	String[] values = new String[]{};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_details_list,
				container, false);
		listView = (ListView) view.findViewById(R.id.detailsList);
		imageView = (ImageView) view.findViewById(R.id.image);
		
        list = new ArrayList<Item>();
		for (int i = 0; i < labels.length; ++i) {
			list.add(new Item(labels[i], values[i]));
		}
		adapter = new DetailsListAdapter(getActivity().getApplicationContext(), list);
		listView.setAdapter(adapter);
		return view;
	}
	
	//Atualiza dados na lista
	public void updateDetails(ArrayList<Item> list) {
		adapter.updateList(list); 
	}
	
	//Atualiza foto da vítima
	public void updatePicture(Bitmap image) {
		imageView.setImageBitmap(image);
	}

}
