package App;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

/**
 * Created by Eddie Sangwon Kim on 2016-11-11.
 */
public class Server {
    public static void main(String[] args) throws IOException {
        InetSocketAddress addr = new InetSocketAddress(443);
        HttpServer server = HttpServer.create(addr, 0);

        server.createContext("/", new MyHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }


    static class MyHandler implements HttpHandler {

        private String root = "C:/ServerTest/";
        HashMap<String, HashMap> parameters = new HashMap<String, HashMap>();

        public void handle(HttpExchange exchange) throws IOException {
            String requestMethod = exchange.getRequestMethod();

            if (requestMethod.equalsIgnoreCase("GET")) {

                Headers responseHeaders = exchange.getResponseHeaders();
                responseHeaders.set("Content-Type", "text/plain");

                URI uri = exchange.getRequestURI();
                System.out.println(uri.getPath());
                OutputStream responseBody = exchange.getResponseBody();
                BufferedReader br = new BufferedReader(new StringReader(makeData(parameters)));//

                exchange.sendResponseHeaders(200, 0);

                int b = 0;
                while ((b = br.read()) != -1) {
                    responseBody.write(b);
                }
                responseBody.close();

            } else if (requestMethod.equalsIgnoreCase("POST")) {

                //Map parameters = (Map) exchange.getAttribute("parameters");


                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);

                URI uri = exchange.getRequestURI();
                parseQuery(uri.getQuery(), parameters);//

                for (String data : parameters.keySet()) {
                    HashMap<String, String> childHash = parameters.get(data);
                    String result = "";
                    for (String key : childHash.keySet()) {
                        result += key;
                        result += "/";
                        result += childHash.get(key);
                        result += " ";
                    }
                    System.out.println(data + " = " + result);
                }
                Headers responseHeader = exchange.getResponseHeaders();
                responseHeader.set("Content_Type", "text/plain");

                exchange.sendResponseHeaders(200, 0);

                OutputStream responseBody = exchange.getResponseBody();
                responseBody.write('1');
                responseBody.close();
                br.close();
                isr.close();
            }
        }

        private String makeData(Map parameters) {
            if (!parameters.isEmpty()) {
                String dataString = "";

                Set<String> keys = parameters.keySet();
                int cnt = 1;
                for (String key : keys) {
                    dataString += key;
                    dataString += "=";

                    HashMap<String, String> childHash = (HashMap<String, String>) parameters.get(key);
                    Set<String> childKeys = childHash.keySet();
                    int childCnt = 1;
                    for (String childKey : childKeys) {
                        dataString += childKey;
                        dataString += "/";
                        dataString += childHash.get(childKey);

                        if (childCnt++ != childKeys.size()) {
                            dataString += ",";
                        }
                    }

                    if (cnt++ != keys.size()) {
                        dataString += "&";
                    }
                }
                return dataString;
            }

            return "none";
        }

        private void parseQuery(String query, Map parameters) {

            if (query != null) {//1=H/10,T:20&2=H/20,T:30
                String pairs[] = query.split("[&]");//[0] 1=H/10,T/20  [1] 2=H/20,T/30

                for (String pair : pairs) {
                    String param[] = pair.split("[=]");//[0] 1 [1]H/10,T/20
                    HashMap<String, String> childHash = new HashMap<String, String>();

                    if (param[0].equalsIgnoreCase("cmd")) {
                        if (param[1].equalsIgnoreCase("clear")) {
                            parameters.clear();
                        } else if (param[1].equalsIgnoreCase("exit")) {
                            System.exit(1);
                        }
                        return;
                    }

                    for (String body : param) {
                        String mass[] = body.split("[,]");//[0] H/10 [1] T/20

                        if (mass.length > 1) {
                            for (String keys : mass) {
                                String values[] = keys.split("[/]"); // [0] H [1] 20

                                String key = null;
                                String value = null;

                                try {
                                    if (values.length > 0) {
                                        key = URLDecoder.decode(values[0], System.getProperty("file.encoding"));
                                    }

                                    if (values.length > 1) {
                                        value = URLDecoder.decode(values[1], System.getProperty("file.encoding"));
                                    }

                                    childHash.put(key, value);
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    parameters.put(param[0], childHash);
                }
            }
        }
    }

}