package com.jefersondev.tasks.services;

import com.jefersondev.tasks.domain.entities.TaskList;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskListService {
    List<TaskList>  listTaskList();
    TaskList creatTaskList(TaskList taskList);
    Optional<TaskList> getTaksList(UUID id);
    TaskList updateTaskList(UUID taskListId, TaskList taskList);
    void deleteTaskList(UUID taskLisId);

}
