import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    // Взял ArrayList, а не HashMap, поскольку
    // - хотел оставить taskHashNumber неотъемлемо внутри Task
    // - список дел по определению не настолько велик, чтобы поиск перебором составлял проблему
    private ArrayList<Task> taskList = new ArrayList<>();
    private HashMap<Integer, Task> taskHashMap = new HashMap<Integer, Task>();
    public ArrayList<Task> getTaskList() {
        // не хочу отдавать сам список, отдадим его копию, чтобы не вмешались в оригинал
        ArrayList<Task> res = (ArrayList<Task>) taskList.clone();
        return res;
    }

    public void delAllTasks() {
        taskList.clear();
    }

    public Task getTask(int taskHashNumber) {
        for (Task task : taskList) {
            if (task.taskHashNumber == taskHashNumber) {
                // отдаём копию
                return (Task) task.clone();
            }
        }
        return null;
    }

    // d. Создание. Сам объект должен передаваться в качестве параметра.
    public void addTask(Task task) {
        // добавим в список копию полученной задачи, чтобы у пользователя не оставалось на неё ссылки для
        // несанкционированного доступа
        Task newTask = (Task) task.clone();
        taskList.add(newTask);
    }

    public void addTask(Epic epic) {
        Epic newTask = (Epic) epic.clone();
        taskList.add(newTask);
    }
    // такую функцию можно вызывать только изнутри: ссылка на Epic снаружи не известна
    private void addSubTask(Epic epic, Subtask subtask) {
        epic.addSubtask(subtask);
    }

    //  e. Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
    public void updateTask(Task task, int newTaskHashNumber) {
        // не понял что это. У переданной задачи обновить TaskHashNumber ?!
    }

    // f. Удаление по идентификатору.
    public void delTask(int taskHashNumber) {
        for (Task task : taskList) {
            if (task.taskHashNumber == taskHashNumber) {
                taskList.remove(task);
                return;
            }
        }
    }
}
