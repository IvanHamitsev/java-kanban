package com.practicum.kanban.service;

import com.practicum.kanban.model.*;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Stream;

public class InMemoryTaskManager implements TaskManager {
    // Класс хранит только Task и Epic
    // Подзадачи каждый Epic хранит самостоятельно
    protected HashMap<Integer, Task> taskList = new HashMap<>();
    protected HashMap<Integer, Epic> epicList = new HashMap<>();

    // задачи в отсортированном виде. Работаем только с задачами с заполненным временем
    private Comparator<Task> comparator = ((a, b) -> {
        // вот беда: TreeSet вместо equals использует компаратор, придётся нагрузить его и функционалом равенства
        //if (a.getTaskId() == b.getTaskId()) {
        if (a.equals(b)) {
            return 0;
        }
        // а вот идентичность времён двух разных задач - не повод не добавлять их. Костыли в студию.
        return (int) (Timestamp.valueOf(a.getStartTime().get()).getTime() -
                Timestamp.valueOf(b.getStartTime().get()).getTime() - 1);
    });
    private TreeSet<Task> sortedTasksSet = new TreeSet<>(comparator);

    // Хэш-таблица для хранения информации занятого времени
    private HashMap<Long, Boolean> busyMap = new HashMap<>();
    protected static long STEP_TIME; // шаг времени планирования (в минутах)

