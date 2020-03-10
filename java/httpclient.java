import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class Main {

    public static void main(String[] args) throws NoSuchAlgorithmException, URISyntaxException, IOException {
        String license = "L01234567890123P";
        String secret = "ABCDEFGHIJKLMNOP";
        String ts = String.valueOf(System.currentTimeMillis() / 1000);
        String uri = "https://api.ttproxy.com/v1/obtain";

        // Step 1 : Obtain proxy IP
        // Important: the ip addresses in the obtained ip:port list belong to TTProxy central server, NOT the proxy node ip which finally communicate with the target server.
    
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update((license + ts + secret).getBytes());
        String sign = new BigInteger(1, md5.digest()).toString(16);

        CloseableHttpClient httpclient = HttpClients.createDefault();

        URIBuilder builder = new URIBuilder(uri);
        builder
                .setParameter("license", license)
                .setParameter("time", ts)
                .setParameter("sign", sign)
                .setParameter("cnt", "1");

        HttpGet httpGet = new HttpGet(builder.build());

        CloseableHttpResponse response = httpclient.execute(httpGet);

        HttpEntity httpEntity = response.getEntity();
        ProxyResult proxyResult;
        try {
            Reader reader = new InputStreamReader(httpEntity.getContent());
            proxyResult = new Gson().fromJson(reader, ProxyResult.class);
        } finally {
            response.close();
        }

        if ( proxyResult == null || proxyResult.getCode() != 0 ) {
            System.out.println("no proxies");
            return;
        }

        // Step 2 : Use proxy IP
        HttpHost myip = new HttpHost("myip.ipip.net", 80, "http");
        testProxy(proxyResult.getProxies().get(0), myip);
    }

    private static void testProxy(String proxyAddr, HttpHost target) throws IOException {
        testProxy(proxyAddr, target, "/");
    }

    private static void testProxy(String proxyAddr, HttpHost target, String path) throws IOException {
        String[] parts = proxyAddr.split(":");

        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpHost proxy = new HttpHost(parts[0], Integer.parseInt(parts[1]), "http");
            RequestConfig config = RequestConfig.custom()
                    .setProxy(proxy)
                    .build();

            HttpGet request = new HttpGet(path);
            request.setConfig(config);

            System.out.println("Executing request " + request.getRequestLine() + " to " + target + " via " + proxy);
            CloseableHttpResponse response = httpclient.execute(target, request);
            try {
                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                System.out.println(EntityUtils.toString(response.getEntity()));
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }
}

class ProxyResult {
    private int code;
    private String error;
    private List proxies;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public List getProxies() {
        return proxies;
    }

    public void setProxies(List proxies) {
        this.proxies = proxies;
    }
}
