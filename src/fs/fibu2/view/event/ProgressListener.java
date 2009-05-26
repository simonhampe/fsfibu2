package fs.fibu2.view.event;

import javax.swing.SwingWorker;

/**
 * A ProgressListener is notified of the beginning, the progress and the end of a SwingWorker task.
 * @author Simon Hampe
 *
 */
public interface ProgressListener<T,V> {

	/**
	 * Indicates that a task has just started
	 */
	public void taskBegins(SwingWorker<T,V> source);
	
	/**
	 * Indicates that the progress property of the task has changed
	 */
	public void progressed(SwingWorker<T, V> source);
	
	/**
	 * Indicates that a task has just finished
	 */
	public void taskFinished(SwingWorker<T, V> source); 
	
}
