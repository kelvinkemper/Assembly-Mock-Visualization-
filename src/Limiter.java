
/**
 * This class is used to sync the silos after they've been paused.
 */
public class Limiter {

    /**
     * This method forces the thread entering to wait.
     */
    public synchronized void pause() throws InterruptedException {
        try {
            wait();
        } catch (InterruptedException ex) {
            throw new InterruptedException();
        }
    }

    /**
     * This method notifies the waiting threads.
     */
    public synchronized void letSilosRun() {
        notifyAll();
    }
}
