
package com.moodmap;

import android.content.Context;
import android.os.RemoteException;
import android.text.format.Time;

import com.mood.models.Mood;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.probemanager.ProbeWriter;
import org.ohmage.probemanager.ResponseBuilder;

import java.util.Date;
import java.util.UUID;

public class MoodMapProbeWriter extends ProbeWriter {

    private static final String CAMPAIGN_URN = "urn:campaign:ohmage:mood_map";
    private static final String CAMPAIGN_CREATED = "2012-08-29 16:10:15";

    public MoodMapProbeWriter(Context context) {
        super(context);
    }

    public void writeResponse(int locationType, Mood moodPoint) {

        JSONArray responses = new JSONArray();
        JSONObject loc = new JSONObject();
        JSONObject energy = new JSONObject();
        JSONObject mood = new JSONObject();

        try {
            loc.put("value", locationType);
            loc.put("prompt_id", "location");

            energy.put("value", Math.round(moodPoint.energyLevel * 10));
            energy.put("prompt_id", "energy");

            mood.put("value", Math.round(moodPoint.moodLevel * 10));
            mood.put("prompt_id", "mood");

            responses.put(loc);
            responses.put(energy);
            responses.put(mood);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Create the response builder for the correct campaign
        ResponseBuilder response = new ResponseBuilder(CAMPAIGN_URN, CAMPAIGN_CREATED);

        // Set all the data for the response
        response.withTime(new Date().getTime(), Time.getCurrentTimezone())
                .withSurveyKey(UUID.randomUUID().toString()).withSurveyId("moodMap")
                .withSurveyLaunchContext(new Date().getTime(), Time.getCurrentTimezone())
                .withResponses(responses.toString());

        // Write the response
        try {
            response.write(this);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
