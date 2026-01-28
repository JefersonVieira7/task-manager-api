package com.jefersondev.tasks.mappers;

import com.jefersondev.tasks.domain.entities.TaskList;
import com.jefersondev.tasks.domain.entities.dto.TaskListDto;

public interface TaskListMapper {
    TaskList fromDto(TaskListDto taskListDto);

    TaskListDto toDto(TaskList taskList);
}
