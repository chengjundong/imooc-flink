# Reference
[Win10+Clickhouse开发环境搭建](https://www.cnblogs.com/throwable/p/14015092.html)  
Currently, Clickhouse only can be installed in Linux/Mac OS. You need docker desktop to install it in Win10.  
If you can read Chinese, I highly recommend you to read this blog. It is very detailed.

# Steps
## 1 Install docker desktop
To install docker desktop, you might run into some issue if you are using Win 10 Home edition. Please search the answers in Google or Bing.
## 2 Pull image
[yandex/clickhouse-server](https://hub.docker.com/r/yandex/clickhouse-server)  
[yandex/clickhouse-client](https://hub.docker.com/r/yandex/clickhouse-client)  
**You need use tag: 20.x, I use 20.8.19.4**  
All steps in below are based on this tag, any other version like 21.x might fail. I have failed once when I use `latest` tag. (21.x)  
When I connect clickhouse-server by clickhouse-client, it shows an error related to time zone.  
Not sure if it is related to this PR [ClickHouse/pull/11827](https://github.com/ClickHouse/ClickHouse/pull/11827/files)
## 3 Start a temp container
```ps1
# in order to copy the auto-generated configuration files
docker run --rm -d --name=temp-clickhouse-server yandex/clickhouse-server:20.3.21.2
``` 
## 4 Copy configuration files
```ps1
# copy clickhouse config.xml & users.xml into local folders
# all local folders should be created in advance
docker cp temp-clickhouse-server:/etc/clickhouse-server/config.xml C:/jared/click-house/conf/config.xml
docker cp temp-clickhouse-server:/etc/clickhouse-server/users.xml C:/jared/click-house/conf/users.xml
```
## 5 Create user
```shell
# log into temp container
docker exec -it temp-clickhouse-server /bin/bash
# execute these two commands and get user SHA256 hash token
PASSWORD=$(base64 < /dev/urandom | head -c8); echo "default"; echo -n "default" | sha256sum | tr -d '-'
#  it might print something like 37a8eec1ce19687d132fe29051dca629d164e2c4958ba141d5f4133a33f0688f
PASSWORD=$(base64 < /dev/urandom | head -c8); echo "root"; echo -n "root" | sha256sum | tr -d '-'
#  similar as above, it might print something like 4813494d137e1631bba301d5acab6e7bb7aa74ce1185d456565ef51d737677b2
```
## 6 Change config
To change these two files in local, you can find them in the copy target folder in [step#4](#copy-configuration-files)
### user.xml
```xml
<!-- copy default's profile and create a same one for root -->
<!-- otherwise, you might run into "no profile root" while connecting clickhouse-server by using root -->
<profiles>
    <default>
        <max_memory_usage>10000000000</max_memory_usage>
        <use_uncompressed_cache>0</use_uncompressed_cache>
        <load_balancing>random</load_balancing>
    </default>
    <root>
        <max_memory_usage>10000000000</max_memory_usage>
        <use_uncompressed_cache>0</use_uncompressed_cache>
        <load_balancing>random</load_balancing>
    </root>
    <readonly>
        <readonly>1</readonly>
    </readonly>
</profiles>
<!-- remove comments at here and add another user root -->
<users>
    <default>
        <password_sha256_hex>37a8eec1ce19687d132fe29051dca629d164e2c4958ba141d5f4133a33f0688f</password_sha256_hex>
        <networks incl="networks" replace="replace">
            <ip>::/0</ip>
        </networks>
        <profile>default</profile>
        <quota>default</quota>
    </default>
    <root>
        <password_sha256_hex>4813494d137e1631bba301d5acab6e7bb7aa74ce1185d456565ef51d737677b2</password_sha256_hex>
        <networks incl="networks" replace="replace">
            <ip>::/0</ip>
        </networks>
        <profile>root</profile>
        <quota>root</quota>
    </root>
</users>
```
### config.xml
```xml
<!-- uncomment listen_host tag, depends on if you enable IPv6 or not -->
<!-- Listen specified host. use :: (wildcard IPv6 address), if you want to accept connections both with IPv4 and IPv6 from everywhere. -->
<!-- <listen_host>::</listen_host> -->
<!-- Same for hosts with disabled ipv6: -->
<listen_host>0.0.0.0</listen_host>
```
## 7 Start Clickhouse-Server
```ps1
# notice, it is better to use same port to mapping clickhouse port: 8123, 9000, 9009
# add three folder mapping for conf, data, log
docker run -d --name=jared-click-house -p 8123:8123 -p 9000:9000 -p 9009:9009 --ulimit nofile=262144:262144 -v C:/jared/click-house/conf/:/etc/clickhouse-server:rw -v C:/jared/click-house/data/:/var/lib/clickhouse:rw -v C:/jared/click-house/log/:/var/log/clickhouse-server:rw yandex/clickhouse-server:20.3.21.2
```

## 8 Use Clickhouse-Client to connect
```ps1
PS C:\Users\angel lu> docker run -it --rm --link jared-click-house:clickhouse-server yandex/clickhouse-client:20.3.21.2 -uroot --password root --host clickhouse-server

# if you successfully connect to clickhouse-server, you can see these information in below
ClickHouse client version 20.3.21.2 (official build).
Connecting to clickhouse-server:9000 as user root.
Connected to ClickHouse server version 20.3.21 revision 54433.

# try a simple query
89eb08c7a666 :) select 1

SELECT 1

┌─1─┐
│ 1 │
└───┘

1 rows in set. Elapsed: 0.003 sec.

89eb08c7a666 :)
```

## 9 Close Clickhouse-Server
Remember to stop Clickhouse-server in your local before closing your PC
```ps1
docker stop jared-click-house
```