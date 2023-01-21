package Messages;

// Represents a message containing a guess to be sent to the server. Fields not used in code are
// used for serialization.
public class GuessMessage {
    final String type = "guess";
    final String id;
    final String word;

    public GuessMessage(String id, String word) {
        this.id = id;
        this.word = word;
    }
}
