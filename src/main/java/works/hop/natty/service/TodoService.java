package works.hop.natty.service;

import works.hop.natty.entity.Events;

import java.util.concurrent.CompletableFuture;

public interface TodoService {

    CompletableFuture<Events.TaskCreated> createTask(Events.CreateTask event) ;

    CompletableFuture<Events.TaskToggled> toggleTask(Events.ToggleTask event);

    CompletableFuture<Events.TaskDeleted> deleteTask(Events.DeleteTask event);

    CompletableFuture<Events.TasksList> fetchTasks(Events.ListTasks event);
}
