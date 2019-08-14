import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Cliente {
	/**
	 * Constantes
	 */
	public static final String EXIT = "SALIR";
	public static final String EXITSERVER = "Usted se ha desconectado del servidor";
	public static final String CLIENTES = "LISTA";
	public static final String EMPTY = "No hay nadie con quien conversar";
//	public static final String CONEXION = "CONECTAR";

	/*
	 * 
	 * Direccion local de la maquina
	 */
	public static final String LOCAL_HOST = "localhost";


	/**
	 * Puerto por donde se establecera la conexion
	 */
	public static final int PORT = 9000;
	/**
	 * Socket que permitira la conexion con el servidor
	 */
	private static Socket socket;
	
	private static boolean exit = false;
	private static boolean conexion = false;
	
	/**
	 * Buffers
	 */
	private static BufferedReader br;
	private static BufferedWriter bw;
	private static DataInputStream in;
	private static DataOutputStream out;
	private static int key;

	/**
	 * Main
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			br = new BufferedReader(new InputStreamReader(System.in));
			bw = new BufferedWriter(new OutputStreamWriter(System.out));

			socket = new Socket(LOCAL_HOST, PORT);
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			
			bw.write("::Se ha conectado al servidor::\n");
			bw.write("::Si desea salir del chat escriba SALIR::\n");
			bw.write("::Si desea ver la lista de usuario escriba LISTA::\n");
			bw.write("::Si desea conectar con un usuario escriba CONECTAR;idUsuario::\n");
			bw.flush();
			
			key = Integer.parseInt(desencriptacionHexadecimal(in.readUTF()));
			
			/*
			 * Hilo para lectura 
			 */
			Runnable lectura = new lectura();
			new Thread(lectura).start();
			
			/*
			 * Hilo para escritura 
			 */
			Runnable escritura = new escritura();
			new Thread(escritura).start();
			
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}

	}
	
	/**
	 * Hilo lectura, un hilo que sirve para estar siempre escuchando las respuestas del servidor
	 * @author Joe
	 *
	 */
	public static class lectura implements Runnable {
		
		@Override
		public void run() {
			while (!exit) {
				try {
					String word = in.readUTF();
					if (conexion  && !word.equals(EXITSERVER)) {
						word = desencriptacionCesar(word, key);
					}
					if (conexion  && word.equals(EMPTY)) {
						conexion = false;
					}
					
					bw.write(word+"\n");
					bw.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			try {
				// Cierro los buffers
				br.close();
				bw.close();
				
				// Cierro la conexion con el servidor
				socket.close();
				in.close();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	
	/**
	 * Hilo de escritura siempre esta pendiente para enviar mensajes al servidor
	 * @author Joe
	 *
	 */
	public static class escritura implements Runnable {
		@Override
		public void run() {
			while (!exit) {
				try {
					bw.write("-");
					bw.flush();
					String word = br.readLine();
					if (word.equals(EXIT)) {
						exit = true;
					}
					
					else {
						word = encriptacionCesar(word, key);
					}
					out.writeUTF(word);

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
	
	/*
	 * Metodo que se encagar de desencriptar el valor hexadecimal y llevarlo a
	 * decimal
	 */
	private static String desencriptacionHexadecimal(String key) {
		StringBuilder wordDecimal = new StringBuilder();
		String arr[] = key.split(" ");

		for (int i = 0; i < arr.length; i++) {
			int value = Integer.parseInt(arr[i], 16);
			wordDecimal.append((char) value + "");
		}

		return wordDecimal.toString();
	}
	

	/*
	 * Metodo que se encarga de encritar la palabra orginal, usando el cifrado
	 * cesar, el cual consiste en generar un numero del 1-20 y desplazar hacia la
	 * izquierda o derecha la letra dicha cantidad en el alfabeto. En este caso se
	 * hara hacia la derecha
	 */
	private static String encriptacionCesar(String word, int key) {
		StringBuilder wordEncrypted = new StringBuilder();
		char arr[] = word.toCharArray();

		for (int i = 0; i < arr.length; i++) {
			if (Character.isAlphabetic(arr[i])) {
				if (Character.isUpperCase(arr[i])) {
					int value = arr[i] - 'A';
					value = (value + key) % 26;
					value += 'A';
					wordEncrypted.append((char) value + "");
				} else {
					int value = arr[i] - 'a';
					value = (value + key) % 26;
					value += 'a';
					wordEncrypted.append((char) value + "");
				}
			} else
				wordEncrypted.append(arr[i] + "");
		}

		return wordEncrypted.toString();
	}

	/*
	 * Metodo que desencripta la palabra encriptada
	 */
	private static String desencriptacionCesar(String word, int key) {
		StringBuilder wordDecrypted = new StringBuilder();
		char arr[] = word.toCharArray();

		for (int i = 0; i < arr.length; i++) {
			if (Character.isAlphabetic(arr[i])) {
				if (Character.isUpperCase(arr[i])) {
					int value = arr[i] - 'A';
					value = (value - key);
					if (value < 0)
						value = 26 + value;
					else
						value %= 26;
					value += 'A';
					wordDecrypted.append((char) value + "");
				} else {
					int value = arr[i] - 'a';
					value = (value - key);
					if (value < 0)
						value = 26 + value;
					else
						value %= 26;
					value += 'a';
					wordDecrypted.append((char) value + "");
				}
			} else
				wordDecrypted.append(arr[i] + "");
		}

		return wordDecrypted.toString();
	}

}

