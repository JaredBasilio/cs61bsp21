package gitlet;

import java.io.Serializable;
import java.util.Date;
import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/** Represents a gitlet commit object.
 *
 *  @author Jared Basilio
 */
public class Commit implements Serializable {
    /** The message of this Commit. */
    private String message;

    /** The timestamp for this Commit. */
    private String timeStamp;

    /** The universal UID for the commit. */
    private String UID;

    /** the parents of the commit */
    private ArrayList<String> parents;

    /** the tracked files of the commit*/
    private HashMap<String, String> files;

    /** the merge condition for the print statement */
    private boolean merged;

    /** the standard format for log and status*/
    private SimpleDateFormat format =
            new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z");

    /** Makes the Initial Commit. */
    public Commit(String message) {
        this.message = message;
        this.timeStamp = format.format(new Date(0));
        this.files = new HashMap<>();
        parents = new ArrayList<>();
    }

    /**
     * returns the message of the commit
     * @return message
     */
    public String message() {
        return message;
    }
    /**
     * returns the timestamp of the commit
     * @return timestamp
     */
    public String timeStamp() {
        return timeStamp;
    }

    /**
     * returns the parents of the commit
     * @return parents
     */
    public ArrayList<String> getParents() {
        return parents;
    }

    /**
     * returns the ID of the commit
     * @return uid
     */
    public String uid() {
        return UID;
    }

    /**
     * sets the uid of the commit to the given ID
     * @param uid
     */
    public void giveUID(String uid) {
        this.UID = uid;
    }

    /**
     * resets the parents list and add the given parent
     * @param parent
     */
    public void addParentForCommit(String parent) {
        parents = new ArrayList<>();
        parents.add(parent);
    }

    /**
     * adds the given parent to the parent list
     * @param parent
     */
    public void addParent(String parent) {
        parents.add(parent);
    }

    /**
     * sets the message to the given message
     * @param message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * adds the given file with the UID to be tracked
     * @param name
     * @param blobUID
     */
    public void setData(String name, String blobUID) {
        files.put(name, blobUID);
    }

    /**
     * untracks a file from this commit
     * @param name
     */
    public void removeData(String name) {
        files.remove(name);
    }

    /**
     * returns the files tracked
     * @return files
     */
    public HashMap<String, String> getFiles() {
        return files;
    }

    /**
     * sets the timestamp of the commit to the current time in the format
     */
    public void setTimestampCurrent() {
        this.timeStamp = format.format(new Date());
    }

    /**
     * saves the commit into the commits folder
     */
    public void saveCommit() {
        File outFile = Utils.join(Repository.COMMITS, UID);
        Utils.writeObject(outFile, this);
    }

    /**
     * gets the blob ID of the name provided in the given commit
     * @param name
     * @return file or null
     */
    public String getFile(String name) {
        if (files.containsKey(name)) {
            return files.get(name);
        }
        return null;
    }

    /**
     * tracks multiple files
     * @param data
     */
    public void setDataMulti(Map<String, String> data) {
        for (Map.Entry<String, String> entry : data.entrySet()) {
            setData(entry.getKey(), entry.getValue());
        }
    }

    /**
     * untracks multiple files
     * @param data
     */
    public void removeDataMulti(Map<String, String> data) {
        for (Map.Entry<String, String> entry : data.entrySet()) {
            removeData(entry.getKey());
        }
    }

    /**
     * returns true if the file is a merged commit
     * @return boolean merged
     */
    public boolean isMerged() {
        return merged;
    }

    /**
     * sets the commit to be a merged commit
     */
    public void setMerged() {
        merged = true;
    }
}