    InaccurateTime inaccurateTime;
    protected HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager manager, int timeStep) {
        historyManager = manager;
        STEP_TIME = timeStep;
        inaccurateTime = new InaccurateTime(Duration.ofMinutes(STEP_TIME), ZoneId.systemDefault());
    }

    public InMemoryTaskManager(HistoryManager manager) {
        this(manager, 15);
    }

    public InMemoryTaskManager() {
        this(Managers.getDefaultHistoryManager(), 15);
    }

    public boolean toSortedTaskSet(Task task) {
        if (task.getStartTime().isPresent()) {
            boolean res = sortedTasksSet.add(task);
            return res;
        } else {
            return false;
        }
    }

    public boolean removeFromSortedTaskSet(Task task) {
        if (task.getStartTime().isPresent() && sortedTasksSet.contains(task)) {
            sortedTasksSet.remove(task);
            return true;
        } else {
            return false;
        }
    }

    public List<Task> getPrioritizedTasks() {
        return sortedTasksSet.stream().toList();
    }

    // a. Получение списка всех задач.
    public Map<Integer, Task> getTaskList() {
        // отдавать копию
        Map<Integer, Task> res = (Map) taskList.clone();
        return res;
    }

    public Map<Integer, Epic> getEpicList() {
        // отдать копию
        Map<Integer, Epic> res = (Map) epicList.clone();
        return res;
    }

    // Получение списка всех подзадач определённого эпика.
    @Override
    public Map<Integer, Subtask> getSubtaskList(int id) {
        Map<Integer, Subtask> result = null;
        Epic epic = (Epic) epicList.get(id);
        if (null != epic) {
            result = (Map) epic.getSubtasks().clone();
        }
        return result;
    }

    // Получение списка всех подзадач.
    @Override
    public Map<Integer, Subtask> getSubtaskList() {
        Map<Integer, Subtask> result = new HashMap<>();
        epicList.values().stream()
                .forEach(e -> {
                    e.getSubtasks().values().stream()
                            .forEach(sub -> {
                                result.put(sub.getTaskId(), sub);
                            });
                });
        return result;
    }

    // b. Удаление всех задач.
    @Override
    public void deleteAllTasks() {
        taskList.values().stream()
                .forEach(t -> {
                    historyManager.remove(t.getTaskId());
                    removeFromSortedTaskSet(t);
                });
        taskList.clear();
    }

    @Override
    public void deleteAllEpics() {
        epicList.values().stream()
                .forEach(e -> {
                    historyManager.remove(e.getTaskId());
                    removeFromSortedTaskSet(e);
                });
        epicList.clear();
    }

    @Override
    public void deleteSubtasks(int id) {
        Epic epic = epicList.get(id);
        if (null != epic) {
            epic.getSubtasks().values().stream()
                    .forEach(s -> {
                        if (s.getStartTime().isPresent()) {
                            freeTime(s.getStartTime().get(), s.getDuration().get());
                        }
                        historyManager.remove(s.getTaskId());
                        removeFromSortedTaskSet(s);
                    });
            epic.getSubtasks().clear();
            // обновить статус эпика
            epic.setStatus(Status.NEW);
            // времени начала и окончания эпика теперь нет
            epic.clearTime();
        }
    }

    // c. Получение по идентификатору.
    @Override
    public Task getTask(int id) {
        Task task = taskList.get(id);
        if (task != null) {
            // добавляем в историю копию объекта
            historyManager.add(task.copy());
            // и возвращаем копию объекта
            return new Task(task);
        }
        return null;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epicList.get(id);
        if (epic != null) {
            // отдаём и добавляем в историю копию объекта
            historyManager.add(epic.copy());
            return new Epic(epic);
        }
        return null;
    }

    @Override
    public Subtask getSubtask(int id) {
        Optional<Epic> optionalEpic = epicList.values().stream()
                .filter(epic -> epic.getSubtasks().get(id) == null ? false : true)
                .findAny();
        if (optionalEpic.isPresent()) {
            Subtask subtask = optionalEpic.get().getSubtasks().get(id);
            // добавляем в историю копию объекта
            historyManager.add(subtask.copy());
            return subtask.copy();
        } else {
            return null;
        }
    }

    // d. Создание. Сам объект должен передаваться в качестве параметра.
    @Override
    public int addTask(Task task) {
        // могли передать не задачу, проверить
        if ((task != null) && (task.getClass() == Task.class)) {
            //задача может быть не добавлена, если есть пересечение времени
            if ((task.getStartTime().isEmpty()) ||
                    ((task.getStartTime().isPresent()) &&
                            (ifTimeIsFree(task.getStartTime().get(), task.getDuration().get())))) {
                // добавим копию полученной задачи
                Task newTask = task.copy();
                // в случае, если задача существует - обновим её
                if (taskList.containsKey(newTask.getTaskId())) {
                    taskList.replace(newTask.getTaskId(), newTask);
                } else {
                    taskList.put(newTask.getTaskId(), newTask);
                }
                // для задач, содержащих время
                if (task.getStartTime().isPresent()) {
                    // зарезервировать время в общем учёте
                    reservTime(task.getStartTime().get(), task.getDuration().get());
                    // добавить задачу в отсортированное хранилище
                    // добавление оригинала, не коппии. Для того, чтобы при апдейте задачи изменялось и здесь
                    toSortedTaskSet(task);
                }
                return newTask.getTaskId();
            }
        }
        return -1;
    }

    @Override
    public int addEpic(Epic epic) {
        if (epic != null) {
            if ((epic.getStartTime().isEmpty()) ||
                    ((epic.getStartTime().isPresent()) &&
                            (ifTimeIsFree(epic.getStartTime().get(), epic.getDuration().get())))) {
                Epic newEpic = epic.copy();
                if (epicList.containsKey(newEpic.getTaskId())) {
                    epicList.replace(newEpic.getTaskId(), newEpic);
                } else {
                    epicList.put(newEpic.getTaskId(), newEpic);
                }
                if (epic.getStartTime().isPresent()) {
                    // эпик не занимает время в общем учёте, его займут подзадачи
                    //reservTime(epic.getStartTime().get(), epic.getDuration().get());
                    toSortedTaskSet(epic);
                }

                return newEpic.getTaskId();
            }
        }
        return -1;
    }

    @Override
    public int addSubtask(Subtask subtask) {
        if (subtask != null) {
            Epic epic = (Epic) epicList.get(subtask.getParentId());
            if (epic != null) {
                // теперь добавление подзадачи может быть неудачным, если она пересекается во времени
                if ((subtask.getStartTime().isEmpty()) ||
                        ((subtask.getStartTime().isPresent()) &&
                                (ifTimeIsFree(subtask.getStartTime().get(), subtask.getDuration().get())))) {
                    Subtask newSubtask = subtask.copy();
                    // клонирование не проставляет ссылки, проставим их
                    newSubtask.setParentId(epic.getTaskId());
                    if (epic.getSubtasks().containsKey(newSubtask.getTaskId())) {
                        epic.getSubtasks().replace(newSubtask.getTaskId(), newSubtask);
                    } else {
                        epic.getSubtasks().put(newSubtask.getTaskId(), newSubtask);
                    }
                    // обновить статус эпика
                    calcStatusAdd(epic, newSubtask.getStatus());
                    if (newSubtask.getStartTime().isPresent()) {
                        // зарезервировать время в общем учёте
                        reservTime(newSubtask.getStartTime().get(), newSubtask.getDuration().get());
                        // обновить время начала и окончания эпика
                        epic.expandingTimeUpdate(newSubtask.getStartTime().get(), newSubtask.getEndTime().get());
                        toSortedTaskSet(newSubtask);
                        // эпик получает время от подзадачи и требует добавления в sortedTaskSet
                        toSortedTaskSet(epic);
                    }
                    return newSubtask.getTaskId();
                }
            }
        }
        return -1;
    }

    //  e. Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
    @Override
    public int updateTask(Task task) {
        if (task != null) {
            // в текущей реализации это замена существующего элемента
            return addTask(task);
        }
        return -1;
    }

    @Override
    public int updateEpic(Epic epic) {
        if (epic != null) {
            return addEpic(epic);
        }
        return -1;
    }

    @Override
    public int updateSubtask(Subtask subtask) {
        Epic epic = (Epic) epicList.get(subtask.getParentId());
        if (epic != null) {
            // пересчёты статусов внутри функции предполагают новую подзадачу
            int ret = addSubtask(subtask);
            // пересчитать статус
            calcStatus(epic);
            return ret;
        }
        return -1;
    }

    // f. Удаление по идентификатору.
    @Override
    public void deleteTask(int id) {
        Task task = getTask(id);
        if ((task != null) && (task.getStartTime().isPresent())) {
            freeTime(task.getStartTime().get(), task.getDuration().get());
        }
        taskList.remove(id);
        historyManager.remove(id);
        removeFromSortedTaskSet(task);
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epicList.get(id);
        if (null != epic) {
            epic.getSubtasks().values().stream()
                    .forEach(s -> {
                        if (s.getStartTime().isPresent()) {
                            freeTime(s.getStartTime().get(), s.getDuration().get());
                        }
                        historyManager.remove(s.getTaskId());
                    });
            historyManager.remove(id);
            epicList.remove(id);
            removeFromSortedTaskSet(epic);
        }
    }

    @Override
    public void deleteSubtask(int id) {
        epicList.values().stream()
                .filter(epic -> null != epic.getSubtasks().get(id))
                .forEach(epic -> {
                    Subtask subtask = epic.getSubtasks().get(id);
                    Status status = subtask.getStatus();
                    if (subtask.getStartTime().isPresent()) {
                        freeTime(subtask.getStartTime().get(), subtask.getDuration().get());
                        removeFromSortedTaskSet(subtask);
                        // и пересчитать время самого эпика
                        epic.reduceTimeUpdate();
                    }
                    epic.getSubtasks().remove(id);
                    historyManager.remove(id);
                    calcStatusRemove(epic, status);
                });
    }

    // Истории последних операций получения задач/эпиков/подзадач
    @Override
    public List<Task> getHistory() {
        // метод get делает копии элеметов, поэтому достаточно сделать Collections.unmodifiableList
        return Collections.unmodifiableList(historyManager.getHistory());
    }

    // Дополнительные методы:
    // пересчёт статуса с учётом изменения
    private void calcStatusAdd(Epic epic, Status status) {
        if (epic.getSubtasks().size() == 1) {
            // единственная подзадача эпика определит его статус
            epic.setStatus(status);
        } else {
            switch (epic.getStatus()) {
                case NEW:
                    if (status != Status.NEW) {
                        epic.setStatus(Status.IN_PROGRESS);
                    }
                    break;
                case DONE:
                    if (status != Status.DONE) {
                        epic.setStatus(Status.IN_PROGRESS);
                    }
            }
        }
    }

    private void calcStatusRemove(Epic epic, Status status) {
        // если подзадач не осталось статус NEW
        if (epic.getSubtasks().isEmpty()) {
            epic.setStatus(Status.NEW);
        }
        if (epic.getSubtasks().size() == 1) {
            // единственная оставшаяся подзадача эпика определит его статус
            epic.setStatus(epic.getSubtasks().values().iterator().next().getStatus());
        } else {
            // при удалении подзадачи статус может измениться, только если он был IN_PROGRESS
            if (epic.getStatus() == Status.IN_PROGRESS) {
                // и нужен полный пересчёт
                calcStatus(epic);
            }
        }
    }

    // полный пересчёт статуса
    private void calcStatus(Epic epic) {
        if (epic.getSubtasks().size() > 0) {
            // временные переменные для подсчёта итогового статуса
            Status hiStatus = Status.DONE;
            Status lowStatus = Status.NEW;
            // за один проход по всем подзадачам эпика пересчитать статус
            // (не ложится на идеологию стримов: обработка потока задач для формирования внешних переменных)
            for (Subtask subtask : epic.getSubtasks().values()) {
                if (subtask.getStatus() == Status.NEW) {
                    hiStatus = Status.IN_PROGRESS;
                }
                if (subtask.getStatus() == Status.IN_PROGRESS) {
                    // есть подзадача IN_PROGRESS - статус эпика может быть только IN_PROGRESS
                    epic.setStatus(Status.IN_PROGRESS);
                    return;
                }
                if (subtask.getStatus() == Status.DONE) {
                    lowStatus = Status.IN_PROGRESS;
                }
                if (lowStatus == hiStatus) {
                    // если обе переменные сравнялись, то только на IN_PROGRESS
                    epic.setStatus(Status.IN_PROGRESS);
                    return;
                }
            }

            if (hiStatus == Status.DONE) {
                epic.setStatus(Status.DONE);
            }
            if (lowStatus == Status.NEW) {
                epic.setStatus(Status.NEW);
            }
        } else {
            // нет подзадач - статус NEW
            epic.setStatus(Status.NEW);
        }
    }

    public void reservTime(LocalDateTime start, Duration duration) {
        if (null != start) {
            // нужно округлить вверх, но так, чтобы для целого числа не получилось +1
            long count = (long) Math.ceil(((double) duration.toMinutes() / STEP_TIME));
            // поток времени от start
            Stream<Long> timeStream = Stream.iterate(inaccurateTime.getMinutes(start), val -> val + STEP_TIME).limit(count);
            // занять все моменты
            timeStream.forEach(time -> busyMap.put(time, true));

        }
    }

    public boolean ifTimeIsFree(LocalDateTime start, Duration duration) {
        if (null != start) {
            long count = (long) Math.ceil(((double) duration.toMinutes() / STEP_TIME));
            long startMinutes = inaccurateTime.getMinutes(start);
            // поток времени от start
            Stream<Long> timeStream = Stream.iterate(startMinutes, val -> val + STEP_TIME).limit(count);
            // ни один элемент потока не соответствуют критерию "занято"
            return timeStream.noneMatch(time -> busyMap.containsKey(time));
        } else {
            // отсутствующее время - свободно
            return true;
        }
    }

    public void freeTime(LocalDateTime start, Duration duration) {
        if (null != start) {
            long count = (long) Math.ceil(((double) duration.toMinutes() / STEP_TIME));
            // поток времени от start
            Stream<Long> timeStream = Stream.iterate(inaccurateTime.getMinutes(start), val -> val + STEP_TIME).limit(count);
            // найти и удалить занятые моменты
            timeStream.filter(time -> busyMap.containsKey(time) ? busyMap.get(time) : false).forEach(time -> busyMap.remove(time));
        }
    }

    @Override
    public void clearInstance() {
        this.deleteAllEpics();
        this.deleteAllTasks();
    }

    @Override
    public String toString() {
        Stream<String> strings = Stream.concat(Stream.of(taskList.values().toString()), Stream.of(epicList.values().toString()));
        return strings.reduce("TaskManager{\n", String::concat).concat("}\n");
    }
}
