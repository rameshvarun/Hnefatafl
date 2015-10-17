package net.varunramesh.hnefatafl;

import net.varunramesh.hnefatafl.simulator.GameType;

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

    public static final int GAMETYPE_PLAYER_VS_AI = 0;
    public static final int GAMETYPE_PASS_AND_PLAY = 1;
    public static final int GAMETYPE_ONLINE_MATCH = 2;

    @Index
    private int gameType;
    public int getGameType() { return gameType; }
    public void setGameType(int gameType) { this.gameType = gameType;};
    public void setGameType(GameType gameType) {
        if(gameType instanceof GameType.PassAndPlay) {
            this.gameType = GAMETYPE_PASS_AND_PLAY;
        } else if (gameType instanceof GameType.PlayerVsAI) {
            this.gameType = GAMETYPE_PLAYER_VS_AI;
        } else if (gameType instanceof GameType.OnlineMatch){
            this.gameType = GAMETYPE_ONLINE_MATCH;
        } else {
            throw new UnsupportedOperationException("Tried to store unsupported game type in database.");
        }
    };

    private byte[] data;
    public byte[] getData() { return data; }
    public void setData(byte[] data) { this.data = data; }
}
