package org.example.vvpd.task.model;

public enum TaskPriority {
    LOW(1),
    MEDIUM(2),
    HIGH(3);

    private final int weight;

    TaskPriority(int weight) {
        this.weight = weight;
    }

    public int weight() {
        return weight;
    }
}
