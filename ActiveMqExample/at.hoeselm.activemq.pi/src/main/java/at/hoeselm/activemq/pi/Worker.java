package at.hoeselm.activemq.pi;

import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

public class Worker implements Runnable {

	// variable definitions
	private final String messageBrokerUrl = "tcp://localhost:61616?jms.prefetchPolicy.queuePrefetch=1"; // URL
																										// of
																										// message
																										// broker
	private ActiveMQConnectionFactory connectionFactory; // factory / connection
															// handler
	private Connection connection; // connection object
	private Session session; // session object
	private Destination destinationWorker; // destination object
	private int active_for_minutes = 60; // defines how long the consumer runs
	private MessageConsumer consumer; // consumer object
	private static int worker_id = 0;

	public Worker() throws Exception {

		// every worker has its own id by implementing static counter
		++worker_id;
		System.out.println("Creating new worker with id: " + worker_id);
		
		// create a ActiveMQConnection Factory instance
		connectionFactory = new ActiveMQConnectionFactory(messageBrokerUrl);
		// create connection to the message broker
		connection = connectionFactory.createConnection();
		connection.start();
		// create a session based on the connection, with transaction handling
		// false and auto acknowledged
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		// queue definitions
		destinationWorker = session.createQueue("Queue.Work");

		// create a message consumer using the session object
		consumer = session.createConsumer(destinationWorker);
	}

	// termination method, run after execution
	protected void finalize() throws Exception {

		// close the message producer
		consumer.close();

		// close an active connection
		if (connection != null) {
			connection.close();
		}
	}

	// execution method
	public void run() {
		try {
			System.out.println("Worker started with id: " + worker_id);
			// create a listener
			consumer.setMessageListener(new WorkerListener());
			TimeUnit.MINUTES.sleep(active_for_minutes);
			connection.stop();
		} catch (Exception ex) {
			System.out.println("Exception: " + ex.getMessage());
		}
	}

}