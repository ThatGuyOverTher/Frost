package frost.threads;


public interface BoardUpdateThreadListener
{
    /**
     * Is called if a Thread is finished.
     */
    public void boardUpdateThreadFinished(BoardUpdateThread thread);

    /**
     * Is called if a Thread is started.
     */
    public void boardUpdateThreadStarted(BoardUpdateThread thread);
}