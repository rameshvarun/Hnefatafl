package net.varunramesh.hnefatafl;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Varun on 8/10/2015.
 */
public class SavedGame extends RealmObject {
    @PrimaryKey
    private String id;
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @Index
    private Date createdDate;
    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }

    @Index
    private Date lastMoveDate;
    public Date getLastMoveDate() { return lastMoveDate; }
    public void setLastMoveDate(Date lastMoveDate) { this.lastMoveDate = lastMoveDate; }

    private byte[] data;
    public byte[] getData() { return data; }
    public void setData(byte[] data) { this.data = data; }
}
