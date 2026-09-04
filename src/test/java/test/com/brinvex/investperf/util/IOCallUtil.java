package test.com.brinvex.investperf.util;

import java.io.IOException;
import java.io.UncheckedIOException;

public class IOCallUtil {

    public interface NoArgIOCall<O> {
        O call() throws IOException;
    }

    public static <O> O uncheckedIO(NoArgIOCall<O> ioCall) {
        try {
            return ioCall.call();
        } catch (IOException e) {
            throw new UncheckedIOException(String.format("IOException occurred: type=[%s], msg=[%s]",
                    e.getClass().getName(), e.getMessage()), e);
        }
    }
}
