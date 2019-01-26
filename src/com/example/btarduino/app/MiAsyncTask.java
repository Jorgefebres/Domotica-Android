package com.example.btarduino.app;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
* Created by witchraper on 24/01/15.
*/
public class MiAsyncTask extends AsyncTask<BluetoothDevice, Sensor, Void> {
	//private static final String TAG = "MiAsyncTask";

	// Identificador unico universal del puerto bluetooth en android (UUID)
	private static final String UUID_SERIAL_PORT_PROFILE = "00001101-0000-1000-8000-00805F9B34FB";
	private Sensor sensor = new Sensor();
	public BluetoothSocket mSocket = null;
	private BufferedReader mBufferedReader = null;
	private MiCallback callback;
	private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	private boolean recibiendo = false;
	private InputStream iStream = null;
	private InputStreamReader iReader = null;
	private int contadorConexiones = 0;

	public interface MiCallback {
		void onTaskCompleted();

		void onCancelled();

		void onSensorUpdate(Sensor p);
	}

	public MiAsyncTask(MiCallback CALLBACK) {
		callback = CALLBACK;

	}

	@Override
	protected Void doInBackground(BluetoothDevice... devices) {

		final BluetoothDevice device = devices[0];

		// Realizamos la conexion al disp.blueetoth. A veces la conexion falla
		// aunque el dispositivo
		// este presente. Asi que si falla, y la tarea no ha sido cancelada, lo
		// reintentamos.
		while (!isCancelled()) {
			if (!recibiendo) {
				recibiendo = conectayRecibeBT(device);
			}
		}
		cierra();
		return null;
	}

	private boolean conectayRecibeBT(BluetoothDevice device) {
		// Abrimos la conexión con el dispositivo.
		boolean ok = true;

		try {
			contadorConexiones++;

			mSocket = device
					.createRfcommSocketToServiceRecord(getSerialPortUUID());
			mSocket.connect();
			iStream = mSocket.getInputStream();
			iReader = new InputStreamReader(iStream);
			mBufferedReader = new BufferedReader(iReader);

			sensor.setInformacion("Sin datos...");
			publishProgress(sensor);
			/*
			 * Mientras no se cancele la tarea asincrona (cuando se destruya la
			 * actividad) se interroga al canal de comunicación por la
			 * temperatura
			 */

			while (!isCancelled()) {

				try {

					String aString = mBufferedReader.readLine();
					if ((aString != null) && (!aString.isEmpty())) {
						// Instante de tiempo en que recuperamos un dato.
						sensor.setInformacion(sdf.format(new Date()));

						// Recibimos la información en una cadena de la forma
						// YY,XX
						//donde YY es la luminosidad
						// donde XX es la temperatura.
						try {

							String s[] = aString.split(",");
							sensor.setLuminosidad(s[0]);
							sensor.setTemperatura(s[1]);
							publishProgress(sensor);
						} catch (Exception e) {
							// Si falla el formateo de los datos, no hacemos
							// nada. Mostramos la excepción en la consola para
							// observar el error.
							e.printStackTrace();
						}

					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

			// Una vez la tarea se ha cancelado, cerramos la conexión con el
			// dispositivo bluetooth.
			sensor.setInformacion("Cerrando conexion BT");

		} catch (IOException e) {
			ok = false;
			e.printStackTrace();
			sensor
					.setInformacion("Error conectando con dispositivo bt, reintento "
							+ contadorConexiones
							+ "... Si este error se repite, reinicie el arduino.");
			publishProgress(sensor);
			cierra();

		}
		return ok;
	}

	private void cierra() {
		close(mBufferedReader);
		close(iReader);
		close(iStream);
		close(mSocket);
	}

	private UUID getSerialPortUUID() {
		return UUID.fromString(UUID_SERIAL_PORT_PROFILE);
	}

	private void close(Closeable aConnectedObject) {
		if (aConnectedObject == null)
			return;
		try {
			aConnectedObject.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		aConnectedObject = null;
	}

	@Override
	protected void onProgressUpdate(Sensor... values) {
		super.onProgressUpdate(values);
		callback.onSensorUpdate(values[0]);
	}

	@Override
	protected void onCancelled() {
		callback.onCancelled();
	}
}
