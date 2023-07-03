# Java Tool using JMeter API for testing Coherence Cluster that using Rest Proxy

## What can this tool do ?
This tool contains 3 functionalies:
1. Generate Json Data.
2. Execute Put Test using JMeter API to Coherence Cluster.
3. Execute Get Test using JMeter API to Coherence Cluster.

## Requirements
- JRE/JDK1.8

## How to generate json data?
```java -cp CoherenceJMeter.jar --count=100 --size=100KB --start-id=1```


## How to execute Put Test to Coherence Cluster?
```java -cp CoherenceJMeter.jar org.example.PutTest --jmeterHome=<Your_Local_JMeter_Installation_Directory> --threads=10 --hosts=192.0.0.1,192.0.0.2 --port=8080```


## How to execute Gut Test to Coherence Cluster?
```java -cp CoherenceJMeter.jar org.example.GetTest --jmeterHome=<Your_Local_JMeter_Installation_Directory> --threads=10 --hosts=192.0.0.1,192.0.0.2 --port=8080```

