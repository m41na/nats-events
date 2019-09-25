package works.hop.natty;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import works.hop.natty.entity.Events;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static works.hop.natty.entity.Events.*;

public class EventsSource implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(EventsSource.class);

    private final Connection conn;
    private final ObjectMapper mapper;
    private final Dispatcher dispatcher;

    public EventsSource() throws IOException, InterruptedException {
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
        dispatcher.subscribe(EVENTS_ACKNOWLEDGE, this::onTaskOperation);
        return dispatcher;
    }

    private void onTaskOperation(Message message) {
        try {
            Events.EventEntity event = this.mapper.readValue(message.getData(), new TypeReference<EventEntity>() {
            });
            LOG.info("received acknowledgement for task operation in dispatcher's callback {}", event);
            System.out.println(event.entity);
        } catch (IOException e) {
            LOG.error("problem receiving 'acknowledgement' message in dispatcher's callback");
            e.printStackTrace(System.err);
        }
    }

    @Override
    public void run() {
        System.out.println("enter :q to exit or use c: to create, t: to toggle, d: to delete or l: to list");
        List<String> chars = Arrays.asList("c", "t", "d", "l");
        String line = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while ((line = reader.readLine()) != null) {
                if (":q".equals(line)) {
                    System.out.println("now exiting application...");
                    break;
                }
                String[] tokens = line.split(":");
                if (tokens.length != 2) {
                    System.err.println("Invalid format%n" +
                            "Expected format is [command]:[task]%n" +
                            "for instance \"c:run a mile\", which means create a task to run a mile%n");
                    continue;
                }
                if (!chars.contains(tokens[0].trim())) {
                    System.err.printf("Invalid command%n" +
                            "Expected commands:%" +
                            "c - create%n" +
                            "t - toggle%n" +
                            "d - delete%n" +
                            "l - list");
                    continue;
                }
                if (tokens[1].length() == 0) {
                    System.err.println("Expected a name for the task to be created%n");
                    continue;
                }
                switch(tokens[0].trim()) {
                    case "c": {
                        EventEntity createTask = Events.EventEntityBuilder.newEventEntity().type(CREATE_TASK_EVENT)
                                .entity(TaskBuilder.newTask().task(tokens[1].trim())).build();
                        conn.publish(EVENTS_CREATE, EVENTS_ACKNOWLEDGE, mapper.writeValueAsBytes(createTask));
                        break;
                    }
                    case "t": {
                        EventEntity toggleTask = Events.EventEntityBuilder.newEventEntity().type(TOGGLE_TASK_EVENT)
                                .entity(new ToggleTask(tokens[1].trim())).build();
                        conn.publish(EVENTS_CREATE, EVENTS_ACKNOWLEDGE, mapper.writeValueAsBytes(toggleTask));
                        break;
                    }
                    case "d": {
                        EventEntity deleteTask = Events.EventEntityBuilder.newEventEntity().type(DELETE_TASK_EVENT)
                                .entity(new DeleteTask(tokens[1].trim())).build();
                        conn.publish(EVENTS_CREATE, EVENTS_ACKNOWLEDGE, mapper.writeValueAsBytes(deleteTask));
                        break;
                    }
                    case "l": {
                        String[] vals = tokens[1].trim().split(",");
                        EventEntity listTasks = Events.EventEntityBuilder.newEventEntity().type(LIST_TASKS_EVENT)
                                .entity(new ListTasks(Integer.parseInt(vals[0].trim()), Integer.parseInt(vals[1].trim()))).build();
                        conn.publish(EVENTS_CREATE, EVENTS_ACKNOWLEDGE, mapper.writeValueAsBytes(listTasks));
                        break;
                    }
                    default: {
                        System.err.println("unknown input. Please try again");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    public void close() throws InterruptedException, TimeoutException, ExecutionException {
        CompletableFuture<Boolean> drained = conn.drain(Duration.ofSeconds(10));
        drained.get();
        conn.close();
    }

    public static void main(String...args) throws IOException, InterruptedException, TimeoutException, ExecutionException {
        EventsSource source = new EventsSource();
        Thread sourceThread = new Thread(source);
        sourceThread.start();
        sourceThread.join();
        //close when runnable exits
        source.close();
    }
}
