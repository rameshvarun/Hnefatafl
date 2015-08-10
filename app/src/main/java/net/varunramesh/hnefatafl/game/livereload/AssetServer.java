package net.varunramesh.hnefatafl.game.livereload;

import android.support.v4.util.Pair;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Varun on 8/9/2015.
 */
public class AssetServer extends NanoHTTPD implements AssetManager {
    private static final String TAG = "AssetServer";

    private final Queue<Pair<Texture, FileHandle>> messageQueue = new ConcurrentLinkedQueue<>();

    private final HashMap<String, Texture> textures = new HashMap<>();

    public AssetServer() {
        super(8080);
    }

    public String getURL() {
        try {
            String ip = Utils.getIPAddress(true);
            return "http://" + ip + ":" + getListeningPort() + "/";
        } catch (SocketException e) {
            e.printStackTrace();
            return "¯\\_(ツ)_/¯";
        }
    }

    @Override
    public synchronized Texture getTexture(String textureFile) {
        String normalized = null;
        try {
            normalized = new URI(textureFile).normalize().getPath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            normalized = textureFile;
        }

        if(!textures.containsKey(normalized)) {
            textures.put(normalized, new Texture(normalized));
        }
        return textures.get(normalized);
    }

    @Override
    public void update() {
        // Execute queued actions.

        while(messageQueue.size() > 0) {
            Pair<Texture, FileHandle> message = messageQueue.remove();
            message.first.load(TextureData.Factory.loadFromFile(message.second, null, false));
        }
    }

    @Override
    public Response serve(IHTTPSession session) {
        Log.d(TAG, "Method: " + session.getMethod() + " URL: " + session.getUri());

        if(session.getUri().equals("/")) {
            String msg = "Textures:\n";
            for(Map.Entry<String, Texture> entry : textures.entrySet()) {
                msg += entry.getKey() + "\n";
            }
            return newFixedLengthResponse(msg);
        }

        if(session.getMethod().equals(Method.GET)) {
            try {
                String file = session.getUri().substring(1);
                Texture texture = getTexture(file);
                texture.getTextureData().prepare();
                final Pixmap pixmap = texture.getTextureData().consumePixmap();

                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                Utils.writePNG(out, pixmap);

                final ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
                return newChunkedResponse(NanoHTTPD.Response.Status.OK, "image/png", in);
            } catch (Exception e) {
                e.printStackTrace();
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                return newFixedLengthResponse(sw.toString());
            }
        }

        if(session.getMethod().equals(Method.PUT)) {
            UUID id = UUID.randomUUID();

            try {
                String file = session.getUri().substring(1);
                Texture texture = getTexture(file);

                FileHandle tempFile = session.parseBodyRaw();

                messageQueue.add(new Pair<>(texture, tempFile));
                return newFixedLengthResponse("Success.");
            } catch (Exception e) {
                e.printStackTrace();
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", sw.toString());
            }
        }

        return super.serve(session);
    }
}
