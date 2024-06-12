package si.uni_lj.fe.tnuv.taskman;

public class User {
    private final String id;
    private final String username;
    private boolean isSelected;

    public User(String id, String username) {
        this.id = id;
        this.username = username;
        this.isSelected = false;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}

