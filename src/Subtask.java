import java.util.HashMap;

// implements Cloneable чтобы через склонированный список нельзя было добраться до оригинального Epic и его
// оригинальных подзадач и менять мир
class Subtask extends Task implements Cloneable {
    // приватный указатель (известен только внутри TaskManager)
    private Epic parent;
    // открытый указатель (используется для навигации вне TaskManager)
    private Integer parentHashNumber;

    public Subtask(String name) {
        super(name);
    }
    public Subtask(String name, String description) {
        super(name, description);
    }
    public Subtask(String name, String description, Status status) {
        super(name, description, status);
    }
    protected Subtask(String name, String description, Integer taskHashNumber, Status status) {
        super(name, description, taskHashNumber, status);
    }

    public void setParent(Epic parent) {
        this.parent = parent;
    }

    public Integer getParentHashNumber() {
        return parentHashNumber;
    }

    public void setParentHashNumber(Integer parentHashNumber) {
        this.parentHashNumber = parentHashNumber;
    }

    @Override
    protected Object clone() {
        //Subtask newSub = (Subtask) this.clone();
        Subtask newSub = new Subtask(this.name, this.description, this.getTaskHashNumber(), this.status);
        // Осторожно! Ссылка parent на оригинальный объект недопустима!
        // При клонировании, Epic должен пройти по своим подзадачам и проставить им ссылки parent
        newSub.parent = null;
        // открытый указатель также не проставляем, это не всегда нужно
        newSub.parentHashNumber = null;
        return newSub;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "parentHashNumber=" + parentHashNumber +
                ", taskHashNumber=" + getTaskHashNumber() +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", " + status +
                " } ";
    }
}
