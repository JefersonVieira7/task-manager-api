package com.jefersondev.tasks.repositories;
import com.jefersondev.tasks.domain.entities.Task;
import com.jefersondev.tasks.domain.entities.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    Page<Task> findByTaskListId(UUID taskListId, Pageable pageable);
    @Query("""
        SELECT t FROM Task t
        WHERE t.taskList.id = :taskListId
        AND (:status IS NULL OR t.status = :status)
        AND (:title IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', CAST(:title AS string), '%')))
        """)
    Page<Task> findByTaskListIdWithFilters(
            @Param("taskListId") UUID taskListId,
            @Param("status") TaskStatus status,
            @Param("title") String title,
            Pageable pageable
    );
    Optional<Task> findByTaskListIdAndId(UUID taskListId, UUID id);
    void deleteByTaskListIdAndId(UUID taskListId, UUID id);
}