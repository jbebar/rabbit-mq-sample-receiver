package org.jbebar

import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.Delivery
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val arguments = args.map { it.split("=") }.map { Pair(it[0], it[1]) }.toMap()
    val autoAck = arguments["autoAck"].toBoolean()
    val qOS = arguments["qos"]?.toInt()
    val processDurationSeconds = arguments["processDurationSeconds"]!!.toLong()
    val countDownLatch = CountDownLatch(10)

    var processedMessageCount = AtomicInteger(0)
    val factory = ConnectionFactory()
    factory.host = "localhost"

    val connection = factory.newConnection()
    val channel = connection.createChannel().apply { qOS?.let { basicQos(it) } }

    val deliveryCallback = { _: String, message: Delivery ->
        val receivedMessage = String(message.body)
        println("Processing : $receivedMessage")
        Thread.sleep(processDurationSeconds * 1000)
        if (!autoAck) {
            println("Acknowledging message $receivedMessage")
            channel.basicAck(message.envelope.deliveryTag, false)
        }
        processedMessageCount.incrementAndGet()
        countDownLatch.countDown()
        println("Processed : $receivedMessage")
    }

    channel.basicConsume("demo-queue", autoAck, deliveryCallback, { _ -> })

    countDownLatch.await(10, TimeUnit.SECONDS)

    println("Processed ${processedMessageCount.get()} messages.")
    exitProcess(0)
}