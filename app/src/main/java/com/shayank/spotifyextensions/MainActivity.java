package com.shayank.spotifyextensions;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.Switch;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.error.SpotifyAppRemoteException;
import com.spotify.protocol.types.PlayerState;

public class MainActivity extends AppCompatActivity {

    private static final String CLIENT_ID = "903da8b6bca24cb49892412701cac9fb";
    private static final String REDIRECT_URI = "http://com.shayank.spotifyextensions/callback";
    private SpotifyAppRemote mSpotifyAppRemote;
    private boolean connected = false;

    private Switch shuffleSwitch;
    private NumberPicker nextTrackPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        String action = intent.getAction();
        Uri data = intent.getData();
        if (data != null) {
            Log.d("MainActivity", data.getHost());
        } else {
            Log.d("MainActivity", "Intent data has no host");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        shuffleSwitch = findViewById(R.id.shuffleSwitch);
        shuffleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                toggleShuffle(isChecked);
            }
        });

        connect();
        if (connected) {
            connected();
        }

        nextTrackPicker = findViewById(R.id.nextTrackPicker);
        nextTrackPicker.setMinValue(1);
        nextTrackPicker.setMaxValue(100);
        nextTrackPicker.setWrapSelectorWheel(true);
    }

    private void toggleShuffle(boolean toShuffle) {
        if (connected){
            mSpotifyAppRemote.getPlayerApi().setShuffle(toShuffle);
        } else {
            Log.d("MainActivity", "Cannot complete shuffle toggle as spotify is not connected");
            connect();
        }
    }

    private void connect() {
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();
        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity", "Connected! Yay!");

                        connected = true;
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e("MainActivity", throwable.getMessage(), throwable);

                        connected = false;
                    }
                });
    }

    private void connected() {
        mSpotifyAppRemote.getPlayerApi().getPlayerState().setResultCallback(new CallResult.ResultCallback<PlayerState>() {
            @Override
            public void onResult(PlayerState playerState) {
                if (playerState.playbackOptions.isShuffling) {
                    shuffleSwitch.setChecked(true);
                } else {
                    shuffleSwitch.setChecked(false);
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
        connected = false;

    }
}
