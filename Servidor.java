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
	public static final String CLIENTES = "LISTA";
	public static final String CONEXION = "CONECTAR";

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
//	private static int key = (int) Math.random() * 20 + 1;
	
	/**
	 * Mapas para obtener varios clientes y recibir varias peticiones
	 */
	private static HashMap<Long, Socket> clientes = new HashMap<Long, Socket>();
	private static HashMap<Long, Long> conexion = new HashMap<Long, Long>();
	private static HashMap<Long, DataInputStream> inC = new HashMap<Long, DataInputStream>();
	private static HashMap<Long, DataOutputStream> outC= new HashMap<Long, DataOutputStream>();

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
		private long idCliente;
		private long idCliente2;
		private boolean exit = false;
		private DataInputStream in;
		private DataOutputStream out;
//		private int key;
		
		public comunicacion(Socket cliente, long idCliente,  DataInputStream in, DataOutputStream out) {
			this.cliente = cliente;
			this.idCliente = idCliente;
			this.in = in;
			this.out = out;
//			this.key = -1;
			this.idCliente2 = -1;
		}

		@Override
		public void run() {
			while (!exit) {
				try {
					String msj = in.readUTF();
					if (idCliente2 != -1 && conexion.containsKey(idCliente)) {
						idCliente2 = conexion.get(idCliente);
					}
					
					
					if (msj.equals(EXIT)) { //Si el usuario quiere salir del servidor
						String msjExit = "Usted se ha desconectado del servidor";
						out.writeUTF(msjExit);
						
						if (idCliente2 != -1) {
							out.writeUTF("No hay nadie con quien conversar");
						}
						stop();
					} else if(msj.equals(CLIENTES)) { //Muestra la lista de clientes
						out.writeUTF("Esta es la lista de clientes conectados:\n");
						for (Map.Entry<Long, Socket> pair : clientes.entrySet()) {
							long v = pair.getKey();
							if (v != idCliente) 
								out.writeUTF(v+"");
						}
					}
					else if(msj.contains(CONEXION)) { //Por si un usuario quiere crear un chat con otro usuario
						long id = Long.parseLong(msj.split(";")[1]);
						outC.get(id).writeUTF("El usuario "+ idCliente + "ha creado un chat");
						
						int k = (int) Math.random() * 20 + 1;
						String key = encriptacionHexadecimal(k+"");
						
						out.writeUTF("La llave es: " + key +"\n");
						outC.get(id).writeUTF("La llave es: " + key +"\n");
						out.writeUTF("KEY;" + key +"\n");
						outC.get(id).writeUTF("KEY;" + key +"\n");
						
						conexion.put(idCliente, id);
						conexion.put(id, idCliente);
					}
					
					else { //Si ya hay un usuario con el cual hacer conversacion. Se le envia los mensajes
						if (clientes.containsKey(idCliente2)) {
							outC.get(idCliente2).writeUTF(msj);
						} else {
							idCliente2 = -1;
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
			clientes.remove(idCliente);
			in.close();
			out.close();
			inC.remove(idCliente);
			outC.remove(idCliente);
			System.out.println("::El usuario " + (idCliente) + " se ha desconectado::");
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
					long id = System.nanoTime();
					clientes.put(id, serverSocket.accept());
					Socket temp = clientes.get(id);
					DataInputStream in =  new DataInputStream(temp.getInputStream());
					DataOutputStream out = new DataOutputStream(temp.getOutputStream());
					inC.put(id, in);
					outC.put(id, out);
					
					System.out.println("::El cliente con id " + id + " se ha conectado::");
					System.out.println(temp.getLocalPort());
					System.out.println(temp.getPort());
//					out.writeUTF(encriptacionHexadecimal(key+""));
					
					Runnable comunicacion = new comunicacion(temp, id, in, out);
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
			int value = arr[i];
			wordHexadecimal.append(Integer.toHexString(value) + " ");
		}

		return wordHexadecimal.toString().trim();
	}
}
