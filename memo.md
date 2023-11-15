# Memo

## How to setup local coherence environment
```
./startCashServerWithRestProxy.sh /Users/yanlinfeng/Oracle/Middleware/Oracle_Home/coherence

export COHERENCE_HOME=/Users/yanlinfeng/Oracle/Middleware/Oracle_Home/coherence
java -cp /Users/yanlinfeng/Oracle/Middleware/Oracle_Home/coherence/config:/Users/yanlinfeng/Oracle/Middleware/Oracle_Home/coherence/lib/coherence.jar -Dcoherence.distributed.localstorage=false com.tangosol.net.CacheFactory
```
curl 'http://127.0.0.1:8000/cache/json/entries.json?q=age%20%3E%2029' \
-H 'Accept: */*' \
-H 'Accept-Language: en,ja;q=0.9,en-US;q=0.8,zh-CN;q=0.7,zh;q=0.6' \
-H 'Connection: keep-alive' \
-H 'Cookie: csrftoken=QjeZouDqXKUbRZnl6E6RdybL4vBPyEXf' \
-H 'Referer: http://127.0.0.1:8000/application/ojet/index.html?root=page5' \
-H 'Sec-Fetch-Dest: empty' \
-H 'Sec-Fetch-Mode: cors' \
-H 'Sec-Fetch-Site: same-origin' \
-H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36' \
-H 'X-Requested-With: XMLHttpRequest' \
-H 'sec-ch-ua: "Google Chrome";v="119", "Chromium";v="119", "Not?A_Brand";v="24"' \
-H 'sec-ch-ua-mobile: ?0' \
-H 'sec-ch-ua-platform: "macOS"' \
--compressed