package br.com.taskmanager.task.repository;

import br.com.taskmanager.task.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    /** Tarefas de um dia: pendentes primeiro e, em cada grupo, na ordem de criação. */
    List<Task> findByScheduledDateOrderByCompletedAscIdAsc(LocalDate scheduledDate);

    /** Quantidade total e de pendentes por dia, apenas para os dias que possuem tarefas. */
    @Query("""
            select new br.com.taskmanager.task.repository.DailyTaskCount(
                       t.scheduledDate,
                       count(t),
                       sum(case when t.completed = false then 1L else 0L end))
              from Task t
             where t.scheduledDate between :start and :end
             group by t.scheduledDate
             order by t.scheduledDate
            """)
    List<DailyTaskCount> countByDayBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
