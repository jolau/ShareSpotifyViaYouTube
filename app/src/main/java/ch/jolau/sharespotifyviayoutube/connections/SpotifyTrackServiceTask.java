package ch.jolau.sharespotifyviayoutube.connections;

import android.os.AsyncTask;

import ch.jolau.sharespotifyviayoutube.AppConstants;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by jolau on 05.10.15.
 */
public class SpotifyTrackServiceTask extends AsyncTask<Object, Void, Object[]> implements
        ServiceTaskInterface {
    private static final String TAG = SpotifyTrackServiceTask.class.getSimpleName();
    private ServerResponseListener mServerResponseListener = null;
    private int mRequestCode = 0;

    public void setmServerResponseListener(
            ServerResponseListener mServerResponseListener) {
        this.mServerResponseListener = mServerResponseListener;
    }

    public SpotifyTrackServiceTask(int iReqCode) {
        mRequestCode = iReqCode;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mServerResponseListener.prepareRequest(mRequestCode);
    }

    @Override
    protected Object[] doInBackground(Object... params) {
        if (params == null)
            throw new NullPointerException("Parameters to the async task can never be null");

        mServerResponseListener.goBackground();

        Object[] resultDetails = new Object[2];
        resultDetails[0] = mRequestCode;

        switch (mRequestCode) {
            case AppConstants.GET_SPOTIFY_TRACK:
                Track track = getSpotifyTrack((String) params[0]);
                resultDetails[1] = track;
                break;
        }

        return resultDetails;
    }

    private Track getSpotifyTrack(String trackId) {
        SpotifyApi api = new SpotifyApi();

        SpotifyService spotify = api.getService();

        Track track = spotify.getTrack((String) trackId);
        return track;
    }

    @Override
    protected void onPostExecute(Object[] result) {
        super.onPostExecute(result);
        mServerResponseListener.completedRequest(result);
    }
}
