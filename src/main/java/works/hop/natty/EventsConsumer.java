package works.hop.natty;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import works.hop.natty.entity.Events;
import works.hop.natty.service.TodoService;
import works.hop.natty.service.TodoServiceImpl;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static works.hop.natty.entity.Events.*;

public class EventsConsumer implements Runnable{

    private static final Logger LOG = LoggerFactory.getLogger(EventsConsumer.class);

    private final Connection conn;
    private final ObjectMapper mapper;
    private final Dispatcher dispatcher;
    private final TodoService service;
    private final CountDownLatch stop = new CountDownLatch(1);

    public EventsConsumer(TodoService service) throws IOException, InterruptedException {
        Connection nc = null;
        try {
            nc = initConnection();
        } catch (IOException | InterruptedException e) {
            LOG.error("The NATS connection has not been initialized");
            e.printStackTrace();
        }
        this.conn = nc;
        this.mapper = new ObjectMapper();
        this.mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        //this.mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        //This will subscribe and only get called back in a background thread if a message is received
        this.dispatcher = this.initDispatcher();
        this.service = service;
    }

    private Connection initConnection() throws IOException, InterruptedException {
        Options o = new Options.Builder().server("nats://localhost:4222").maxReconnects(-1).build();
        Connection nc = Nats.connect(o);
        return nc;
    }

    private Dispatcher initDispatcher() throws InterruptedException, IOException {
        //A dispatcher can also accept individual callbacks for any given subscription.
        Dispatcher dispatcher = conn.createDispatcher((msg) -> {
        });
        dispatcher.subscribe(EVENTS_CREATE, EVENTS_ACKNOWLEDGE, this::onTaskOperation);
        return dispatcher;
    }

    private void onTaskOperation(Message message) {
        try {
            Events.EventEntity event = this.mapper.readValue(message.getData(), new TypeReference<Events.EventEntity>() {
            });
            LOG.info("received message for task operation in dispatcher's callback {}", event);
            System.out.println(event.type);
            switch (event.type) {
                case CREATE_TASK_EVENT: {
                    Events.EventEntity<CreateTask> createEvent = mapper.convertValue(event, new TypeReference<Events.EventEntity<CreateTask>>(){});
                    CreateTask createTask = createEvent.entity;
                    CompletableFuture<TaskCreated> response = service.createTask(createTask);
                    byte[] payload = mapper.writeValueAsBytes(Events.EventEntityBuilder.newEventEntity().type(TASK_CREATED_EVENT)
                            .entity(response.join()).build());
                    conn.publish(message.getReplyTo(), payload);
                    break;
                }
                case TOGGLE_TASK_EVENT: {
                    Events.EventEntity<ToggleTask> toggleEvent = mapper.convertValue(event, new TypeReference<Events.EventEntity<ToggleTask>>(){});
                    ToggleTask toggleTask = toggleEvent.entity;
                    CompletableFuture<TaskToggled> response = service.toggleTask(toggleTask);
                    response.handle((res, th) -> {
                        if(res != null) {
                            try {
                                byte[] payload = mapper.writeValueAsBytes(EventEntityBuilder.newEventEntity().type(TASK_TOGGLED_EVENT)
                                        .entity(response.join()).build());
                                conn.publish(message.getReplyTo(), payload);
                            }
                            catch(JsonProcessingException e){
                                return e;
                            }
                        }
                        return res;
                    });
                    break;
                }
                case DELETE_TASK_EVENT: {
                    Events.EventEntity<DeleteTask> deleteEvent = mapper.convertValue(event, new TypeReference<Events.EventEntity<DeleteTask>>(){});
                    DeleteTask deleteTask = deleteEvent.entity;
                    CompletableFuture<TaskDeleted> response = service.deleteTask(deleteTask);
                    response.handle((res, th) -> {
                        if(res != null) {
                            try {
                                byte[] payload = mapper.writeValueAsBytes(EventEntityBuilder.newEventEntity().type(TASK_DELETED_EVENT)
                                        .entity(res).build());
                                conn.publish(message.getReplyTo(), payload);
                            }
                            catch(JsonProcessingException e){
                                return e;
                            }
                        }
                        return res;
                    });
                    break;
                }
                case LIST_TASKS_EVENT: {
                    Events.EventEntity<ListTasks> listEvent = mapper.convertValue(event, new TypeReference<Events.EventEntity<ListTasks>>(){});
                    ListTasks listTasks = listEvent.entity;
                    CompletableFuture<TasksList> response = service.fetchTasks(listTasks);
                    byte[] payload = mapper.writeValueAsBytes(EventEntityBuilder.newEventEntity().type(TASKS_LIST_EVENT)
                            .entity(response.join()).build());
                    conn.publish(message.getReplyTo(), payload);
                }
            }
        } catch (IOException e) {
            LOG.error("problem receiving 'acknowledgement' message in dispatcher's callback");
            e.printStackTrace(System.err);
        }
    }

    public void close() throws InterruptedException, TimeoutException, ExecutionException {
        stop.countDown();
        CompletableFuture<Boolean> drained = conn.drain(Duration.ofSeconds(10));
        drained.get();
        conn.close();
    }

    @Override
    public void run() {
        try {
            stop.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String...args) throws IOException, InterruptedException, TimeoutException, ExecutionException {
        EventsConsumer consumer = new EventsConsumer(new TodoServiceImpl());
        Thread consumerThread = new Thread(consumer);
        consumerThread.start();

        System.out.println("press any key to exit\n");
        System.in.read();
        System.out.println("now exiting");
        consumer.close();
    }
}
