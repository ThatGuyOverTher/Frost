package frost.messages;



/**
 * This class holds the basic variables of a message.
 * It is used by the MessageObjectFile (XML file representation)
 * and the FrostMessageObject (database representation).
 */
public abstract class AbstractMessageObject extends AbstractMessageStatusProvider {

    protected AttachmentList attachments = null;
    protected String content = null;
    private String subject = "";
    private String recipientName = ""; // set if msg was encrypted
    private String signature = ""; // set if message is signed
    private String messageId = null;
    private String inReplyTo = null;
    private int idLinePos = -1;
    private int idLineLen = -1;

    public String getContent() {
        return content;
    }
    public String getRecipientName() {
        return recipientName;
    }
    public String getSignature() {
        return signature;
    }
    public String getSubject() {
        return subject;
    }
    public String getMessageId() {
        return messageId;
    }
    public String getInReplyTo() {
        return inReplyTo;
    }

    public void setContent(String content) {
        this.content = content;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }
    public void setRecipientName(String rec) {
        recipientName = rec;
    }
    public void setSignature(String sig) {
        signature = sig;
    }
    public void setInReplyTo(String s) {
        this.inReplyTo = s;
    }
    public void setMessageId(String s) {
        this.messageId = s;
    }

    /**
     * This method adds a new Attachment to the attachments list.
     * @param attachment the new Attachment to add to the attachments list.
     */
    public void addAttachment(Attachment attachment) {
        getAttachmentList().add(attachment);
    }

    /**
     * This method returns the AttachmentList. If no one exists, it creates a new one.
     * @return the AttachmentList
     */
    public AttachmentList getAttachmentList() {
        if (attachments == null) {
            attachments = new AttachmentList();
        }
        return attachments;
    }

    public void setAttachmentList(AttachmentList al) {
        this.attachments = al;
    }

    /**
     * This method returns an AttachmentList containing all of the
     * attachments of the given type. The type can be one of those:
     *  Attachment.FILE
     *  Attachment.BOARD
     *  Attachment.PERSON (currently unused)
     * @param type the type of attachments to return in the AttachmentList
     * @return an AttachmentList containing all of the attachments of the given type.
     */
    public AttachmentList getAttachmentsOfType(int type) {
        if (attachments == null) {
            attachments = new AttachmentList();
        }
        return attachments.getAllOfType(type);
    }

    public boolean containsAttachments() {
        if ((getAttachmentsOfType(Attachment.BOARD).size() > 0) ||
            (getAttachmentsOfType(Attachment.FILE).size() > 0)) {
            return true;
        }
        return false;
    }
    
    public int getIdLineLen() {
        return idLineLen;
    }
    public void setIdLineLen(int idLineLen) {
        this.idLineLen = idLineLen;
    }

    public int getIdLinePos() {
        return idLinePos;
    }
    public void setIdLinePos(int idLinePos) {
        this.idLinePos = idLinePos;
    }
}
