package frost.fileTransfer.download;

import frost.fileTransfer.search.FrostSearchItem;
import frost.gui.objects.Board;
import frost.util.model.ModelItem;

public class FrostDownloadItem extends ModelItem {

	// the constants representing field IDs

	public final static int FIELD_ID_DONE_BLOCKS = 100;
	public final static int FIELD_ID_ENABLED = 101;
	public final static int FIELD_ID_FILE_AGE = 102;
	public final static int FIELD_ID_FILE_NAME = 103; 
	public final static int FIELD_ID_FILE_SIZE = 104;
	public final static int FIELD_ID_KEY = 105;
	public final static int FIELD_ID_OWNER = 106;
	public final static int FIELD_ID_REQUIRED_BLOCKS = 107;
	public final static int FIELD_ID_RETRIES = 108;
	public final static int FIELD_ID_SHA1 = 109;
	public final static int FIELD_ID_STATE = 110;
	public final static int FIELD_ID_SOURCE_BOARD = 111;
	public final static int FIELD_ID_TOTAL_BLOCKS = 112;
	

	// the constants representing download states
	public final static int STATE_WAITING = 1;
	public final static int STATE_TRYING = 2;
	public final static int STATE_DONE = 3;
	public final static int STATE_FAILED = 4;
	public final static int STATE_REQUESTING = 5;
	public final static int STATE_PROGRESS = 6; // download runs
	public final static int STATE_REQUESTED = 7;
	public final static int STATE_DECODING = 8; // decoding runs

	// the fields
	private String fileName = null;		//FIELD_ID_FILE_NAME
	private Long fileSize = null;			//FIELD_ID_FILE_SIZE
	private String fileAge = null;		//FIELD_ID_FILE_AGE
	private String key = null;			//FIELD_ID_KEY
	private Board sourceBoard;	//FIELD_ID_SOURCE_BOARD
	private int retries;					//FIELD_ID_RETRIES
	private Boolean enableDownload = 
				new Boolean(true);			//FIELD_ID_ENABLED
	private String owner = null;			//FIELD_ID_OWNER
	private String sha1 = null;			//FIELD_ID_SHA1
	private int state = 0;				//FIELD_ID_STATE
	private int doneBlocks = 0;			//FIELD_ID_DONE_BLOCKS
	private int requiredBlocks = 0;		//FIELD_ID_REQUIRED_BLOCKS
	private int totalBlocks = 0;			//FIELD_ID_TOTAL_BLOCKS

	private String batch = null;

	private String redirect;

	private long lastDownloadStopTimeMillis = 0;
	
	/**
	 * @param searchItem
	 */
	// time when download try finished, used for pause between tries

	public FrostDownloadItem(FrostSearchItem searchItem) {
		fileName = searchItem.getFilename();
		fileSize = searchItem.getSize();
		fileAge = searchItem.getDate(); 
		key = searchItem.getKey();
		owner = searchItem.getOwner();
		sourceBoard = searchItem.getBoard();
		sha1 = searchItem.getSHA1();
		batch = searchItem.getBatch();
		retries = 0;

		state = STATE_WAITING;

		redirect = searchItem.getRedirect();

	}

	//TODO: add .redirect to this or fix it to use SharedFileObject
	public FrostDownloadItem(String fileName, String key, Board board) {
		
		this.fileName = fileName;
		fileSize = null; // not set yet
		fileAge = null;
		this.key = key;
		sourceBoard = board;
		retries = 0;

		state = STATE_WAITING;
	}

	public FrostDownloadItem(
		String fileName,
		String fileSize,
		String fileAge,
		String key,
		String tries,
		String from,
		String SHA1,
		int state,
		boolean isDownloadEnabled,
		Board board) {
		this.fileName = fileName;
		if (fileSize != null)
			this.fileSize = new Long(fileSize);

		if (tries != null)
			retries = Integer.parseInt(tries);
		else
			retries = 0;

		this.fileAge = fileAge;
		this.key = key;
		this.sourceBoard = board;
		this.state = state;
		this.sha1 = SHA1;
		this.enableDownload = Boolean.valueOf(isDownloadEnabled);
		owner = from;
	}

	public String getFileName() {
		return fileName;
	}
	public Long getFileSize() {
		return fileSize;
	}
	/**
	 * @param newFileSize
	 */
	public void setFileSize(Long newFileSize) {
		Long oldFileSize = fileSize;
		fileSize = newFileSize;
		fireFieldChange(FIELD_ID_FILE_SIZE, oldFileSize, newFileSize);		
	}

