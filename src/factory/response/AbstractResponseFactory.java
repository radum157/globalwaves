package factory.response;

import factory.Factory;
import lombok.Getter;

/**
 * ResponseFactory factory (abstract factory).
 */
public final class AbstractResponseFactory implements Factory<ResponseFactory> {
    @Getter
    private static AbstractResponseFactory instance;

    static {
        synchronized (AbstractResponseFactory.class) {
            instance = new AbstractResponseFactory();
        }
    }

    private AbstractResponseFactory() {
    }

    /**
     * Generates a response factory or subclass, depending on the command type
     */
    @Override
    public ResponseFactory createObject(final Object[] data) {
        String commandType = (String) data[0];

        /* Watch out for future commands not respecting this */
        if (commandType.startsWith("show") || commandType.startsWith("get")
                || commandType.equals("seeMerch")) {
            return new InfoResponseFactory();
        }

        if (commandType.equals("wrapped")) {
            return new StatResponseFactory();
        }

        return switch (commandType) {
            case "search" -> new SearchResponseFactory();
            case "status" -> new StatusResponseFactory();
            default -> new ResponseFactory();
        };
    }
}
