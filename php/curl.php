<?php
$license = 'L01234567890123P';
$secret = 'ABCDEFGHIJKLMNOP';
$time = time();

// Step 1 : Obtain proxy IP
// Important: the ip addresses in the obtained ip:port list belong to TTProxy central server, NOT the proxy node ip which finally communicate with the target server.
$queries = [
    'license' => $license,
    'time' => $time,
    // 'cnt' => 1, // Get number of proxies (optional)

];

$queries['sign'] = md5($license . $time . $secret);

$getProxyApiurl = 'https://api.ttproxy.com/v1/obtain?' . http_build_query($queries);

$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, $getProxyApiurl);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
$response = curl_exec($ch);

if (!$response) {
  die('Error: "' . curl_error($ch) . '" - Code: ' . curl_errno($ch));
}

echo 'HTTP Status Code: ' . curl_getinfo($ch, CURLINFO_HTTP_CODE) . PHP_EOL;
echo 'Response Body: ' . $response . PHP_EOL;

curl_close($ch);

$resData = json_decode($response, true);

// Step 2 : Use proxy IP
$targetUrl = "https://httpbin.org/get";

$proxyServer = $resData['data']['proxies'][0];
$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, $targetUrl);

curl_setopt($ch, CURLOPT_HTTPPROXYTUNNEL, false);
curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);

curl_setopt($ch, CURLOPT_PROXYTYPE, CURLPROXY_HTTP);
curl_setopt($ch, CURLOPT_PROXY, $proxyServer);
curl_setopt($ch, CURLOPT_USERAGENT, "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727;)");

curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, 3);
curl_setopt($ch, CURLOPT_TIMEOUT, 5);

curl_setopt($ch, CURLOPT_HEADER, true);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

$result = curl_exec($ch);
curl_close($ch);

var_dump($result);
