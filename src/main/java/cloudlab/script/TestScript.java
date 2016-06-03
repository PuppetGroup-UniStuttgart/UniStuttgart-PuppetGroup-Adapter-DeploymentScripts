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
 * Created by shreyasbr on 31-05-2016.
 */
public class TestScript {
    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("Waiting for server to initialize");
        Thread.sleep(90000);
        System.out.println("Now it begins");
        Properties properties = new Properties();
        InputStream propIn = new FileInputStream(new File("config.properties"));
        properties.load(propIn);

        String serviceName = properties.getProperty("serviceName");
        String method = properties.getProperty("method");

        String[] requestParameters = new String[2];

        if (serviceName.equals("StringOps")) {
            requestParameters[0] = properties.getProperty("name");
            requestParameters[1] = properties.getProperty("place");
        } else if (serviceName.equals("IntegerOps")) {
            requestParameters[0] = properties.getProperty("firstNumber");
            requestParameters[1] = properties.getProperty("secondNumber");
        } else if (serviceName.equals("DoubleOps")) {
            requestParameters[0] = properties.getProperty("firstNumber");
            requestParameters[1] = properties.getProperty("secondNumber");
        } else if (serviceName.equals("FloatOps")) {
            requestParameters[0] = properties.getProperty("firstNumber");
            requestParameters[1] = properties.getProperty("secondNumber");
        } else if (serviceName.equals("EvenOddOps")) {
            requestParameters[0] = properties.getProperty("number");
        }

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("requestParameters", requestParameters);
        params.put("serviceName", properties.getProperty("serviceName"));

        String id = UUID.randomUUID().toString();

        JSONRPC2Request reqOut = new JSONRPC2Request(method, params, id);
        String jsonString = reqOut.toString();

        String url = "http://localhost:8080/generic-adapter/request";
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("jsonString", jsonString));
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        HttpResponse response = client.execute(post);

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
            System.out.println("\tid     : " + respIn.getID());
        } else {
            System.out.println("The request failed :");

            JSONRPC2Error err = respIn.getError();

            System.out.println("\terror.code    : " + err.getCode());
            System.out.println("\terror.message : " + err.getMessage());
            System.out.println("\terror.data    : " + err.getData());
        }

    }
}
