# Caching proxy

##How to run
To run this, go to the project folder and execute command: sh run.sh. Then the program starts working. 
Java 8 or later needs to be installed.

##How it works
ProxyServerEmulator class emulates some http server. It has the queue of incoming http requests INCOMING_REQUESTS_QUEUE. When some http request comes, it is put to this queue.
Then, SERVER_HANDLER_POOL threadpool takes requests from INCOMING_REQUESTS_QUEUE and handles them. Data to be sent to the main server is put to SENDER's incoming queue.
Then, SENDER's thread in SEND_DATA_EXECUTOR takes the next data batch and tries to send it to the server. If it fails, timeout happens and then the sending is executed next time. 
If error appears again, timeout is increased to predefined value. It may be increased until the maximum timeout is reached. If the next data batch sending is successful, timeout is reset to the initial value

The program "sends" up to 50000 "requests" to the proxy emulator and then the proxy "handles" these "requests". It throws exception with some probability so that we can see how 
proxy could handle errors. 