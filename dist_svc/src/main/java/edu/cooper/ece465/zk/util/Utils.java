package edu.cooper.ece465.zk.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

public class Utils {
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static String getHostPortOfServer(String ipPort) {
        if (ipPort != null) {
            return ipPort;
        }
        String ip;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException("failed to fetch Ip!", e);
        }
        int port = Integer.parseInt(System.getProperty("server.port"));
        ipPort = ip.concat(":").concat(String.valueOf(port));
        return ipPort;
    }

// see https://www.baeldung.com/how-to-use-resttemplate-with-basic-authentication-in-spring#manual_auth
//    public static HttpHeaders createHeaders(String username, String password){
//        return new HttpHeaders() {{
//            String auth = username + ":" + password;
//            byte[] encodedAuth = Base64.encodeBase64(
//                    auth.getBytes(Charset.forName("US-ASCII")) );;
//            String authHeader = "Basic " + new String( encodedAuth );
//            set( "Authorization", authHeader );
//        }};
//    }

    public static String generateRandomString(int length) {
        Random random = new Random();
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        String randomString = "";
        for (int i = 0; i < length; i++) {
            randomString += characters.charAt(random.nextInt(characters.length()));
        }
        return randomString;
    }

}
