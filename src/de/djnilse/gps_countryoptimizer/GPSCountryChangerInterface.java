package de.djnilse.gps_countryoptimizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

public class GPSCountryChangerInterface extends Activity {

	private String countrycodes[] = { "ao", "ar", "am", "au", "at", "bs", "bd",
			"by", "be", "ba", "br", "bg", "kh", "ca", "cl", "cn", "cr", "hr",
			"cz", "dk", "sv", "ee", "fi", "fr", "de", "gr", "gt", "hk", "hu",
			"in", "id", "ir", "iq", "ie", "il", "it", "jp", "kz", "kr", "kg",
			"lv", "lt", "lu", "mk", "mg", "my", "mx", "md", "nl", "nc", "nz",
			"no", "om", "pk", "pa", "ph", "pl", "pt", "qa", "rs", "ro", "ru",
			"sa", "sg", "sk", "si", "za", "es", "lk", "se", "ch", "tw", "tz",
			"th", "tr", "ug", "ua", "ae", "uk", "us", "uz", "ve", "vn", "yu" };

	private File directory;
	private File file;
	private FileInputStream fileIn;
	private String verifyCode;
	private boolean clearLog = false;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.about, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_about:
			startActivity(new Intent(this, About.class));
			break;
		}
		return true;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		addLog("Asking for root permissions ...\n");

		// remount system to access /system
		try {
			remountSystem(true);
		} catch (IOException e3) {
			e3.printStackTrace();
		} catch (InterruptedException e3) {
			e3.printStackTrace();
		}

		directory = getDir("code", MODE_PRIVATE);
		file = new File(directory, "countrycode");

		/*
		 * If no countrycode file was found, sets automatically to the country
		 * you set your system to
		 */
		if (file.exists() == false) {
			addLog("First use ? No worries, I set the gps.conf to the country you live in\n");
			String location = getResources().getConfiguration().locale
					.getCountry().toLowerCase();
			addLog("So you live in "
					+ getResources().getConfiguration().locale
							.getDisplayCountry() + ". You're welcome ;)\n");
			addLog("Setting location to your country ...\n");

			try {
				configureGPS(location); // thats the important part i forgot :P

				FileOutputStream fileOut;
				fileOut = new FileOutputStream(file);
				saveFile(fileOut, location);
				addLog("Done!\n");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			fileIn = new FileInputStream(file);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		final Spinner countrySpinner = (Spinner) findViewById(R.id.spCountries);

		Resources res = getResources();
		String countries[] = res.getStringArray(R.array.countries);

		try {
			addLog("Loading last selected country ...\n");
			verifyCode = loadCountryCode(fileIn);
			for (int i = 0; i <= 83; i++) {
				if (countrycodes[i].equals(verifyCode)) {
					countrySpinner.setSelection(i, true);
					addLog("Success! Last country selected was " + countries[i]
							+ "\n\n");
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

        /** Called when the activity is destroyed. */
        @Override
        public void onDestroy() {
                addLog("Resetting filesystem mounts...\n");

                // clean up system mount
                try {
                        remountSystem(false);
                } catch (IOException e3) {
                        e3.printStackTrace();
                } catch (InterruptedException e3) {
                        e3.printStackTrace();
                }
               super.onDestroy();
        }

	// changes the gps.conf and saves countrycode to /data
	public void Start(View view) throws InterruptedException, IOException {

		if (clearLog) {
			TextView txtViewLog = (TextView) findViewById(R.id.txtlog);
			txtViewLog.setText("");
		}
		final Spinner countrySpinner = (Spinner) findViewById(R.id.spCountries);
		int selectedId = (int) countrySpinner.getSelectedItemId();

		configureGPS(countrycodes[selectedId]);

		try {
			addLog("Saving countrycode for next time...\n");

			directory = getDir("code", MODE_PRIVATE);
			file = new File(directory, "countrycode");

			FileOutputStream fileOut = new FileOutputStream(file);
			saveFile(fileOut, countrycodes[selectedId]);

			addLog("Success! C ya next time :D\n");
			// addLog("");
			addLog("This application was brought to you by djnilse@xda\n");
			addLog("Thanks go to all of THE #GingerDX IRC channel :D");
			clearLog = true;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	// load the last choosed country
	private String loadCountryCode(FileInputStream fileIn) throws IOException {
		BufferedReader fileInBuffer = new BufferedReader(new InputStreamReader(
				fileIn));
		String code = "";
		String zeile;
		try {
			while ((zeile = fileInBuffer.readLine()) != null) {
				code += zeile;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			fileInBuffer.close();
		}
		return code;
	}

	// writes a text in text and saves it at the defined location
	private void saveFile(FileOutputStream fileOut, String text)
			throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(fileOut);
		try {
			writer.write(text);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			writer.close();
		}
	};

	private void configureGPS(String code) throws IOException,
			InterruptedException {

                static Runtime rt = Runtime.getRuntime();
                addLog("Backing up old gps.conf if necessary... \n");
                Process gpsbackup = rt.exec(
                                new String[] {
                                                "su",
                                                "-c",
                                                "test -f /system/etc/gps.conf.orig || cp -a /system/etc/gps.conf /system/etc/gps.conf.orig" });
                gpsbackup.waitFor();
		addLog("Changing country in gps.conf... \n");
		Process setnewgps = rt.exec(
				new String[] {
						"su",
						"-c",
						"sed 's/" + ".*NTP_SERVER=.*/NTP_SERVER=" + code
								+ ".pool.ntp.org"
								+ "/g' -i /system/etc/gps.conf" });
		setnewgps.waitFor();
		Process setPerms = Runtime.getRuntime().exec(
				new String[] { "su", "-c",
						"chmod 0644 " + "/system/etc/gps.conf" });
		setPerms.waitFor();

		Resources res = getResources();
		String countries[] = res.getStringArray(R.array.countries);
		for (int i = 0; i <= 83; i++) {
			if (countrycodes[i].equals(code)) {
				addLog("gps.conf now configured for " + countries[i] + "\n");
				break;
			}
		}

	}

	private void addLog(String log) {
		TextView txtViewLog = (TextView) findViewById(R.id.txtlog);
		txtViewLog.setText(txtViewLog.getText().toString() + log);
	}

	/*
	 * remount system to write to /system/ or to finish up
	 */
	private void remountSystem(boolean doRW) throws IOException, InterruptedException {
                String mountPerms = (doRW ? "rw" : "ro");
		Process remount = Runtime
				.getRuntime()
				.exec(new String[] { "su", "-c",
						"mount -o " + mountPerms + ",remount /system" });
		remount.waitFor();
	}
}

