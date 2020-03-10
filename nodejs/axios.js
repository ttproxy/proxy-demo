const axios = require('axios');
const crypto = require('crypto');

const license = 'L01234567890123P';
const secret = 'ABCDEFGHIJKLMNOP';
const ts = Math.floor(+new Date() / 1000);

const queries = {
    license: license,
    time:    ts,
    // cnt: 1, // Get number of proxies (optional)

};

const md5Sum = crypto.createHash('md5');
md5Sum.update(license + ts + secret);

queries.sign = md5Sum.digest('hex').toLowerCase();

// Step 1 : Obtain proxy IP
// Important: the ip addresses in the obtained ip:port list belong to TTProxy central server, NOT the proxy node ip which finally communicate with the target server.

axios.get('https://api.ttproxy.com/v1/obtain', {
    params: queries,
}).then((response) => {
    console.log('Response HTTP Status Code: ', response.status);
    console.log('Response HTTP Response Body: ', response.data);

    // Step 2 : Use proxy IP
    var targetUrl = "https://httpbin.org/get";
    var proxy = response.data.data.proxies[0].split(':')
    axios.get(targetUrl, {
        proxy: {
            host:proxy[0],
            port:proxy[1]
        },
    }).then((response) => {
        console.log('Response HTTP Status Code: ', response.status);
        console.log('Response HTTP Response Body: ', response.data);

    }).catch((e) => {
        console.error('Error:', e);
    });

}).catch((e) => {
    console.error('Error:', e);
});
