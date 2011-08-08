package yaosupdater.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.impl.conn.tsccm.WaitingThread;

import yaosupdater.ui.Principal;


import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class ApplyUpdate {
	
	private static String file;
	private static boolean acceptNoMD5 = false;
	
	public static void aplicarUpdate(String archivo, boolean md5){
		//Si la verificación de md5 está activada, se llama al método de verificación
		if(md5){
			//Si el método de verificación devuelve false, volvemos atrás
			if(!comprobarMD5SUM(archivo)){
				return;
			}
		}
		Handler dialogHandler = new Handler(){
			@Override
			public void handleMessage(final Message msg) {
				super.handleMessage(msg);
				switch(msg.what){
				case(1):
					AlertDialog.Builder alertBuilder = new AlertDialog.Builder(Principal.myContext());
					alertBuilder.setMessage("El teléfono se reiniciará, ¿está seguro de que desea reiniciar?");
					alertBuilder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String dispositivo = reconocerDispositivo();
							try {
								String archivo = file;
								//Creamos el proceso su y hacemos un buffer os para escribir en una consola de linux
								Process su = Runtime.getRuntime().exec("su");
								DataOutputStream os = new DataOutputStream(su.getOutputStream());
								archivo = archivo.replace("/sdcard/", "");
								Log.d("ARCHIVO", archivo);
								os.writeBytes("echo boot-recovery > /cache/recovery/command\n");
								os.writeBytes("echo --update_package=SDCARD:"+archivo+" >> /cache/recovery/command\n");
								os.flush();
								Log.d("DISPOSITIVO",dispositivo);
								//Para estos dispositivos es necesaria un pequeño hack, de lo contrario, el recovery no aplica la actualización
								if((dispositivo.equals("galaxysmtd"))||(dispositivo.equals("umts_sholes"))||(dispositivo.equals("leo"))||(dispositivo.equals("umts_jordan"))){
									os.writeBytes("rm /cache/recovery/command\n");
					            	os.writeBytes("echo 'install_zip(\"/sdcard/"+archivo+"\");' > /cache/recovery/extendedcommand\n");
					            	os.writeBytes("reboot recovery\n");
								}else{
									os.writeBytes("reboot recovery\n");
								}
								//Se ejecutan las líneas
								os.flush();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});
					alertBuilder.setTitle("Atención");
					alertBuilder.setNegativeButton("Cancelar", null);
					AlertDialog dialog = alertBuilder.create();
					dialog.show();
					break;
				}
			}
		};
		file = archivo;
		dialogHandler.sendEmptyMessage(1);
	}
	
	private static String reconocerDispositivo(){
		String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop ro.product.device");
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
            return line;
        }catch (IOException ex) {
            Log.e("MIUIESUPDATER", "Unable to read sysprop ro.product.device", ex);
            return null;
        }
	}
	
	private static boolean comprobarMD5SUM(final String archivo){
		//Se lee el archivo .md5sum
		File md5File = new File(archivo+".md5sum");
		try {
			FileInputStream fis = new FileInputStream(md5File);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String md5 = br.readLine();
			//Se leen los 32 primeros caracteres (la cadena md5 generada)
			md5 = md5.substring(0, 32);
			File archivoFile = new File(archivo);
			//Comprobamos que la cadena leída sea la misma que la generada al comprobar el archivo descargado
			if(MD5.checkMD5(md5, archivoFile)){
				return true;
			}else{
				Handler handler = new Handler(){
					@Override
			        public void handleMessage(Message msg) {
			            super.handleMessage(msg);
	
			            switch (msg.what) {
			            case 1:
			            	Toast.makeText(Principal.myContext(), "El archivo descargado está corrupto, vuelva a descargarlo y pruebe de nuevo.", Toast.LENGTH_LONG).show();
			            	break;
			            }
					}
				};
				handler.sendEmptyMessage(1);
			}
		} catch (FileNotFoundException e) {
			//En caso de que no se encuentre el archivo
			final Handler fnfHandler = new Handler(){
				@Override
				public void handleMessage(final Message msg) {
					super.handleMessage(msg);
					AlertDialog.Builder alert = new AlertDialog.Builder(Principal.myContext());
					alert.setTitle("Archivo MD5 no encontrado");
					alert.setMessage("No se encontró ningún archivo MD5 para verificar la integridad del archivo.\n¿Desea intentar instalarlo de todas formas?");
					alert.setPositiveButton("Sí", new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							//Si se selecciona "sí", aplicamos la actualización aún sin tener el md5
							aplicarUpdate(archivo, false);
						}
					});
					alert.setNegativeButton("No", new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							acceptNoMD5 = false;
						}
					});
					alert.show();
				}
			};
			Thread fnfThread = new Thread(new Runnable() {
				@Override
				public void run() {
					fnfHandler.sendEmptyMessage(0);
				}
			});
			fnfThread.start();
			
			if(acceptNoMD5){
				return true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
}
