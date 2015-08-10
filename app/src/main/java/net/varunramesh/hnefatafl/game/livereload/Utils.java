package net.varunramesh.hnefatafl.game.livereload;

import android.graphics.Bitmap;

import com.badlogic.gdx.graphics.Pixmap;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

/**
 * Created by Varun on 8/9/2015.
 */
public class Utils {
    /**
     * From http://stackoverflow.com/a/13007325
     * Get IP address from first non-localhost interface
     * @param ipv4  true=return ipv4, false=return ipv6
     * @return  address or empty string
     */
    public static String getIPAddress(boolean useIPv4) throws SocketException {
        List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
        for (NetworkInterface intf : interfaces) {
            List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
            for (InetAddress addr : addrs) {
                if (!addr.isLoopbackAddress()) {
                    String sAddr = addr.getHostAddress().toUpperCase();
                    boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                    if (useIPv4) {
                        if (isIPv4)
                            return sAddr;
                    } else {
                        if (!isIPv4) {
                            int delim = sAddr.indexOf('%'); // drop ip6 port suffix
                            return delim<0 ? sAddr : sAddr.substring(0, delim);
                        }
                    }
                }
            }
        }

        throw new UnsupportedOperationException("Could not find IP address.");
    }

    public static final int IMG_QUALITY = 95;

    /**
     * Based off http://www.badlogicgames.com/forum/viewtopic.php?f=11&t=8947&p=40600&hilit=saving+pixmap+as+jpeg#p40606
     */
    public static void writePNG(OutputStream stream, Pixmap p) throws IOException {
        int w = p.getWidth();
        int h = p.getHeight();

        int[] pixels = new int[w * h];
        for (int y=0; y<h; y++) {
            for (int x=0; x<w; x++) {
                //convert RGBA to RGB
                int value = p.getPixel(x, y);
                int R = ((value & 0xff000000) >>> 24);
                int G = ((value & 0x00ff0000) >>> 16);
                int B = ((value & 0x0000ff00) >>> 8);
                int A = ((value & 0x000000ff));

                int i = x + (y * w);
                pixels[ i ] = (A << 24) | (R << 16) | (G << 8) | B;
            }
        }

        Bitmap b = Bitmap.createBitmap(pixels, w, h, Bitmap.Config.ARGB_8888);
        b.compress(Bitmap.CompressFormat.PNG, IMG_QUALITY, stream);
    }
}
