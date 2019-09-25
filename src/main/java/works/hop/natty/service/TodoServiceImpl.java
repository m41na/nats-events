package works.hop.natty.service;

import works.hop.natty.entity.Events;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class TodoServiceImpl implements TodoService {

    private List<Events.Task> repo = new LinkedList<>();

    @Override
    public CompletableFuture<Events.TaskCreated> createTask(Events.CreateTask event) {
        Events.Task newTask = new Events.Task(event.task, event.completed);
        repo.add(newTask);
        return CompletableFuture.completedFuture(new Events.TaskCreated(newTask));
    }

    @Override
    public CompletableFuture<Events.TaskToggled> toggleTask(Events.ToggleTask event) {
        AtomicInteger index = new AtomicInteger(0);
        CompletableFuture response = new CompletableFuture();
        for(Events.Task task : repo){
            int i = index.getAndIncrement();
            if (task.task.equals(event.task)) {
                Events.Task updatedTask = Events.TaskBuilder.newTask().task(task.task).completed(!task.completed).build();
                repo.set(i, updatedTask);
                response.complete(new Events.TaskToggled(updatedTask));
            }
        }
        return response;
    }

    @Override
    public CompletableFuture<Events.TaskDeleted> deleteTask(Events.DeleteTask event) {
        AtomicInteger index = new AtomicInteger(0);
        CompletableFuture response = new CompletableFuture();
        for(Events.Task task : repo){
            int i = index.getAndIncrement();
            if (task.task.equals(event.task)) {
                Events.Task deletedTask = repo.remove(i);
                response.complete(new Events.DeleteTask(deletedTask.task));
            }
        }
        return response;
    }

    @Override
    public CompletableFuture<Events.TasksList> fetchTasks(Events.ListTasks event) {
        Events.TasksList tasks = new Events.TasksList(repo);
        return CompletableFuture.completedFuture(tasks);
    }
}
