import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Servidor {
	/**
	 * Constantes
	 */
	public static final String EXIT = "SALIR";

	/**
	 * Puerto por donde el servidor atendera a los clientes
	 */
	public static final int PORT = 9000;
	/**
	 * El servidor dispone de un serversocket, para permitir la conexion a los
	 * clientes
	 */
	private static ServerSocket serverSocket;
	/**
	 * El servidor dispone de un socket para atender a cada cliente por individual
	 */

	/**
	 * Llave para la encriptacion cesar
	 */
	private static int key = (int) Math.random() * 20 + 1;
	
	/**
	 * Arraylis para obtener varios clientes y recibir varias peticiones
	 */
	private static ArrayList<Socket> clientes = new ArrayList<Socket>();
	private static ArrayList<DataInputStream> in = new ArrayList<DataInputStream>();
	private static ArrayList<DataOutputStream> out = new ArrayList<DataOutputStream>();

	public static void main(String[] args) {
//		DataInputStream in;
//		DataOutputStream out;
		try {
			serverSocket = new ServerSocket(PORT);
			System.out.println("::Servidor escuchando a los posibles clientes::");

//			while (true) {
			/*
			 * Esperando respuesta del usuario, solo acepta uno a uno, usar hilos para
			 * atender a varios usuarios al timepo.
			 */

			Runnable conexion = new conexion();
			new Thread(conexion).start();

			while (true) {}

			// Recibo el mensaje del cliente1
			// Envio el mensaje al cliente2
//				out2.writeUTF(in1.readUTF());
//
//				// Recibo el mensaje del cliente2
//				// Envio el mensaje al cliente1
//				out1.writeUTF(in2.readUTF());

//				// Palabara y llave encriptada
//				String wordEncrypted = encriptacionCesar(word, key);
//				String keyEncrypted = encriptacionHexadecimal(key + "");
//
//				// Desencriptar palabra y key
//				String keyDecrypted = desencriptacionHexadecimal(keyEncrypted);
//				String wordDecryted = desencriptacionCesar(wordEncrypted, Integer.parseInt(keyDecrypted));
//
//				out.writeUTF("La palabra Encriptada es: " + wordEncrypted + "\n" + "La clave encriptada es: "
//						+ keyEncrypted + "\n" + "La palabra Desencriptada es: " + wordDecryted + "\n"
//						+ "La clave Desencriptada es: " + keyDecrypted + "\n");

			// Cierro la conexion con el cliente
//				cliente1.close();
//				cliente2.close();
//				in1.close();
//				out1.close();
//
//				in2.close();
//				out2.close();
//			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	/**
	 * hilo comunicacion, sirve para enviar el mensaje de un usuario a todos los demas.
	 * @author Joe
	 *
	 */
	public static class comunicacion implements Runnable {
		private Socket cliente;
		private int numCliente;
		private boolean exit = false;

		public comunicacion(Socket cliente) {
			this.cliente = cliente;
			this.numCliente = clientes.indexOf(cliente);
		}

		@Override
		public void run() {
			while (!exit) {
				try {
					String msj = in.get(numCliente).readUTF();
					if (desencriptacionCesar(msj, key).equals(EXIT)) {
						String msjExit = "Usted se ha desconectado del servidor";
						out.get(numCliente).writeUTF(encriptacionCesar(msjExit, key));
						stop();
					} else {
						for (int i = 0; i < clientes.size(); i++) {
							if (i != numCliente) {
								out.get(i).writeUTF(msj);
							}
						}
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		/**
		 * Este metodo sirve para detener el hilo cuando el usuario abandona el servidor
		 * @throws IOException
		 */
		public void stop() throws IOException {
			exit = true;
			cliente.close();
			clientes.remove(numCliente);
			in.get(numCliente).close();
			in.remove(numCliente);
			out.get(numCliente).close();
			out.remove(numCliente);
			System.out.println("::El usuario " + (numCliente+1) + " se ha desconectado::");
		}

	}

	/**
	 * Clase conexion, sirve para inicializar cada cliente que este ingresando al servidor
	 * @author Joe
	 *
	 */
	public static class conexion implements Runnable {
		private int value;

		public conexion() {
			value = 0;
		}

		@Override
		public void run() {
			while (true) {

				try {
					clientes.add(serverSocket.accept());
					value = clientes.size() - 1;
					in.add(new DataInputStream(clientes.get(value).getInputStream()));
					out.add(new DataOutputStream(clientes.get(value).getOutputStream()));
					System.out.println("::El cliente numero " + (value + 1) + " se ha conectado::");
					out.get(value).writeUTF(encriptacionHexadecimal(key+""));
					Runnable comunicacion = new comunicacion(clientes.get(value));
					new Thread(comunicacion).start();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/*
	 * Metodo que se encagar de encriptar la key del encriptado cesar, usando un
	 * metodo de encriptacion hexadecimal, usando los valores de la tablas ascii y
	 * convertirlos a hexadecimal. Consiste en cojer cada numero por aparte
	 * convertirlo a ascii y dicho numero ascii pasarlo a hexadecimal.
	 */
	private static String encriptacionHexadecimal(String key) {
		StringBuilder wordHexadecimal = new StringBuilder();
		char arr[] = key.toCharArray();

		for (int i = 0; i < arr.length; i++) {
			int value = (int) arr[i];
			wordHexadecimal.append(Integer.toHexString(value) + " ");
		}

		return wordHexadecimal.toString().trim();
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
