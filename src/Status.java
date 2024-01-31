enum Status {
    NEW,
    IN_PROGRESS,
    DONE;
    // "сравнить" два статуса, исходя из весов статусов 1 0 2
    static Status interesting(Status arg1, Status arg2) {
        if ((arg1 == DONE)||(arg2 == DONE)) {
            return DONE;
        }
        if ((arg1 == NEW)||(arg2 == NEW)) {
            return NEW;
        }
        return IN_PROGRESS;
    }
}
