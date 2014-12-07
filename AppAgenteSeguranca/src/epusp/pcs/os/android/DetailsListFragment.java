package epusp.pcs.os.android;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import epusp.pcs.os.workflow.emcallworkflowendpoint.model.EmergencyCall;

public class DetailsListFragment extends Fragment {

	private OnUpdateListener listener;
	String text;
	TextView textView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_details_list,
				container, false);
		textView = (TextView) view.findViewById(R.id.detailsText);
		//textView.setText(text);
		//Button button = (Button) view.findViewById(R.id.button1);
//		button.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//
//				listener.onUpdate("A1");
//			}
//		});
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
//		if (activity instanceof OnUpdateListener) {
//			listener = (OnUpdateListener) activity;
//		} else {
//			throw new ClassCastException(activity.toString()
//					+ " must implemenet DetailsListFragment.OnUpdateListener");
//		}
	}

	public interface OnUpdateListener {
		public void onUpdate(String vehicleId);
	}

	// May also be triggered from the Activity
	public void updateDetails() {
		textView.setText(text);
	}

}
