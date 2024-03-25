package eu.su.mas.dedaleEtu.mas.knowledge;
import java.io.Serializable;

public class DynamicObjectInfo implements Serializable {
    private static final long serialVersionUID = -7973495559646849940L;

    public enum Type{agent, golem}
    private String id;
    private Type type; // "agent" or "golem"
    private Long editTime;
    private String position; // Assuming position is represented as a String

    public DynamicObjectInfo(String id, Type type, Long editTime, String position) {
        this.id = id;
        this.type = type;
        this.editTime = editTime;
        this.position = position;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public long getEditTime() {
        return editTime;
    }

    public void setEditTime(long editTime) {
        this.editTime = editTime;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}