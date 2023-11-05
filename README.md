# dispatch application


## run

```
mvn clean install
mvn spring-boot:run
```


## Produce

```
cd $HOME/.local/kafka_2.12-3.6.0
bin/kafka-console-producer.sh --topic order.created --bootstrap-server localhost:9092
```

`{"orderId": "5f33d22e-586d-4bcf-b8c9-02e9d0aea300", "item": "item-1"}`

- e0fa7290-92d9-4fda-a00a-b35c5fdb5867
- cb7c7bee-a7fe-463a-9402-64c176aa61b1
- adec6441-f9ab-426d-917e-455f13b0a389