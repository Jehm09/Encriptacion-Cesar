import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Cliente {
	/*
	 * 
	 * Direccion local de la maquina
	 */
	public static final String LOCAL_HOST = "localhost";
//	public static final String LOCAL_HOST = "120.0.0.1";
	/**
	 * Puerto por donde se establecera la conexion
	 */
	public static final int PORT = 8000;
	/**
	 * Socket que permitira la conexion con el servidor
	 */
	private static Socket socket;

	/**
	 * Main
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		DataInputStream in = null;
		DataOutputStream out = null;

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));

			socket = new Socket(LOCAL_HOST, PORT);
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());

			bw.write("::Cliente disponible para ser atendido::\n");
			bw.flush();

			bw.write("Ingrese la palabra que desea encriptar\n");
			bw.flush();
			
			String word = br.readLine();
			// Envia el mensaje al servidor
			out.writeUTF(word);

			// Obtiene respuesta del servidor
			String mensajeDelServidor = in.readUTF();
			bw.write(mensajeDelServidor);
			bw.flush();

			// Cierro los buffers
			bw.close();
			br.close();

			// Cierro la conexion con el servidor
			socket.close();
			in.close();
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}

	}

}