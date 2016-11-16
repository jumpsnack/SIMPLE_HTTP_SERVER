package App;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import jdk.nashorn.internal.parser.JSONParser;

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
                responseHeaders.set("Content-Type", "text/json");

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
                    System.out.println(data + " :: " + parameters.get(data));
                }
                Headers responseHeader = exchange.getResponseHeaders();
                responseHeader.set("Content_Type", "text/json");

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
                    dataString += parameters.get(key);

                    if (cnt++ != keys.size()) {
                        dataString += "&";
                    }
                }
                return dataString;
            }

            return "none";
        }

        private void parseQuery(String query, Map parameters) {

            if (query != null) {
                String pairs[] = query.split("[&]");

                for (String pair : pairs) {
                    String param[] = pair.split("[=]");

                    String key = null;
                    String value = null;

                    try {
                        if (param.length > 0) {
                            key = URLDecoder.decode(param[0], System.getProperty("file.encoding"));
                        }

                        if (param.length > 1) {
                            value = URLDecoder.decode(param[1], System.getProperty("file.encoding"));
                        }

                        if (key.equalsIgnoreCase("cmd")) {
                            if (value.equalsIgnoreCase("clear")) {
                                parameters.clear();
                            } else if (value.equalsIgnoreCase("exit")) {
                                System.exit(1);
                            }
                            return;
                        }

                        parameters.put(key, value);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

        private void parseJsonQuery(String jsonQuery, Map parameters) {
            if (jsonQuery != null) {

            }
        }
    }

}