import java.util.HashMap;

public class TaskManager {
    // HashMap класса TaskManager будет хранить только два типа объектов Task и Epic
    // Свои подзадачи каждый Epic хранит (ссылается на HashMap объектов Subtask) самостоятельно
    private HashMap<Integer, Task> taskList = new HashMap<>();

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
                return (Task) epic.getSubtask(taskHashNumber);
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
            taskList.put(newTask.taskHashNumber, newTask);
        }
    }
    // функции-посредники, чтобы была возможность добавить только объекты двух типов
    public void addTask(Task task) {
        addTask((Object) task);
    }
    public void addTask(Epic epic) {
        addTask((Object) epic);
    }
    private void addSubTask(Integer epicIdent, Subtask subtask) {
        Task task = taskList.get(epicIdent);
        if ((task != null)&&(task.getClass() == Epic.class)) { // о, рефлексией запахло
            Epic epic = (Epic) task;
            epic.addSubtask(subtask);
        }
    }
    // функцию можно вызывать только изнутри: ссылка на epic снаружи не известна
    private void addSubTask(Epic epic, Subtask subtask) {
        epic.addSubtask(subtask);
    }

    //  e. Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
    public void updateTask(Task task) {
        // как не впихнуть новый объект, а заменить существующий?
        if (task != null) {
            // впрямую можно обновить только элементы this.taskList
            if ((task.getClass() == Task.class)||(task.getClass() == Epic.class)) {
                addTask(task);
            }
            // если нам дали подзадачу, нужно найти её родителя. Пока не ясно как
        }
    }

    // f. Удаление по идентификатору.
    public void delTask(int taskHashNumber) {
        // просто удалить Task и Epic
        taskList.remove(taskHashNumber);
        // а вот Subtask надо ещё найти
    }

    // Дополнительные методы:
    // a. Получение списка всех подзадач определённого эпика.


}
