package dev.dubhe.mpm.exceptions;

public class VersionsSyntaxErrorException extends RuntimeException {
    public VersionsSyntaxErrorException(String versions) {
        super("Versions %s is not a valid versions syntax".formatted(versions));
    }
}
