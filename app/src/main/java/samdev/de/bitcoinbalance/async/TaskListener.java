package samdev.de.bitcoinbalance.async;

public interface TaskListener {
    void onTaskStarted();

    void onTaskFinished();
}