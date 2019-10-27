package client;

public class StandardPacketContent {
    private String command;

    public StandardPacketContent(String data) {
        this.command = data;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
