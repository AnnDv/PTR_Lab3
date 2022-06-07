# PTR Lab 3 - Message Broker

## Technologies:
- Scala
- Akka library

## Actors Role
- Multiplier - receive all messages and distribute them to Connections
- Connection - responsible for sending messages to the  Listener (Subscriber)
- Queue - filters messages by topic and sends them one by one to Connection
- TCP Manager - responsible for mantaining connection between server and client
- Listener - receives and acknowledges subscribed topics
## Diagrams
### Supervisor Tree
![supervisor-tree](/docs/images/SupervisorTree.png)

### General Message Flow
![general-MF](/docs/images/GeneralMessageFlow.png)

### Message ACK
![message-ack](/docs/images/MessageFlow(ACK).png)