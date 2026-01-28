package com.jefersondev.tasks.mappers;

import com.jefersondev.tasks.domain.entities.Task;
import com.jefersondev.tasks.domain.entities.dto.TaskDto;

public interface TaskMapper {
    Task fromDto(TaskDto taskDto);

    TaskDto toDto(Task task);
}
