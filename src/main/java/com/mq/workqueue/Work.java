package com.mq.workqueue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

/**
 * <p>Description:</p>
 *
 * @author xinjian.wu
 * @date 2017-06-19
 */
public class Work {

    //队列名称
    private final static String QUEUE_NAME = "workqueue";

    public static void main(String[] argv) throws java.io.IOException,
            java.lang.InterruptedException {
        //区分不同工作进程的输出
        int hashCode = Work.class.hashCode();
        //创建连接和频道
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        //声明队列
        boolean durable = true;
        channel.queueDeclare(QUEUE_NAME, durable, false, false, null);
        //设置最大服务转发消息数量
        int prefetchCount = 1;
        channel.basicQos(prefetchCount);
        System.out.println(hashCode
                + " [*] Waiting for messages. To exit press CTRL+C");
        QueueingConsumer consumer = new QueueingConsumer(channel);
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

    }

    /**
     * 每个点耗时1s
     *
     * @param task
     * @throws InterruptedException
     */
    private static void doWork(String task) throws InterruptedException {
        for (char ch : task.toCharArray()) {
            if (ch == '.')
                Thread.sleep(1000);
        }
    }
}
