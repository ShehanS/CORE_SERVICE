package filters;

import play.mvc.EssentialAction;
import play.mvc.EssentialFilter;

import javax.inject.Inject;
import java.util.concurrent.Executor;

public class AppFilter extends EssentialFilter {
    private final Executor exec;

    /**
     * @param exec This class is needed to execute code asynchronously.
     */
    @Inject
    public AppFilter(Executor exec) {
        this.exec = exec;
    }

    @Override
    public EssentialAction apply(EssentialAction next) {
        return EssentialAction.of(request ->
                next.apply(request).map(result ->
                        result.withHeader("X-ExampleFilter", "foo"), exec)
        );
    }
}
