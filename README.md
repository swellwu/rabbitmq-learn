# rabbitmq学习
## helloworld
首先启动rabbitmq服务器，生产者发送消息，消费者接收消息，使用阻塞队列在存储和匹配，
就是普通的阻塞队列的功能。

## 工作队列
### round-robin
按照消费这数量一次行分发任务，分发出去后就不再管了，但是可能造成消费者被杀导致消息丢失。
### 消息应答
也即发送消息后，需要消费者手动应答后，生产才会认为消息已被处理，会自动将已断开连接的消费者未应答的消息分发给
其他消费者处理。
```
        // 指定消费队列,自动应答为true表示一分发消息立刻完成应答，也即不管执行情况，为false表示需要手动来应答
        boolean autoAck = false;
        channel.basicConsume(QUEUE_NAME, autoAck, consumer);
        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());
            System.out.println(hashCode + " [x] Received '" + message + "'");
            doWork(message);
            System.out.println(hashCode + " [x] Done");
            //发送应答
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        }
```
### 消息持久化
消息持久化可以在rabbitmq崩溃时将消息保存到磁盘上，不会丢失消息，待服务重启后继续处理消息。
1. 声明为持久化队列
```
        //声明队列
        boolean durable = true;
        channel.queueDeclare(QUEUE_NAME, durable, false, false, null);
```
2. 发送消息时，设定为持久化消息
```
    channel.basicPublish("", "task_queue",MessageProperties.PERSISTENT_TEXT_PLAIN,message.getBytes());
```
### 公平转发
 公平转发，设置每个消费者最大待处理消息数量。
 ```
         //设置最大服务转发消息数量
         int prefetchCount = 1;
         channel.basicQos(prefetchCount);
 ```
 ## 转发器 Exchange
 生产者只能把消息发给转发器，由转发器发给对应队列。
 转发器类型：
 1. Direct
 2. Topic
 3. Headers
 4. Fanout - 类似广播机制，一个消息发送到所有消费者
 5. "" - 匿名转发器
 