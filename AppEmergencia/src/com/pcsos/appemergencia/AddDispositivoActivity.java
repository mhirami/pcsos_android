package com.pcsos.appemergencia;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;
import com.pcsos.backend.shared.dispositivoendpoint.Dispositivoendpoint;
import com.pcsos.backend.shared.dispositivoendpoint.model.Dispositivo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddDispositivoActivity extends Activity {
	EditText editId;
	EditText editLatitude;
	EditText editLongitude;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_adddispositivo);

		editId = (EditText)findViewById(R.id.editId);
		editLatitude = (EditText)findViewById(R.id.editLatitude);
		editLongitude = (EditText)findViewById(R.id.editLongitude);

		//Event Listener for About App button
		Button btnAddQuote = (Button)findViewById(R.id.btnAddDispositivo);
		btnAddQuote.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				//Check if values are provided
				Double txtId = Double.valueOf(editId.getText().toString());
				Double txtAuthorName = Double.valueOf(editLatitude.getText().toString());
				Double txtMessage = Double.valueOf(editLongitude.getText().toString());

				if ((txtAuthorName == 0) || (txtMessage == 0)) {
					Toast.makeText(AddDispositivoActivity.this, "Coloque valores diferentes de zero", Toast.LENGTH_SHORT).show();
					return;
				}

				//Go ahead and perform the transaction
				Double[] params = {txtId,txtAuthorName,txtMessage};
				new AddDispositivoAsyncTask(AddDispositivoActivity.this).execute(params);

			}
		});

	}

	private class AddDispositivoAsyncTask extends AsyncTask<Double, Void, Dispositivo>{
		Context context;
		private ProgressDialog pd;

		public AddDispositivoAsyncTask(Context context) {
			this.context = context;
		}

		protected void onPreExecute(){ 
			super.onPreExecute();
			pd = new ProgressDialog(context);
			pd.setMessage("Adding dispositivo...");
			pd.show();    
		}

		protected Dispositivo doInBackground(Double... params) {
			Dispositivo response = null;
			try {
				Dispositivoendpoint.Builder builder = new Dispositivoendpoint.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
				Dispositivoendpoint service =  builder.build();
				Dispositivo dispositivo = new Dispositivo();
				dispositivo.setId(params[0].longValue());
				dispositivo.setLatitude(params[1]);
				dispositivo.setLongitude(params[2]);
				response = service.insertDispositivo(dispositivo).execute();
			} catch (Exception e) {
				Log.d("Could not Add Dispositivo", e.getMessage(), e);
			}
			return response;
		}

		protected void onPostExecute(Dispositivo dispositivo) {
			//Clear the progress dialog and the fields
			pd.dismiss();
			editId.setText("");
			editLongitude.setText("");
			editLatitude.setText("");

			//Display success message to user
			Toast.makeText(getBaseContext(), "Dispositivo added succesfully", Toast.LENGTH_SHORT).show();
		}

	}
}
