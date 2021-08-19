package com.yukx.spring_boot.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @Author yukx
 * @Date 2021/7/14
 **/
public class SignUtil {
    private final static String SECRET_ID = "re0ScaBXBxwHLb7OVnThahKRwBn0MrYz";
    private final static String SECRET_KEY = "WO1mTNj8KLalCx4YVxL1PeF7gXie1Uc8";
    private final static String CT_JSON = "application/json; charset=utf-8";

    public static byte[] hmac256(byte[] key, String msg) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, mac.getAlgorithm());
        mac.init(secretKeySpec);
        return mac.doFinal(msg.getBytes(StandardCharsets.UTF_8));
    }

    public static String sha256Hex(String s) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
        return DatatypeConverter.printHexBinary(d).toLowerCase();
    }

    public static void main(String[] args) throws Exception { // 此为测试域名，生产是另外的域名
        String host = "auth.test.investoday.net";
// 请求参数 2 个
        String username = "admin";
        String svcHost = "auth.test.investoday.net";
//        String svcHost = "faip.test.invstoday.net";
        final String region = "ap-shenzhen";
        final String version = "2019-11-19";
        final String algorithm = "JC1-HMAC-SHA256";
        String httpRequestMethod = "POST";
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String date =
                LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
// ************* 步骤 1:拼接规范请求串 *************
        final String canonicalUri = "/sys/login/ticket";
        String service = StringUtils.splitByWholeSeparator(canonicalUri, "/")[0];
        final String signedHeaders = "content-type;host";
        String payload = String.format("{\"Username\":\"%s\",\"SvcHost\":\"%s\"}", username, svcHost);
        String canonicalQueryString = "";
        System.out.println("payload===>" + payload);
        String hashedRequestPayload = sha256Hex(payload);
        System.out.println("hashedRequestPayload==>" + hashedRequestPayload + "\n");
        String canonicalHeaders = "content-type:" + CT_JSON + "\n" + "host:" + host + "\n";
        String canonicalRequest = httpRequestMethod + "\n" + canonicalUri + "\n"
                + canonicalQueryString + "\n" + canonicalHeaders + "\n"
                + signedHeaders + "\n" + hashedRequestPayload;
        System.out.println("canonicalRequest==>" + canonicalRequest.replace("\n", "\\n"));
        String hashedCanonicalRequest = sha256Hex(canonicalRequest);
        System.out.println("hashedCanonicalRequest==>" + hashedCanonicalRequest + "\n");
// ************* 步骤 2:拼接待签名字符串 *************
        String credentialScope = date + "/" + service + "/jc1_request";
        String stringToSign = algorithm + "\n" + timestamp + "\n" + credentialScope + "\n"
                + hashedCanonicalRequest;
        System.out.println("stringToSign==>" + stringToSign.replace("\n", "\\n"));
// ************* 步骤 3:计算签名 *************
        byte[] secretDate = hmac256(("JC1" + SECRET_KEY).getBytes(StandardCharsets.UTF_8), date);
        byte[] secretService = hmac256(secretDate, service);
        byte[] secretSigning = hmac256(secretService, "jc1_request");
        byte[] signatureBin = hmac256(secretSigning, stringToSign);
        String signature = DatatypeConverter.printHexBinary(signatureBin).toLowerCase();
        System.out.println();
        System.out.println("Timestamp==>" + timestamp);
        System.out.println("signature==>" + signature);
        System.out.println();
// ************* 步骤 4:拼接 Authorization *************
        String authorization = algorithm + " " + "Credential=" + SECRET_ID + "/" +
                credentialScope + ", "
                + "SignedHeaders=" + signedHeaders + ", " + "Signature=" + signature;
        System.out.println("authorization==>" + authorization + "\n");
        TreeMap<String, String> headers = new TreeMap<String, String>();
        headers.put("Authorization", authorization);
        headers.put("Content-Type", CT_JSON);
        headers.put("Host", host);
        headers.put("X-JC-Timestamp", timestamp);
        headers.put("X-JC-Version", version);
        headers.put("X-JC-Region", region);
        StringBuilder sb = new StringBuilder();
        sb.append("curl -X POST https://").append(host).append(canonicalUri)
                .append(" -H \"Authorization: ").append(authorization).append("\"").append(" -H \"Content-Type: " + CT_JSON + "\"")
                .append(" -H \"Host: ").append(host).append("\"")
                .append(" -H \"X-JC-Timestamp: ").append(timestamp).append("\"").append(" -H \"X-JC-Version: ").append(version).append("\"").append(" -H \"X-JC-Region: ").append(region).append("\"")
                .append(" -d '").append(payload).append("'");
        System.out.println(sb);

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost("https://"+host+"/sys/login/ticket");
        StringEntity entity = new StringEntity(payload, ContentType.APPLICATION_JSON);
        post.setEntity(entity);
        headers.forEach(post::addHeader);

        CloseableHttpResponse response = client.execute(post);
        String result = EntityUtils.toString(response.getEntity(), "utf-8");
        System.out.println(result);
        /*Map<Integer, String> map = new HashMap<>(16);
        map.put(65, "1");
        map.put(129, "2");
        map.put(257, "3");
        map.put(513, "4");
        map.put(1025, "5");
        map.put(2049, "6");
        map.put(4097, "7");
        map.put(8193, "8");
        map.put((8192<<1)+1, "9");
        System.out.println(15+"          ->          "+Integer.toBinaryString(15));

        map.forEach((k,v)->{
            System.out.println(k+"          ->          "+Integer.toBinaryString(k));
        });*/

        Map<Integer, String> map2 = new HashMap<>(16);
        map2.put(0b10001, "1");
        map2.put(0b100001, "2");
        map2.put(0b1000001, "3");
        map2.put(0b10000001, "4");
        map2.put(0b100000001, "5");
        map2.put(0b1000000001, "6");
        map2.put(0b10000000001, "7");
        map2.put(0b100000000001, "8");
        map2.put(0b1000000000001, "9");
    }
}
