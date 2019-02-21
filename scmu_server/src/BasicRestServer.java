

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.sun.net.httpserver.HttpServer;

public class BasicRestServer {

	public static void main(String[] args) throws Exception {

		URI baseUri = UriBuilder.fromUri("http://0.0.0.0/").port(8080).build();

		ResourceConfig config = new ResourceConfig();

		ArduinoResource arduino = new ArduinoResource();
		config.register(arduino);
		
		HttpServer server = JdkHttpServerFactory.createHttpServer(baseUri, config);

		System.err.println("REST Server ready... ");

		ServerSocket serverSocket = new ServerSocket(8181);
		try { 
			while (true) {
				Socket clientSocket = serverSocket.accept();
				PrintWriter out =
						new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(
						new InputStreamReader(clientSocket.getInputStream()));
				/*String arduinoData = in.readLine();
				System.out.println(arduinoData);
				String[] arduinoParams = arduinoData.split(",");
				if(arduinoParams[0].equals("1"))
					arduino.sendMessage();*/
				PreferencesClass pref = arduino.preferences;
				int ac, blinds;
				ac = pref.acOn ? 1 : 0;
				blinds = pref.openBlinds ? 1 : 0;
				out.print(ac + " " + blinds+"\r");
				out.flush();
				arduino.data.setTemperature(in.readLine());
				System.out.println(arduino.data.temperature);
				arduino.data.setLuminosity(in.readLine());
				System.out.println(arduino.data.luminosity);
				String intrusion = in.readLine();
				System.out.println(intrusion);
				if (intrusion.equals("1") && arduino.preferences.instrusionDetection)
					arduino.sendMessage();
			}
		} catch (Exception e){
			e.printStackTrace();
		} finally{
			serverSocket.close();
		}
	}
}