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
import java.util.Random;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "search_by_category", mixinStandardHelpOptions = true, description = "Search json by category")
public class SearchByCategory implements Callable<Integer> {
    @CommandLine.Option(names = {"-host", "--host"}, defaultValue = "127.0.0.1", description = "The coherence rest proxy hosts. You can specicy multiple hosts seperated by [,] sign. eg 192.168.0.1,192.168.0.2")
    private String host;
    @CommandLine.Option(names = {"-port", "--port"}, defaultValue = "8080", description = "The port number of coherence rest proxy. Default is 8080.")
    private int port;

    public static void main(String[] args) throws IOException {
        int exitCode = new CommandLine(new SearchByCategory()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        while(true){
            Thread.sleep(1000);
            searchEntries();
        }
    }

    private void searchEntries(){
        String category = String.valueOf(new Random().nextInt(1000)+1);
        List<String> keys = searchByCategory(category);
        if(null != keys && keys.size() > 0){
            System.out.println(String.format("Search json by category %s, retrieved  %s records, record keys are: %s",category,keys.size(),keys));
            for(String key:keys){
                getEntryByKey(key);
            }
        }
    }

    private List<String> searchByCategory(String category){
        String response = executeSimpleHttpRequest(String.format("http://%s:%s/cache/json/keys.json?q=category%20is%20\"%s\"",host,port,category),"GET");
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
