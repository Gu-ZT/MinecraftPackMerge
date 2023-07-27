package dev.dubhe.mpm.exceptions;

public class VersionSyntaxErrorException extends RuntimeException {
    public VersionSyntaxErrorException(String version) {
        super("Version %s is not a valid version syntax".formatted(version));
    }
}
