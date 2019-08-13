import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

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
	 * 
	 */
	private static int iClientes = 1;
	
	/**
	 * Mapas para obtener varios clientes y recibir varias peticiones
	 */
	private static HashMap<Integer, Socket> clientes = new HashMap<Integer, Socket>();
	private static HashMap<Integer, DataInputStream> inC = new HashMap<Integer, DataInputStream>();
	private static HashMap<Integer, DataOutputStream> outC= new HashMap<Integer, DataOutputStream>();

	public static void main(String[] args) {
		try {
			serverSocket = new ServerSocket(PORT);
			System.out.println("::Servidor escuchando a los posibles clientes::");

			/*
			 * Esperando respuesta del usuario, solo acepta uno a uno, usar hilos para
			 * atender a varios usuarios al timepo.
			 */

			Runnable conexion = new conexion();
			new Thread(conexion).start();

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
		private DataInputStream in;
		private DataOutputStream out;
		
		public comunicacion(Socket cliente, int numClient,  DataInputStream in, DataOutputStream out) {
			this.cliente = cliente;
			this.numCliente = numClient;
			this.in = in;
			this.out = out;
		}

		@Override
		public void run() {
			while (!exit) {
				try {
					String msj = in.readUTF();
					if (msj.equals(EXIT)) {
						String msjExit = "Usted se ha desconectado del servidor";
						out.writeUTF(msjExit);
						stop();
					} else {
						for (Map.Entry<Integer, Socket> pair : clientes.entrySet()) {
							int v = pair.getKey();
							if (v != numCliente) 
								outC.get(v).writeUTF(msj);
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
			in.close();
			out.close();
			inC.remove(numCliente);
			outC.remove(numCliente);
			System.out.println("::El usuario " + (numCliente) + " se ha desconectado::");
		}

	}

	/**
	 * Clase conexion, sirve para inicializar cada cliente que este ingresando al servidor
	 * @author Joe
	 *
	 */
	public static class conexion implements Runnable {

		@Override
		public void run() {
			while (true) {

				try {
					int value = iClientes++;
					clientes.put(value, serverSocket.accept());
					Socket temp = clientes.get(value);
					DataInputStream in =  new DataInputStream(temp.getInputStream());
					DataOutputStream out = new DataOutputStream(temp.getOutputStream());
					inC.put(value, in);
					outC.put(value, out);
					
					System.out.println("::El cliente numero " + value + " se ha conectado::");
					System.out.println(temp.getLocalPort());
					System.out.println(temp.getPort());
					out.writeUTF(encriptacionHexadecimal(key+""));
					
					Runnable comunicacion = new comunicacion(temp, value, in, out);
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
}
