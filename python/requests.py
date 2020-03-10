import hashlib
import requests
import time
import json

if __name__ == "__main__":
    secret = 'ABCDEFGHIJKLMNOP'
    params = {
        "license": "L01234567890123P",
        "time": int(time.time()),
        "cnt": 10,
    }
    params["sign"] = hashlib.md5((params["license"] + str(params["time"]) + secret).encode('utf-8')).hexdigest()
    try:

        # Step 1 : Obtain proxy IP
        # {% trans %}Important: the ip addresses in the obtained ip:port list belong to TTProxy central server, NOT the proxy node ip which finally communicate with the target server.{% endtrans %}   
        response = requests.get(
            url="https://api.ttproxy.com/v1/obtain",
            params=params,
            headers={
                "Content-Type": "text/plain; charset=utf-8",
            },
            data="1"
        )
        print('Response HTTP Status Code: {status_code}'.format(
        status_code=response.status_code))
        print('Response HTTP Response Body: {content}'.format(
        content=response.content))

        # Step 2 : Use proxy IP
        res = json.loads(response.content)
        targrtUrl = "https://httpbin.org/get"

        proxies = {
            "http"  : "http://" + res.data.proxies[0],
            "https" : "https://" + res.data.proxies[0],
        }

        response = requests.get(
            url=targrtUrl,
            proxies=proxies
        )

        print('Response HTTP Status Code: {status_code}'.format(
        status_code=response.status_code))
        print('Response HTTP Response Body: {content}'.format(
        content=response.content))

    except requests.exceptions.RequestException:
        print('HTTP Request failed')
