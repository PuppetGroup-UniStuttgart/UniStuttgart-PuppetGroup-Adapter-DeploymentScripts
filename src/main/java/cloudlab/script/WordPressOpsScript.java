package cloudlab.script;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.*;
import java.util.*;

/**
 * WordPressOpsScript: Invokes the JSON Adapter API in order to access the WordPressOps Service 
 * provided by the gRPC API to deploy WordPress onto the EC2 instance. The invocation and deployment
 * is automated by invoking the JSON Adapter API in a loop using 3 different methods as required 
 * by the WordPress gRPC API
 *
 * Created by PuppetGroup on 03-06-2016.
 */
public class WordPressOpsScript {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Now it begins");
        
	int i = 0;
	String[] methodNames = {"deployApp", "deployDB", "connectAppToDB"};
		
	//Read the config.properties file to obtain the values required values
	Properties properties = new Properties();
        InputStream propIn = new FileInputStream(new File("config.properties"));
        properties.load(propIn);

        String[] requestParameters = {
                properties.getProperty("keyPair"),
                properties.getProperty("bucketName"),
                properties.getProperty("username"),
                properties.getProperty("publicIP")
        };

	String url = "http://localhost:8080/generic-adapter/request";
        HttpClient client = HttpClientBuilder.create().build();

	while (i < 3)
	{
		String method = methodNames[i];
		System.out.println("method = " + method);

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("requestParameters", requestParameters);
		params.put("serviceName", "WordPressOps"));

		System.out.println("params = " + params);

		String id = methodNames[i];

		JSONRPC2Request reqOut = new JSONRPC2Request(method, params, id);
		String jsonString = reqOut.toString();

		HttpPost post = new HttpPost(url);
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("jsonString", jsonString));
		post.setEntity(new UrlEncodedFormEntity(urlParameters));
		HttpResponse response = client.execute(post);
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + post.getEntity());
		System.out.println("Response Code : " +
				response.getStatusLine().getStatusCode());
		BufferedReader rd = new BufferedReader(
		new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}

		String jsonResponse = result.toString();
		System.out.println("jsonResponse = " + jsonResponse);
		
		// Parse response string
		JSONRPC2Response respIn = null;

		try {
			respIn = JSONRPC2Response.parse(jsonResponse);
		} catch (JSONRPC2ParseException e) {
			System.out.println(e.getMessage());
		}


		// Check for success or error
		if (respIn.indicatesSuccess()) {
			System.out.println("The request succeeded :");
			System.out.println("\tresult : " + respIn.getResult());
			System.out.println("\tid     : " + respIn.getID());
		} else {
			System.out.println("The request failed :");
			JSONRPC2Error err = respIn.getError();
			System.out.println("\terror.code    : " + err.getCode());
			System.out.println("\terror.message : " + err.getMessage());
			System.out.println("\terror.data    : " + err.getData());
		}
		i++,
	}

    }
}
