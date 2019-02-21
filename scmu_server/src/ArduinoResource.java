
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.filter.LoggingFilter;
import org.json.*;

@Path("/arduino")
public class ArduinoResource {

    public static final String SERVER_KEY = "AIzaSyBJCdlDiHdONY73zFM1A1VXJwGAR3mdvEA";

	public DataClass data = null;
	public PreferencesClass preferences = null;
	static String deviceToken = null;
	WebTarget target;
	
	public ArduinoResource(){
		super();
		data = new DataClass();
		preferences = new PreferencesClass();
		ClientConfig config = new ClientConfig();
	    Client client = ClientBuilder.newClient(config);
	    //client.register(new LoggingFilter());

	    this.target = client.target("https://gcm-http.googleapis.com/gcm/send");
	}

	@GET
	@Path("/data")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getData() {
		return Response.ok(data).build();
	}
	
	@POST
	@Path("/blinds/{state}")
	public Response blindsState(@PathParam("state") String bState) {
		System.out.println(bState);
		if(bState.equalsIgnoreCase("open"))
			preferences.openBlinds = true;
		else
			preferences.openBlinds = false;
		return Response.ok().build();
	}
	
	@POST
	@Path("/ac/{state}")
	public Response acState(@PathParam("state") String acState) {
		System.out.println(acState);
		if(acState.equalsIgnoreCase("on"))
			preferences.acOn = true;
		else
			preferences.acOn = false;
		return Response.ok().build();
	}
	
	@POST
	@Path("/intrusion/{state}")
	public Response intrusionState(@PathParam("state") String intrusion) {
		System.out.println(intrusion);
		if(intrusion.equalsIgnoreCase("on"))
			preferences.instrusionDetection = true;
		else
			preferences.instrusionDetection = false;
		return Response.ok().build();
	}
	
	@POST
	@Path("/registerdevice")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response registerDevice(String token) {
		if(token != null){
			try {
				JSONObject json = new JSONObject(token);
				deviceToken = json.getString("token");
			} catch (JSONException e) {
				e.printStackTrace();
			}System.out.println("Register Device: "+deviceToken);
			return Response.ok().build();
		}
		else
			return Response.status(422).build();
	}
	
	public void sendMessage(){
		JSONObject json = new JSONObject();
		JSONObject data = new JSONObject();
		if(deviceToken!=null){
			try {
				json.put("to", deviceToken);
				data.put("message", "Intrusion Detected");
				SimpleDateFormat sdfDate = new SimpleDateFormat("H:m");
				Date now = new Date();
				String strDate = sdfDate.format(now);
				data.put("time", strDate);
				json.put("data", data);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			Response response = target.request()
					.header("Content-Type", "application/json")
					.header("Authorization", "key="+SERVER_KEY)
					.accept(MediaType.APPLICATION_JSON)
					.post(Entity.json(json.toString()));
		}
	}

}
