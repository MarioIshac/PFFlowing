package me.theeninja.pfflowing.gui.cardparser;

public enum Host {
    FILE_SYSTEM("File System"),
    GOOGLE_DRIVE("Google Drive"),
    ONE_DRIVE("One Drive");

    public static final Host DEFAULT_HOST = Host.GOOGLE_DRIVE;

    private final String representation;

    Host(String representation) {
        this.representation = representation;
    }

    public String getRepresentation() {
        return representation;
    }

    public static Host getHost(String representation) {
        for (Host host : Host.values()) {
            if (host.getRepresentation().equals(representation))
                return host;
        }

        throw new IllegalArgumentException("No host with representation '" + representation + "'");
    }
}
