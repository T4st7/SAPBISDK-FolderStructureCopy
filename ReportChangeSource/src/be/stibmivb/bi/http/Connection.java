package be.stibmivb.bi.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

public class Connection {
	
	private static HttpURLConnection conn;
	
	public Connection() throws IOException {
	}
	
	public void openConn(URL uriEndPoint) throws IOException {
		conn = (HttpURLConnection)uriEndPoint.openConnection();
	}
	public void buildRequest(String[][] requestProps, String requestMethod) throws ProtocolException {
		conn.setRequestMethod(requestMethod);
		for (int row=0; row < requestProps.length; row ++) {
				conn.setRequestProperty(requestProps[row][0], requestProps[row][1]);
			}
	}
	public void buildRequest(String[][] requestProps, String requestMethod, JSONObject objJSON) throws IOException {
		conn.setDoOutput(true);
        conn.setDoInput(true);
		conn.setRequestMethod(requestMethod);
		for (int row=0; row < requestProps.length; row ++) {
				conn.setRequestProperty(requestProps[row][0], requestProps[row][1]);
			}
		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(),StandardCharsets.UTF_8);
		wr.write(objJSON.toString());
		wr.flush();
	}
	public void testResponse() throws IOException {
		if (conn.getResponseCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
		}
	}
	public void closeConn() {
		conn.disconnect();
	}
	
	//-----------------------------------------------------
	//******************----GET----************************
	//-----------------------------------------------------
	
	//RESPONSE
	public StringBuilder getResponse() throws IOException {
		StringBuilder builder = new StringBuilder();
		BufferedReader br = new BufferedReader( 
				(new InputStreamReader(conn.getInputStream(),StandardCharsets.UTF_8)));
		String output;
		while ((output = br.readLine()) != null) {
			builder.append(output).append("\n");
		}
		return builder;
	}
}
