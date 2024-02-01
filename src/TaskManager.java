import java.util.HashMap;

public class TaskManager {
    // HashMap класса TaskManager будет хранить только два типа объектов Task и Epic
    // Свои подзадачи каждый Epic хранит (ссылается на HashMap объектов Subtask) самостоятельно
    private HashMap<Integer, Task> taskList = new HashMap<>();

    public TaskManager() {
        // функционал конструктора
    }

    // a. Получение списка всех задач.
    public HashMap<Integer, Task> getTask() {
        // не хочу отдавать сам список, отдадим его копию, чтобы не вмешались в оригинал
        HashMap<Integer, Task> res = (HashMap<Integer, Task>) taskList.clone();
        return res;
    }

    // b. Удаление всех задач.
    public void delAllTasks() {
        taskList.clear();
    }

    // c. Получение по идентификатору.
    public Task getTask(Integer taskHashNumber) {
        // если это Task и Epic - взять из taskList
        if (taskList.get(taskHashNumber) != null) {
            // отдаём копию
            return (Task) taskList.get(taskHashNumber).clone();
        }
        // если это Subtask, его надо найти
        for (Task task : taskList.values()) {
            // для каждого Epic заглянуть в подзадачи
            if (task.getClass() == Epic.class) {
                Epic epic = (Epic) task;
                Task result = (Task) epic.getSubtask(taskHashNumber);
                if (null != result) {
                    return result;
                }
            }
        }
        return null;
    }

    // d. Создание. Сам объект должен передаваться в качестве параметра.
    // Вспомогательная функция, ибо dry
    private void addTask(Object task) {
        // добавим в список копию полученной задачи, чтобы у пользователя не оставалось на неё ссылки для
        // несанкционированного доступа
        if (task != null) {
            Task newTask = (Task) task;
            newTask = (Task) newTask.clone();
            taskList.put(newTask.getTaskHashNumber(), newTask);
        }
    }
    // функции-посредники, чтобы была возможность добавить только объекты двух типов
    public void addTask(Task task) {
        addTask((Object) task);
    }
    public void addTask(Epic epic) {
        addTask((Object) epic);
    }
    // особая логика для добавления объекта Subtask с заполненным parentHashNumber
    public void addTask(Subtask subtask) {
        Epic epic = (Epic) taskList.get(subtask.getParentHashNumber());
        if (epic != null) {
            epic.addSubtask(subtask);
        }
    }

    //  e. Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
    public void updateTask(Task task) {
        if (task != null) {
            // впрямую можно обновить только элементы this.taskList, рефлексируем
            if ((task.getClass() == Task.class)||(task.getClass() == Epic.class)) {
                // не впихнуть новый объект, а заменить существующий
                delTask(task.getTaskHashNumber());
                addTask(task);
            }
            // Если нам дали подзадачу, нужно найти её родителя
            if (task.getClass() == Subtask.class) {
                Subtask subtask = (Subtask) task;
                Epic epic = (Epic) taskList.get(subtask.getParentHashNumber());
                if (null != epic) {
                    // не добавить, а заменить
                    epic.dellSubtask(subtask.getTaskHashNumber());
                    epic.addSubtask(subtask);
                }
            }
        }
    }

    // f. Удаление по идентификатору.
    public void delTask(Integer taskHashNumber) {
        // просто и быстро удалить Task и Epic
        if (null == taskList.remove(taskHashNumber)) {
            // не найден элемент в taskList, придётся долго искать в Subtask перебором
            for (Task task : taskList.values()) {
                // для каждого Epic заглянуть в подзадачи
                if (task.getClass() == Epic.class) {
                    Epic epic = (Epic) task;
                    if (null !=  epic.dellSubtask(taskHashNumber)) {
                        // удаляем только первое вхождение, прекращаем перебор
                        return;
                    }
                }
            }
        }
    }

    // Дополнительные методы:
    // a. Получение списка всех подзадач определённого эпика.
    public HashMap<Integer, Subtask> getSubtaskList(Epic epic) {
        return getSubtaskList(epic.getTaskHashNumber());
    }
    public HashMap<Integer, Subtask> getSubtaskList(Integer epicHashNumber) {
        Epic epic = (Epic) taskList.get(epicHashNumber);
        if (null != epic) {
            return epic.getSubtaskList();
        }
        return null;
    }

    @Override
    public String toString() {
        String res = "TaskManager{\n";
        for (Task task : taskList.values()) {
            res = res.concat(task.toString());
        }
        res = res.concat("}");
        return res;
    }
}
