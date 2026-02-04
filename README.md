# Bug reproducer for vertx httpclient decompression issue

1. Prepare environment:

Start the mock server:
```
$ pip3 install flask  # if not already available
$ python3 mock_server.py
```

This serves the contents of the archive data.txt.gz, which you can check with curl:
```
$  curl --compressed -i http://localhost:8889/data/matchmaker_v6
HTTP/1.1 200 OK
Server: Werkzeug/3.1.3 Python/3.9.21
Date: Wed, 04 Feb 2026 15:35:11 GMT
Cache-Control: no-cache
Last-Modified: Wed, 04 Feb 2026 15:31:18 GMT
Content-Type: text/plain; charset=utf-8
Content-Encoding: gzip
Content-Length: 404
Connection: close

d1-b1-bc-abc,b1-bc-abc.yui.fghj.net.uk,false,true,0.000,c1-b1-bc-abc,1.000,0.900,0.800,xy-abcd-efgh-uk:xy-abcd-efghb-uk:xy-hls-efgh-uk:xy-hls-efghb-uk:uv-abcd-efgh-uk:uv-abcd-efghb-uk:uv-hls-efgh-uk:uv-hls-efghb-uk:xy-uhd-efgh-uk:xy-uhd-efghb-uk:vod-hls-uk:vod-dash-uk:zxc-jkl-uk
...
```
(rest of body truncated)


2. Reproduce the bug

This launches a worker verticle with an http client that has decompression enabled, which will attempt to get from the mock server and pipe the result into a file. See the implementation of `fetch` for the relevant code.

Build and run the vertx reproducer like so:
```
$ mvn package
$ java -jar target/vertx-httpclient-gzip-issue-1.0-SNAPSHOT.jar
```

This will produce one of two outcomes - either the error for this issue:
```
$ java -jar target/vertx-httpclient-gzip-issue-1.0-SNAPSHOT.jar
start fetch
Feb 04, 2026 3:36:57 PM io.vertx.launcher.application.VertxApplication
INFO: Succeeded in deploying verticle
Streaming response to file failed
fetch failed:
io.vertx.core.http.HttpClosedException: Connection was closed
```

or successful response handling:
```
$ java -jar target/vertx-httpclient-gzip-issue-1.0-SNAPSHOT.jar
start fetch
Feb 04, 2026 3:38:02 PM io.vertx.launcher.application.VertxApplication
INFO: Succeeded in deploying verticle
fetch succeeded
```
