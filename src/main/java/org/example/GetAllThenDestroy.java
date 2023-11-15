package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import picocli.CommandLine;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "get_all_then_destroy", mixinStandardHelpOptions = true, description = "Get all data then destroy")
public class GetAllThenDestroy implements Callable<Integer> {
    @CommandLine.Option(names = {"-host", "--host"}, defaultValue = "127.0.0.1", description = "The coherence rest proxy hosts. You can specicy multiple hosts seperated by [,] sign. eg 192.168.0.1,192.168.0.2")
    private String host;
    @CommandLine.Option(names = {"-port", "--port"}, defaultValue = "8080", description = "The port number of coherence rest proxy. Default is 8080.")
    private int port;

    public static void main(String[] args) throws IOException {
        int exitCode = new CommandLine(new GetAllThenDestroy()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        while(true){
            Thread.sleep(5000);
            getAllThenDeleteAll();
        }
    }

    private void getAllThenDeleteAll(){
        List<String> keys = getAllEntryKeys();
        for(String key:keys){
            getEntryByKey(key);
            deleteEntryByKey(key);
        }
    }


    private List<String> getAllEntryKeys(){
        String response = executeSimpleHttpRequest(String.format("http://%s:%s/cache/json/keys.json",host,port),"GET");
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<String> keys = mapper.readValue(response, new TypeReference<List<String>>(){});
            return keys;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void getEntryByKey(String key){
        executeSimpleHttpRequest(String.format("http://%s:%s/cache/json/%s.json",host,port,key),"GET");
    }

    private void deleteEntryByKey(String key){
        executeSimpleHttpRequest(String.format("http://%s:%s/cache/json/%s.json",host,port,key),"DELETE");
    }

    private static String executeSimpleHttpRequest(String urlStr,String method){
        try {
            URL url = new URL(urlStr);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(method);

            int responseCode = con.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString();
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }


}
