package scorer.termproject;

import java.util.concurrent.*;

/**
 * AsyncTaskService Utility
 * User: Youngil Cho
 * Date: 13. 7. 24
 * Time: 오후 5:49
 */

public class AsyncTaskService {

    public static final int HIGH_PERFORMANCE = 0;
    public static final int OTHER = 1;

    public final static AsyncTaskService get() { return singleton; }
    private final static AsyncTaskService singleton = new AsyncTaskService();

    private ExecutorService executorServices[] = new ExecutorService[2];
    private ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(4);

    private AsyncTaskService() {
    }

    public void init() {
        for(int i = 0; i < 2; i++) {
            // 속도 증가를 위해 총 6개의 스레드를 쓴다.(이 정도가 안전함. 랩탑 따위 저사양에서도 돌리려면)
            if(i == HIGH_PERFORMANCE) {
                executorServices[i] = Executors.newFixedThreadPool(6);
            } else {
                executorServices[i] = Executors.newFixedThreadPool(4);
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