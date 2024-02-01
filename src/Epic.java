import java.util.HashMap;

// implements Cloneable чтобы через склонированный список Epic нельзя было менять оригинальные подзадачи
class Epic extends Task implements Cloneable {
    private HashMap<Integer, Subtask> subtasks = new HashMap<>();

    public Epic(String name) {
        super(name);
    }

    public Epic(String name, String description) {
        super(name, description);
    }

    protected Epic(String name, String description, Integer taskHashNumber, Status status) {
        super(name, description, taskHashNumber, status);
    }

    public void calcStatus() {
        if (subtasks.size() > 0) {
            // задача за один проход понять, что все статусы уже DONE или наоборот, скатиться в NEW
            // берём две гипотезы, оптимистическую и писсимистическую
            Status optimisticStatus = Status.DONE;
            Status pessimisticStatus = Status.NEW;
            // и корректируем их жизнью
            for (Subtask subtask : subtasks.values()) {
                if (subtask.status == Status.NEW) {
                    // понижаем оптимистичную гипотезу
                    optimisticStatus = Status.IN_PROGRESS;
                }
                if (subtask.status == Status.IN_PROGRESS) {
                    // есть подзадача IN_PROGRESS => всё, статус Задачи может быть лишь IN_PROGRESS
                    this.status = Status.IN_PROGRESS;
                    return;
                }
                if (subtask.status == Status.DONE) {
                    // повышаем писсимистическую гипотезу
                    pessimisticStatus = Status.IN_PROGRESS;
                }
                // если обе теори сошлись во мнении => статус Задачи IN_PROGRESS
                if (pessimisticStatus == optimisticStatus) {
                    this.status = Status.IN_PROGRESS;
                    return;
                }
            }
            // мы здесь => теории не сошлись на IN_PROGRESS
            // выбрать ту, что не IN_PROGRESS
            this.status = Status.interesting(optimisticStatus, pessimisticStatus);
        } else {
            // нет подзадач - статус NEW
            this.status = Status.NEW;
        }
    }

    public HashMap<Integer, Subtask> getSubtaskList() {
        // отдаём копию
        HashMap<Integer, Subtask> newSubtaskList = new HashMap<>();
        for (Subtask subtask : subtasks.values()) {
            // вот тут особенность: у клона нет указателей на parent, мы создаём SubtaskList без хозяина
            newSubtaskList.put(subtask.getTaskHashNumber(), (Subtask) subtask.clone());
        }
        return newSubtaskList;
    }

    public void addSubtask(Subtask subtask) {
        if (null != subtask) {
            // также через clone
            Subtask newSubtask = (Subtask) subtask.clone();
            newSubtask.setParent(this);
            newSubtask.setParentHashNumber(this.getTaskHashNumber());
            subtasks.put(newSubtask.getTaskHashNumber(), subtask);
            // пересчитать статус с учётом новой задачи
            calcStatus();
        }
    }

    public Subtask dellSubtask(Integer taskHashNumber) {
        Subtask result = subtasks.remove(taskHashNumber);
        // процедуру пересчёта статуса есть смысл запускать лишь если удаление состоялось
        if (null != result) {
            calcStatus();
        }
        return result;
    }

    public Subtask getSubtask(Integer taskHashNumber) {
        return this.subtasks.get(taskHashNumber);
    }

    @Override
    protected Object clone() {
        //Epic newEpic = (Epic) super.clone();
        Epic newEpic = new Epic(this.name, this.description, this.getTaskHashNumber(), this.status);
        // может как-то доверить HashMap<>.clone ?
        newEpic.subtasks = new HashMap<>();
        for (Subtask subtask : this.subtasks.values()) {
            Subtask newSub = (Subtask) subtask.clone();
            // установить ссылки в своих подзадачах
            newSub.setParent(newEpic);
            newSub.setParentHashNumber(newEpic.getTaskHashNumber());
            newEpic.subtasks.put(newSub.getTaskHashNumber(), newSub);
        }
        return newEpic;
    }

    @Override
    public String toString() {
        String res = "Epic{" + name + " ";
        for (Task task : subtasks.values()) {
            res = res.concat(task.toString());
        }
        res = res.concat(" " + status + " }\n");
        return res;
    }
}
