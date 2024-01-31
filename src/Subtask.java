import java.util.ArrayList;

// implements Cloneable чтобы через склонированный список нельзя было добраться до оригинального Epic и его
// оригинальных подзадач и менять мир
class Subtask extends Task implements Cloneable {
    Epic parent;

    @Override
    protected Object clone() {
        Subtask newSub = (Subtask) super.clone();
        // Осторожно! Ссылка parent на оригинальный объект недопустима!
        // При клонировании, Epic должен пройти по своим подзадачам и проставить им ссылки parent
        newSub.parent = null;
        return newSub;
    }
}
