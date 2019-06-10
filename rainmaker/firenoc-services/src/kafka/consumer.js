const kafka = require("kafka-node");
import envVariables from "./envVariables";

const Consumer = kafka.Consumer;
let client = new kafka.KafkaClient({
  kafkaHost: envVariables.KAFKA_BROKER_HOST
});

const consumer = new Consumer(client, [{ topic: "SMS", offset: 0 }], {
  autoCommit: false
});

consumer.on("message", function(message) {
  console.log(message.value);
});

consumer.on("error", function(err) {
  console.log("Error:", err);
});

consumer.on("offsetOutOfRange", function(err) {
  console.log("offsetOutOfRange:", err);
});
