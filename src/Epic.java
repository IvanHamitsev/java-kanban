import java.util.ArrayList;

// implements Cloneable чтобы через склонированный список Epic нельзя было менять оригинальные подзадачи
class Epic extends Task implements Cloneable {
    private ArrayList<Subtask> subtasks = new ArrayList<>();

    public void calcStatus() {
        if (subtasks.size() > 0) {
            // берём две гипотезы, оптимистическую и писсимистическую
            Status optimisticStatus = Status.DONE;
            Status pessimisticStatus = Status.NEW;
            // и корректируем их жизнью
            for (Subtask subtask : subtasks) {
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
        }
    }
    public void addSubtask(Subtask subtask) {
        subtask.parent = this;
        this.subtasks.add(subtask);
    }

    @Override
    protected Object clone() {
        Epic newEpic = (Epic) super.clone();
        // Может доверять ArrayList<>.clone ?
        newEpic.subtasks = new ArrayList<>();
        for (Subtask subtask : this.subtasks) {
            Subtask newSub = (Subtask) subtask.clone();
            // установить ссылки в своих подзадачах
            newSub.parent = newEpic;
            newEpic.subtasks.add(newSub);
        }
        return newEpic;
    }
}
