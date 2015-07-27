package net.varunramesh.hnefatafl.simulator;

import com.google.gson.JsonElement;

/**
 * Created by Varun on 7/25/2015.
 */
public interface Saveable {
    JsonElement toJson();
}
