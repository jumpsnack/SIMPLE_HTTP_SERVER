package App;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
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
        //HashMap<String, HashMap> parameters = new HashMap<String, HashMap>();
        HashMap<String, HashMap> parentParameters = new HashMap<String, HashMap>();

        public void handle(HttpExchange exchange) throws IOException {
            String requestMethod = exchange.getRequestMethod();

            if (requestMethod.equalsIgnoreCase("GET")) {

                Headers responseHeaders = exchange.getResponseHeaders();
                responseHeaders.set("Content-Type", "text/plain");

                URI uri = exchange.getRequestURI();
                System.out.println(uri.getPath());
                OutputStream responseBody = exchange.getResponseBody();
                BufferedReader br = new BufferedReader(new StringReader(makeData(parentParameters)));//

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
                parseQuery(uri.getQuery(), parentParameters);//

                //    for (String data : parameters.keySet()) {
                //       System.out.println(data + " :: " + parameters.get(data));
                //   }
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
                int parentCnt = 1;
                String dataString = "{";
                Set<String> parentKeys = parameters.keySet();
                for (String parentKey : parentKeys) {
                    HashMap<String, String> childParameters = (HashMap<String, String>) parameters.get(parentKey);
                    if (!childParameters.isEmpty()) {
                        int childCnt = 1;
                        dataString += parentKey + " : {";
                        Set<String> childKeys = childParameters.keySet();

                        for (String childKey : childKeys) {
                            dataString += childKey;
                            dataString += ":";
                            dataString += childParameters.get(childKey);

                            if (childCnt++ != childKeys.size()) {
                                dataString += ", ";
                            } else {
                                dataString += "}";
                            }
                        }
                    }

                    if (parentCnt++ != parentKeys.size()) {
                        dataString += ", ";
                    }

                }
                dataString += "}";
                return dataString;
            }

            return "none";
        }

        private void parseQuery(String query, Map parameters) {
            /*******************************
             *
             * 여기에서 JSON 파싱해서 저장하는거 만들어야함
             *
             * */
            try {
                if(query != null){
                    String pairs[] = query.split("[=]");

                    JSONObject parentBody = (JSONObject) JSONValue.parse(pairs[1]);
                    Set<String> parentKeys = parentBody.keySet();

                    for (String parentKey : parentKeys) {
                        JSONObject chileBody = (JSONObject) parentBody.get(parentKey);
                        HashMap<String, String> childHash = new HashMap<String, String>();

                        if (chileBody != null) {
                            //String pairs[] = query.split("[&]");
                            Set<String> childKeys = chileBody.keySet();
                            // for (String pair : childKeys) {
                            for (String childKey : childKeys) {
                                //String param[] = pair.split("[=]");

                                String key = null;
                                String value = null;


//                            if (param.length > 0) {
                                //                              key = URLDecoder.decode(param[0], System.getProperty("file.encoding"));
                                //                        }
//
                                //                          if (param.length > 1) {
                                //                            value = URLDecoder.decode(param[1], System.getProperty("file.encoding"));
                                //                      }

                                key = childKey;
                                value = (String) chileBody.get(key);

                                if (key.equalsIgnoreCase("cmd")) {
                                    if (value.equalsIgnoreCase("clear")) {
                                        parameters.clear();
                                    } else if (value.equalsIgnoreCase("exit")) {
                                        System.exit(1);
                                    }
                                    return;
                                }

                                childHash.put(key, value);
                            }
                        }
                        parameters.put(parentKey, childHash);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void parseJsonQuery(String jsonQuery, Map parameters) {
            if (jsonQuery != null) {

            }
        }
    }

}