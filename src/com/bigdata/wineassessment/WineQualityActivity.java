/*
 * WineQuality.java
 * Version:
 * 1
 * 
 * Revisions:
 * 0
 * 
 */
package com.bigdata.wineassessment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;

import weka.classifiers.trees.J48;
import weka.core.Instance;
import android.support.v4.app.Fragment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 * Main Activity for the Wine Assessment Android Application
 * 
 * @author Pranav Dadlani
 * @author Ayush Vora
 * @author Harshal Khandare
 * 
 */
public class WineQualityActivity extends Activity implements
		OnSeekBarChangeListener {

	private SeekBar alcoholBar, volatileBar, sulphatesBar, citricBar,
			densityBar, sulphurBar;
	private float tempAlcohol = 0;
	private float tempVolatile = 0;
	private float tempSulphates = 0;
	private float tempCitric = 0;
	private float tempDensity = 0;
	private float tempSulphur = 0;

	private TextView alcoholTextView, volatileTextView, sulphatesTextView,
			citricTextView, densityTextView, sulphurTextView;
	private Button btnQuality;
	private String result = null;
	static weka.core.Instances inputs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_wine_quality);
		alcoholBar = (SeekBar) findViewById(R.id.alcoholSeekbar);
		alcoholBar.setOnSeekBarChangeListener(this);

		volatileBar = (SeekBar) findViewById(R.id.volatileSeekbar);
		volatileBar.setOnSeekBarChangeListener(this);

		sulphatesBar = (SeekBar) findViewById(R.id.sulphatesSeekbar);
		sulphatesBar.setOnSeekBarChangeListener(this);

		citricBar = (SeekBar) findViewById(R.id.citricSeekbar);
		citricBar.setOnSeekBarChangeListener(this);

		densityBar = (SeekBar) findViewById(R.id.densitySeekbar);
		densityBar.setOnSeekBarChangeListener(this);

		sulphurBar = (SeekBar) findViewById(R.id.totalSo2Seekbar);
		sulphurBar.setOnSeekBarChangeListener(this);

		alcoholTextView = (TextView) findViewById(R.id.alcoholValue);
		volatileTextView = (TextView) findViewById(R.id.volatileValue);

		sulphatesTextView = (TextView) findViewById(R.id.sulphatesValue);
		citricTextView = (TextView) findViewById(R.id.citricValue);

		densityTextView = (TextView) findViewById(R.id.densityValue);
		sulphurTextView = (TextView) findViewById(R.id.totalSo2Value);

		btnQuality = (Button) findViewById(R.id.btnQuality);

		File sdCard = Environment.getExternalStorageDirectory();
		File file = new File(sdCard, "data.arff");
		File attributeFile = new File(sdCard, "att.arff");

		J48 cls = new J48();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			Log.d("E1", "File Error!");
			e1.printStackTrace();
		}
		weka.core.Instances data = null;
		try {
			data = new weka.core.Instances(reader);
		} catch (IOException e1) {
			Log.d("E2", "Data Error!");

			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			inputs = new weka.core.Instances(new BufferedReader(new FileReader(
					attributeFile)));
		} catch (IOException e1) {
			Log.d("E3", "Input Error!");

			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		data.setClassIndex(data.numAttributes() - 1);
		try {
			cls.buildClassifier(data);
		} catch (Exception e1) {

			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ObjectOutputStream oos;
		File modelFile = new File(sdCard, "j48.model");

		try {
			oos = new ObjectOutputStream(new FileOutputStream(modelFile));
			oos.writeObject(cls);
			oos.flush();
			oos.close();

		} catch (IOException e) {
			Log.d("E4444", "Model Error");

			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			btnQuality.setOnClickListener(new OnClickListener() {

				String path = Environment.getExternalStorageDirectory()
						.getPath() + "/j48.model";

				J48 tree = (J48) weka.core.SerializationHelper.read(path);

				@Override
				public void onClick(View v) {

					double alcohol = Double.valueOf(alcoholTextView.getText()
							.toString());
					double volatileAcidity = Double.valueOf(volatileTextView
							.getText().toString());
					double sulphates = Double.valueOf(sulphatesTextView
							.getText().toString());
					double citricAcid = Double.valueOf(citricTextView.getText()
							.toString());
					double density = Double.valueOf(densityTextView.getText()
							.toString());
					double totalSulphur = Double.valueOf(sulphurTextView
							.getText().toString());
					double val = 0;
					try {
						val = tree.classifyInstance(classify(alcohol,
								volatileAcidity, sulphates, citricAcid,
								density, totalSulphur));
					} catch (Exception e) {
						Log.d("E5", "Classify Error");

						// TODO Auto-generated catch block
						e.printStackTrace();
					} // ////////////Text Box Values

					System.out.println(val);// ////////////Give Quality in Final

					AlertDialog.Builder alertDialog = new AlertDialog.Builder(
							WineQualityActivity.this);
					alertDialog.setTitle("Quality");
					if (val == 0)
						result = "MEDIUM!";

					else if (val == 1)
						result = "GOOD!";
					else
						result = "BAD";
					alertDialog.setMessage(result);

					result = null;
					alertDialog.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									dialog.dismiss();

								}
							});
					alertDialog.show();

				}
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static Instance classify(double alcohol, double volatileAcidity,
			double sulphates, double citricAcid, double density,
			double totalSulphur) throws Exception {

		inputs.setClassIndex(inputs.numAttributes() - 1);
		Instance input = inputs.instance(0);
		input.setValue(0, volatileAcidity);
		input.setValue(1, citricAcid);
		input.setValue(2, totalSulphur);
		input.setValue(3, density);
		input.setValue(4, sulphates);
		input.setValue(5, alcohol);

		return input;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.wine_quality, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_wine_quality,
					container, false);
			return rootView;
		}
	}

	@Override
	public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
		String temp = null;
		switch (arg0.getId()) {
		case R.id.alcoholSeekbar:
			tempAlcohol = ((float) progress / 100 * 9 + 7);
			temp = String.format("%.1f", tempAlcohol);
			// Log.d("progress value","" +tempAlcohol +progress);
			alcoholTextView.setText(" " + temp);
			break;
		case R.id.volatileSeekbar:
			tempVolatile = ((float) progress / 100 * 2 + 0);
			temp = String.format("%.2f", tempVolatile);
			volatileTextView.setText(" " + temp);
			break;
		case R.id.sulphatesSeekbar:
			tempSulphates = ((float) progress / 100 * 3 + 0);
			temp = String.format("%.2f", tempSulphates);
			sulphatesTextView.setText(" " + temp);
			break;

		case R.id.citricSeekbar:
			tempCitric = ((float) progress / 100 * 2 + 0);
			temp = String.format("%.2f", tempCitric);
			citricTextView.setText(" " + temp);
			break;
		case R.id.densitySeekbar:
			tempDensity = ((float) progress / 100 * 2 + 0);
			temp = String.format("%.2f", tempDensity);
			densityTextView.setText(" " + temp);
			break;

		case R.id.totalSo2Seekbar:
			tempSulphur = ((float) progress / 100 * 300 + 0);

			sulphurTextView.setText(" " + (int) tempSulphur);

			break;

		}

	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {

	}

}
