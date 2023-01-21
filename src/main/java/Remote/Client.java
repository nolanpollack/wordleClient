package Remote;

import GameMech.GuessFactory;
import GameMech.WordMark;
import Messages.GuessMessage;
import Messages.HelloMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;

import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;

// A client for a wordle game.
public class Client {
    private final String host;
    private final int port;
    private Socket socket;
    private JsonStreamParser input;
    private Writer output;
    private String gameID;
    private GuessFactory guessFactory;
    String username;

    public static void main(String[] args) {
        boolean encryptedConnection = false;
        int port = 27993;
        String hostname;
        String username;
        boolean customPort = false;

        // Parse command line arguments
        if (args.length < 2 || args.length > 5) {
            throw new IllegalArgumentException("Illegal arguments");
        } else if (args.length > 2) {
            for (int i = 0; i < args.length - 2; i++) {
                if (args[i].equals("-s")) {
                    encryptedConnection = true;
                } else if (args[i].equals("-p")) {
                    port = Integer.parseInt(args[i + 1]);
                    customPort = true;
                }
            }
        }
        hostname = args[args.length - 2];
        username = args[args.length - 1];
        if (!customPort) {
            if (encryptedConnection) {
                port = 27994;
            }
        }

        Client client = new Client(port, hostname, username);
        client.connect(encryptedConnection);
    }

    /**
     * Creates a new client.
     * @param port port that the client will connect to.
     * @param hostname hostname that the client will connect to.
     * @param username username that the client will use. This is the northeastern username.
     */
    public Client(int port, String hostname, String username) {
        this.port = port;
        this.host = hostname;
        this.username = username;
    }

    /**
     * Connects to the server.
     * @param encryptedConnection whether to use an encrypted connection.
     */
    private void connect(boolean encryptedConnection) {
        try {
            if (encryptedConnection) {
                connectEncrypted();
            } else {
                connectUnencrypted();
            }
            this.input = new JsonStreamParser(new BufferedReader(new InputStreamReader(socket.getInputStream())));
            this.output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            setup();
            runClient();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Connects to the server using an encrypted TLS connection.
     */
    private void connectEncrypted() {
        try {
            SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            while (socket == null || !socket.isConnected()) {
                try {
                    socket = sslSocketFactory.createSocket(host, port);
                } catch (IOException e) {
                    System.out.println("Connection failed, retrying in 5 seconds");
                    Thread.sleep(5000);
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Connects to the server using an unencrypted connection.
     */
    private void connectUnencrypted() {
        try {
            socket = new Socket(host, port);
            while (!socket.isConnected()) {
                socket = new Socket(host, port);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets up the client by sending a hello message to the server, and processing the response start message.
     */
    private void setup() {
        try {
            sendInfoToServer(new Gson().toJson(new HelloMessage(username)));
            JsonObject startMessage = this.input.next().getAsJsonObject();
            gameID = startMessage.get("id").getAsString();
            guessFactory = new GuessFactory();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Runs the client, sending guesses to the server and processing the responses.
     */
    private void runClient() {
        sendGuess();
        while (!socket.isClosed() && this.input.hasNext()) {
            JsonObject incomingMessage = this.input.next().getAsJsonObject();
            receiveResponse(incomingMessage);
        }
    }

    /**
     * Sends a guess to the server.
     */
    private void sendGuess() {
        String guess = guessFactory.createGuess();
        GuessMessage guessMessage = new GuessMessage(gameID, guess);
        try {
            sendInfoToServer(new Gson().toJson(guessMessage));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Handles a json response from the server. If the response is retry, will communicate the marks to the guess factory,
     * and send a new guess. If bye, it will close the socket.
     *
     * @param json response as a json object.
     */
    private void receiveResponse(JsonObject json) {
        Gson gson = new Gson();
        String type = json.get("type").getAsString();
        switch (type) {
            case "retry":
                WordMark[] marks = gson.fromJson(json.get("guesses").getAsJsonArray(), WordMark[].class);
                guessFactory.ReceiveMarks(marks);
                sendGuess();
                break;
            case "bye":
                closeSocket();
                System.out.println(json.get("flag").getAsString());
                break;
            case "error":
                throw new IllegalArgumentException(json.get("message").getAsString());
        }
    }

    /**
     * Writes the given info to the output stream and flush the stream.
     *
     * @param info the information to be written.
     * @throws IOException if an I/O error occurs.
     */
    private void sendInfoToServer(String info) throws IOException {
        info = info + "\n";
        this.output.write(info);
        this.output.flush();
    }

    /**
     * Closes the socket.
     */
    private void closeSocket() {
        try {
            this.socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
