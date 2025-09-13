package net.coosanta.meldmc.exceptions;

public class ClientJsonNotFoundException extends MeldException {
    public ClientJsonNotFoundException(String message) {
        super(message);
    }

    public boolean isForge() {
        return getMessage().contains("forge");
    }
}
