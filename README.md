# rabbit-mq-sample-receiver

## Startup

required: 
- maven 3.6
- jvm 11


`maven clean package`

example of manualAck and lagging consumer:

`java -jar target/amqp-sender-jar-with-dependencies.jar autoAck=false processDurationSeconds=5`
