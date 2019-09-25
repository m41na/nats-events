package works.hop.natty.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Events {

    public static final String EVENTS_CREATE = "events.create";
    public static final String EVENTS_ACKNOWLEDGE = "events.acknowledge";
    public static final String CREATE_TASK_EVENT = "CREATE_TASK";
    public static final String TASK_CREATED_EVENT = "TASK_CREATED";
    public static final String TOGGLE_TASK_EVENT = "TOGGLE_TASK";
    public static final String TASK_TOGGLED_EVENT = "TASK_TOGGLED";
    public static final String DELETE_TASK_EVENT = "DELETE_TASK";
    public static final String TASK_DELETED_EVENT = "TASK_DELETED";
    public static final String LIST_TASKS_EVENT = "LIST_TASKS";
    public static final String TASKS_LIST_EVENT = "TASKS_LIST";
    
    public static class EventEntity<T> {
        
        public String type;
        public T entity;

        @JsonCreator
        public EventEntity(@JsonProperty("type") String type, @JsonProperty("entity") T entity) {
            this.type = type;
            this.entity = entity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EventEntity)) return false;
            EventEntity<?> that = (EventEntity<?>) o;
            return type.equals(that.type) &&
                    Objects.equals(entity, that.entity);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, entity);
        }

        @Override
        public String toString() {
            return "EventEntity{" +
                    "type='" + type + '\'' +
                    ", entity=" + entity + '\'' +
                    '}';
        }
    }
    
    public static class EventEntityBuilder<T> {

        private String type;
        private T entity;

        private EventEntityBuilder(){}

        public static EventEntityBuilder newEventEntity(){
            return new EventEntityBuilder();
        }

        public EventEntityBuilder type(String type){
            this.type = type;
            return this;
        }

        public EventEntityBuilder entity(T entity){
            this.entity = entity;
            return this;
        }

        public EventEntity build(){
            return new EventEntity(type, entity);
        }
    }

    public static class Task{

        public final String task;
        public final Boolean completed;

        @JsonCreator
        public Task(@JsonProperty("name") String task, @JsonProperty("completed") Boolean completed) {
            this.task = task;
            this.completed = completed;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Task)) return false;
            Task task = (Task) o;
            return this.task.equals(task.task) &&
                    completed.equals(task.completed);
        }

        @Override
        public int hashCode() {
            return Objects.hash(task, completed);
        }

        @Override
        public String toString() {
            return "Task{" +
                    "name='" + task + '\'' +
                    ", completed=" + completed +
                    '}';
        }
    }

    public static class TaskBuilder{

        private String task;
        private Boolean completed = Boolean.FALSE;

        private TaskBuilder(){}

        public static TaskBuilder newTask(){
            return new TaskBuilder();
        }

        public TaskBuilder task(String task){
            this.task = task;
            return this;
        }

        public TaskBuilder completed(Boolean completed){
            this.completed = completed;
            return this;
        }

        public Task build(){
            return new Task(task, completed);
        }
    }

    public static class EventEntityResult<T>{

        public final Integer status;
        public final T data;
        public final Map<String, String> errors;

        public EventEntityResult(Integer status, T data, Map<String, String> errors) {
            this.status = status;
            this.data = data;
            this.errors = errors;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EventEntityResult)) return false;
            EventEntityResult<?> that = (EventEntityResult<?>) o;
            return status.equals(that.status) &&
                    data.equals(that.data);
        }

        @Override
        public int hashCode() {
            return Objects.hash(status, data);
        }

        @Override
        public String toString() {
            return "EventEntityResult{" +
                    "status=" + status +
                    ", data=" + data +
                    ", errors=" + errors +
                    '}';
        }
    }

    public static class CreateTask {

        public final String task;
        public final Boolean completed;

        @JsonCreator
        public CreateTask(@JsonProperty("task") String task) {
            this.task = task;
            this.completed = Boolean.FALSE;
        }
    }

    public static class TaskCreated {

        public final Task task;

        @JsonCreator
        public TaskCreated(@JsonProperty("task") Task task) {
            this.task = task;
        }
    }

    public static class ToggleTask {

        public final String task;

        @JsonCreator
        public ToggleTask(@JsonProperty("task") String task) {
            this.task = task;
        }
    }

    public static class TaskToggled {

        public final Task task;

        @JsonCreator
        public TaskToggled(@JsonProperty("task") Task task) {
            this.task = task;
        }
    }

    public static class DeleteTask {

        public final String task;

        @JsonCreator
        public DeleteTask(@JsonProperty("task") String task) {
            this.task = task;
        }
    }

    public static class TaskDeleted {

        public final String task;

        @JsonCreator
        public TaskDeleted(@JsonProperty("task") String task) {
            this.task = task;
        }
    }

    public static class ListTasks {

        public final Integer limit;
        public final Integer offset;

        @JsonCreator
        public ListTasks(@JsonProperty("limit") Integer limit, @JsonProperty("offset") Integer offset) {
            this.limit = limit;
            this.offset = offset;
        }
    }

    public static class TasksList {

        public final List<Task> tasks;

        @JsonCreator
        public TasksList(@JsonProperty("tasks") List<Task> tasks) {
            this.tasks = tasks;
        }
    }
}