	public String getFileAge() {
		return fileAge;
	}
	/**
	 * @param newFileAge
	 */
	public void setFileAge(String newFileAge) {
		String oldFileAge = fileAge;
		fileAge = newFileAge;
		fireFieldChange(FIELD_ID_FILE_AGE, oldFileAge, newFileAge);
	}
	public String getKey() {
		return key;
	}
	/**
	 * @param newKey
	 */
	public void setKey(String newKey) {
		String oldKey = key;
		key = newKey;
		fireFieldChange(FIELD_ID_KEY, oldKey, newKey);
	}
	public Board getSourceBoard() {
		return sourceBoard;
	}

	public int getState() {
		return state;
	}
	/**
	 * @param newState
	 */
	public void setState(int newState) {
		int oldState = state;
		state = newState; 
		fireFieldChange(FIELD_ID_STATE, oldState, newState);
	}

	public long getLastDownloadStopTimeMillis() {
		return lastDownloadStopTimeMillis;
	}
	public void setLastDownloadStopTimeMillis(long val) {
		lastDownloadStopTimeMillis = val;
	}

	public int getRetries() {
		return retries;
	}
	/**
	 * @param newRetries
	 */
	public void setRetries(int newRetries) {
		int oldRetries = retries;
		retries = newRetries;
		fireFieldChange(FIELD_ID_RETRIES, oldRetries, newRetries);
	}
	public String getBatch() {
		return batch;
	}
	public void setBatch(String batch) {
		this.batch = batch;
	}

	public Boolean getEnableDownload() {
		return enableDownload;
	}
	/**
	 * @param enabled new enable status of the item. If null, the current 
	 * 		  status is inverted
	 */
	public void setEnableDownload(Boolean newEnabled) {
		if (newEnabled == null && enableDownload != null) {
			//Invert the enable status
			boolean enable = enableDownload.booleanValue();
			newEnabled = new Boolean(!enable);
		}
		Boolean oldEnabled = enableDownload;
		enableDownload = newEnabled;
		fireFieldChange(FIELD_ID_ENABLED, oldEnabled, newEnabled);
	}
	public String getOwner() {
		return owner;
	}

	/**
	 * @param newOwner
	 */
	public void setOwner(String newOwner) {
		String oldOwner = owner;
		owner = newOwner;
		fireFieldChange(FIELD_ID_OWNER, oldOwner, newOwner);
	}

	public String getSHA1() {
		return sha1;
	}

	/**
	 * @param newSha1
	 */
	public void setSHA1(String newSha1) {
		String oldSha1 = sha1;
		sha1 = newSha1;
		fireFieldChange(FIELD_ID_SHA1, oldSha1, newSha1);
	}

	/**
	 * @param newFileName
	 */
	public void setFileName(String newFileName) {
		String oldFileName = fileName;
		fileName = newFileName;
		fireFieldChange(FIELD_ID_FILE_NAME, oldFileName, newFileName);
	}

	/**
	 * @return Returns the redirect.
	 */
	public String getRedirect() {
		return redirect;
	}

	/**
	 * @param redirect The redirect to set.
	 */
	public void setRedirect(String redirect) {
		this.redirect = redirect;
	}

	/**
	 * @return
	 */
	public int getDoneBlocks() {
		return doneBlocks;
	}

	/**
	 * @return
	 */
	public int getRequiredBlocks() {
		return requiredBlocks;
	}

	/**
	 * @return
	 */
	public int getTotalBlocks() {
		return totalBlocks;
	}

	/**
	 * @param newDoneBlocks
	 */
	public void setDoneBlocks(int newDoneBlocks) {
		int oldDoneBlocks = doneBlocks;
		doneBlocks = newDoneBlocks;
		fireFieldChange(FIELD_ID_DONE_BLOCKS, oldDoneBlocks, newDoneBlocks);
	}

	/**
	 * @param newRequiredBlocks
	 */
	public void setRequiredBlocks(int newRequiredBlocks) {
		int oldRequiredBlocks = requiredBlocks;
		requiredBlocks = newRequiredBlocks;
		fireFieldChange(FIELD_ID_REQUIRED_BLOCKS, oldRequiredBlocks, newRequiredBlocks);
	}

	/**
	 * @param newTotalBlocks
	 */
	public void setTotalBlocks(int newTotalBlocks) {
		int oldTotalBlocks = totalBlocks; 
		totalBlocks = newTotalBlocks;
		fireFieldChange(FIELD_ID_TOTAL_BLOCKS, oldTotalBlocks, newTotalBlocks);
	}

}
