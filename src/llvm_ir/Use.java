package llvm_ir;

public class Use {
    private User user;

    public Use(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
