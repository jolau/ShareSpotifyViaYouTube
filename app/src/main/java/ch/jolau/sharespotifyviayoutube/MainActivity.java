package ch.jolau.sharespotifyviayoutube;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.api.services.youtube.model.SearchResult;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.jolau.sharespotifyviayoutube.connections.ServerResponseListener;
import ch.jolau.sharespotifyviayoutube.connections.SpotifyTrackServiceTask;
import ch.jolau.sharespotifyviayoutube.connections.YouTubeSearchServiceTask;
import kaaes.spotify.webapi.android.models.Track;

public class MainActivity extends AppCompatActivity implements ServerResponseListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            }
        }
    }

    private void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            String trackId = getSpotifyTrackId(sharedText);
            if (trackId != null) {
                startSpotifyTrackRetrieval(trackId);
            } else {
                showLinkErrorAlert();
            }
        }
    }

    private String getSpotifyTrackId(String url) {
        Pattern p = Pattern.compile("https?://(?:embed\\.|open\\.)(?:spotify\\.com/)(?:track/|\\?uri=spotify:track:)((\\w|-){22})");
        Matcher m = p.matcher(url);
        while (m.find()) { // Find each match in turn; String can't do this.
            String trackId = m.group(1); // Access a submatch group; String can't do this.
            return trackId;
        }
        return null;
    }

    private void showLinkErrorAlert() {
        new AlertDialog.Builder(this)
                .setTitle("No Spotify Link")
                .setMessage("Sorry, this is not a valid Spotify link for a track.")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void startSpotifyTrackRetrieval(String trackId) {
        SpotifyTrackServiceTask spotifyTrackServiceTask = new SpotifyTrackServiceTask(GET_SPOTIFY_TRACK);
        spotifyTrackServiceTask.setmServerResponseListener(this);
        spotifyTrackServiceTask.execute(new String[]{trackId});
    }

    private void startYoutubeSearch(String searchTerm) {
        YouTubeSearchServiceTask mYtServiceTask = new YouTubeSearchServiceTask(SEARCH_VIDEO);
        mYtServiceTask.setmServerResponseListener(this);
        mYtServiceTask.execute(new String[]{searchTerm});
    }

    @Override
    public void prepareRequest(Object... objects) {

    }

    @Override
    public void goBackground(Object... objects) {

    }

    @Override
    public void completedRequest(Object... objects) {
        // Parse the response based upon type of request
        Integer reqCode = (Integer) objects[0];

        if (reqCode == null || reqCode == 0)
            throw new NullPointerException("Request Code's value is Invalid.");

        switch (reqCode) {
            case GET_SPOTIFY_TRACK:
                Track track = ((Track) objects[1]);
                String artistName = track.artists.get(0).name;
                String trackName = track.name;
                startYoutubeSearch(artistName + "+" + trackName);
                break;
            case SEARCH_VIDEO:
                String videoId = ((List<SearchResult>) objects[1]).get(0).getId().getVideoId();
                String url = "https://youtu.be/" + videoId;
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Spotify track as YouTube link");
                sendIntent.putExtra(Intent.EXTRA_TEXT, url);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getResources().getString(R.string.share_using)));
                this.finish();
                break;
        }
    }
}
