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
 * GenericOpsScript: Invokes the JSON Adapter API in order to access the GenericOps Service 
 * provided by the Generic gRPC API to deploy any given generic service onto the EC2 instance. 
 *
 * Created by PuppetGroup on 26-05-2016.
 */
public class GenericOpsScript {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Now it begins");
        
        //Read the config.properties file to obtain the values required values
        Properties properties = new Properties();
        InputStream propIn = new FileInputStream(new File("config.properties"));
        properties.load(propIn);

        String method = properties.getProperty("method");
        System.out.println("method = " + method);

        String[] requestParameters = {
                properties.getProperty("keyPair"),
                properties.getProperty("bucketName"),
                properties.getProperty("username"),
                properties.getProperty("publicIP"),
                properties.getProperty("moduleName"),
                properties.getProperty("installFile")
        };

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("requestParameters", requestParameters);
        params.put("serviceName", properties.getProperty("serviceName"));

        System.out.println("params = " + params);

        String id = properties.getProperty("keyPair");

        JSONRPC2Request reqOut = new JSONRPC2Request(method, params, id);
        String jsonString = reqOut.toString();

        String url = "http://localhost:8080/generic-adapter/request";
        HttpClient client = HttpClientBuilder.create().build();
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
            // Handle exception...
        }


// Check for success or error
        if (respIn.indicatesSuccess()) {
            System.out.println("The request succeeded :");
            System.out.println("\tresult : " + respIn.getResult());
        } else {
            System.out.println("The request failed :");
            JSONRPC2Error err = respIn.getError();
            System.out.println("\terror.code    : " + err.getCode());
            System.out.println("\terror.message : " + err.getMessage());
            System.out.println("\terror.data    : " + err.getData());
        }

    }
}
