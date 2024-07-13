package br.dev.fmota.todolist.tasks;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.dev.fmota.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @GetMapping
    public List<Task> getAllTasks(HttpServletRequest request) {
        Object idUser = request.getAttribute("idUser");
        List<Task> tasks = this.taskRepository.findByIdUser((UUID) idUser);
        return tasks;
    }

    @PostMapping
    public ResponseEntity<Object> createTask(@RequestBody Task task, HttpServletRequest request) {
        Object idUser = request.getAttribute("idUser");
        task.setIdUser((UUID) idUser);

        LocalDateTime currentDate = LocalDateTime.now();
        if (currentDate.isAfter(task.getStartAt()) || currentDate.isAfter(task.getEndAt())) {

            String message = "Data de inicio/término inválida.";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
        }

        if (task.getStartAt().isAfter(task.getEndAt())) {
            String message = "Data de inicio deve ser menor que a data de término";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
        }

        Task taskCreated = this.taskRepository.save(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskCreated);
    }

    @PutMapping("/{idTask}")
    public Object updateTask(@RequestBody Task task, HttpServletRequest request, @PathVariable UUID idTask) {

        var rawTask = this.taskRepository.findById(idTask).orElse(null);

        if (rawTask == null) {
            String message = "Tarefa não encontrada";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
        }

        var idUser = request.getAttribute("idUser");

        if (!rawTask.getIdUser().equals(idUser)) {
            String message = "Usuário não tem permissão para alterar essa tarefa";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
        }

        Utils.copyNonNullProperties(task, rawTask);

        var taskUpdate = this.taskRepository.save(rawTask);
        
        return ResponseEntity.ok().body(taskUpdate);
    }
}