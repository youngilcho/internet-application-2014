package scorer.termproject.youngilcho;

import java.util.concurrent.*;

/**
 * AsyncTaskService Utility
 * User: Youngil Cho
 * Date: 13. 7. 24
 * Time: 오후 5:49
 */
public class AsyncTaskService {

    public static final int TAG_TERM_ASSO = 1;
    public static final int OTHER = 2;

    public final static AsyncTaskService get() { return singleton; }
    private final static AsyncTaskService singleton = new AsyncTaskService();

    private ExecutorService executorServices[] = new ExecutorService[20];
    private ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(4);

    private AsyncTaskService() {
    }

    public void init() {
        for(int i = 0; i < 20; i++) {
            // Login, CheckIn 패킷은 큰 Pool을 할당해준다.
            if(i == TAG_TERM_ASSO) {
                executorServices[i] = Executors.newFixedThreadPool(8);
            } else {
                executorServices[i] = Executors.newFixedThreadPool(8);
            }
        }
    }

    public void close() {
        for(int i=0; i< 20; i++) {
            executorServices[i].shutdown();
        }
    }

    public void runTask (int level, Runnable r) {
        executorServices[level].execute(r);
    }

    public void runTask(Runnable r) {
        executorServices[0].execute(r);
    }

    public int getQueueSize(int level) {
        return ((ThreadPoolExecutor) executorServices[level]).getQueue().size();
    }

    public void runSchedule(Runnable r, long delay) {
        scheduledExecutorService.schedule(r, delay, TimeUnit.MILLISECONDS);
    }
}