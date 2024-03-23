package com.onedayoffer.taskdistribution.services;

import com.onedayoffer.taskdistribution.DTO.EmployeeDTO;
import com.onedayoffer.taskdistribution.DTO.TaskDTO;
import com.onedayoffer.taskdistribution.DTO.TaskStatus;
import com.onedayoffer.taskdistribution.repositories.EmployeeRepository;
import com.onedayoffer.taskdistribution.repositories.TaskRepository;
import com.onedayoffer.taskdistribution.repositories.entities.Employee;
import com.onedayoffer.taskdistribution.repositories.entities.Task;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final TaskRepository taskRepository;
    private final ModelMapper modelMapper;

    private static final String ASC = "ASC";

    public List<EmployeeDTO> getEmployees(@Nullable String sortDirection) {
        List<Employee> employees;
        if (sortDirection == null) {
            employees = employeeRepository.findAll();
        } else {
            Sort.Direction direction = sortDirection.equals(ASC) ? Sort.Direction.ASC : Sort.Direction.DESC;
            employees = employeeRepository.findAllAndSort(Sort.by(direction, "fio"));
        }
        Type listType = new TypeToken<List<EmployeeDTO>>() {}.getType();
        return modelMapper.map(employees, listType);
    }

    @Transactional
    public EmployeeDTO getOneEmployee(Integer id) {
        Optional<Employee> employee = employeeRepository.findById(id);
        if (employee.isEmpty()) {
            throw new IllegalArgumentException(String.format("Employee with id = %d not found", id));
        }
        Type type = new TypeToken<EmployeeDTO>() {}.getType();
        return modelMapper.map(employee.get(), type);
    }

    public List<TaskDTO> getTasksByEmployeeId(Integer id) {
        Optional<Employee> employee = employeeRepository.findById(id);
        if (employee.isEmpty()) {
            throw new IllegalArgumentException(String.format("Employee with id = %d not found", id));
        }
        List<Task> tasks = taskRepository.findByEmployee(employee.get());
        Type listType = new TypeToken<List<TaskDTO>>() {}.getType();
        return modelMapper.map(tasks, listType);
    }

    @Transactional
    public void changeTaskStatus(Integer taskId, TaskStatus status) {
        Optional<Task> task = taskRepository.findById(taskId);
        if (task.isEmpty()) {
            throw new IllegalArgumentException(String.format("Task with id = %d not found", taskId));
        }
        Task currentTask = task.get();
        currentTask.setStatus(status);
        taskRepository.save(currentTask);
    }

    @Transactional
    public void postNewTask(Integer employeeId, TaskDTO newTask) {
        Optional<Employee> employee = employeeRepository.findById(employeeId);
        if (employee.isEmpty()) {
            throw new IllegalArgumentException(String.format("Employee with id = %d not found", employeeId));
        }
        Type listType = new TypeToken<Task>() {}.getType();
        Task task = modelMapper.map(newTask, listType);
        task.setEmployee(employee.get());
        taskRepository.save(task);
    }
}
