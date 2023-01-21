package Messages;
// Represents a hello message to be sent to the server. Fields are not used in code, but are used for serialization.
public class HelloMessage {
    private final String type = "hello";
    private String northeastern_username = "pollack.n";

    public HelloMessage(String northeastern_username) {
        this.northeastern_username = northeastern_username;
    }
}
