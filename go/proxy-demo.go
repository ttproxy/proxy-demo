package main

import (
	"bytes"
	"crypto/md5"
	"encoding/hex"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"net/url"
	"strconv"
	"time"
)

func main() {

	license := "L01234567890123P"
	secret := "ABCDEFGHIJKLMNOP"
	ts := strconv.FormatInt(time.Now().Unix(), 10)

	// Step 1 : Obtain proxy IP
	// Important: the ip addresses in the obtained ip:port list belong to TTProxy central server, NOT the proxy node ip which finally communicate with the target server.

	getProxyApiUri := "https://api.ttproxy.com/v1/obtain"
	queries := url.Values{}
	queries.Set("license", license)
	queries.Set("time", ts)
	queries.Set("cnt", "1") // Get number of proxies (optional)

	signBytes := md5.Sum([]byte(license + ts + secret))
	queries.Set("sign", hex.EncodeToString(signBytes[:]))

	response, err := http.Get(getProxyApiUri + "?" + queries.Encode())
	if err != nil {
		log.Fatalln("Failure : ", err)
	}
	defer response.Body.Close()

	respBody, _ := ioutil.ReadAll(response.Body)

	result := struct {
		Id    string `json:"_id"`
		Code  int    `json:"code"`
		Error string `json:"error"`
		Data  *struct {
			TodayObtain int64    `json:"todayObtain"`
			IpLeft      int64    `json:"ipLeft"`
			TrafficLeft int64    `json:"trafficLeft"`
			Proxies     []string `json:"proxies"`
		} `json:"data"`
	}{}

	err = json.Unmarshal(respBody, &result)
	if err != nil {
		panic(err)
	}

	if result.Code != 0 {
		log.Fatalf("failed: code=%d, error=%s", result.Code, result.Error)
	}

	if result.Data == nil || len(result.Data.Proxies) == 0 {
		log.Fatalf("unknown error")
	}

	// Step 2 : Use proxy IP
	targetUrl := "https://httpbin.org/get"
	client := &http.Client{
		Transport: &http.Transport{
			Proxy: func(*http.Request) (u *url.URL, err error) {
				return url.Parse(fmt.Sprintf("http://%s", result.Data.Proxies[0]))
			},
		},
	}

	req, err := http.NewRequest(http.MethodGet, targetUrl, nil)
	if err != nil {
		panic(err)
	}

	res, err := client.Do(req)
	if err != nil {
		panic(err)
	}
	defer res.Body.Close()

	body, err := ioutil.ReadAll(res.Body)
	if err != nil {
		panic(err)
	}

	var b bytes.Buffer
	res.Header.Write(&b)
	fmt.Println("---------------------------------")
	fmt.Printf("%s %s\n", res.Proto, res.Status)
	fmt.Printf("%s\n", b.String())
	fmt.Printf("%s", body)
	fmt.Println("---------------------------------")
}
