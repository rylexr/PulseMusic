package com.hardcodecoder.pulsemusic.playback;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hardcodecoder.pulsemusic.helper.MediaArtHelper;
import com.hardcodecoder.pulsemusic.model.MusicModel;
import com.hardcodecoder.pulsemusic.providers.ProviderManager;
import com.hardcodecoder.pulsemusic.singleton.TrackManager;

import java.io.InputStream;

public class PlaybackManager implements Playback.Callback {

    public static final String ACTION_LOAD_LAST_TRACK = "LoadLastTrack";
    public static final String TRACK_ITEM = "TrackItem";
    public static final String PLAYBACK_POSITION = "PlaybackPosition";
    public static final short ACTION_PLAY_NEXT = 1;
    public static final short ACTION_PLAY_PREV = -1;
    private final PlaybackState.Builder mStateBuilder = new PlaybackState.Builder();
    private final Playback mPlayback;
    private final PlaybackServiceCallback mServiceCallback;
    private final TrackManager mTrackManager;
    private final Context mContext;
    private boolean mManualPause;
    private final MediaSession.Callback mMediaSessionCallback = new MediaSession.Callback() {
        @Override
        public void onPlay() {
            handlePlayRequest();
            mManualPause = false;
        }

        @Override
        public void onPause() {
            handlePauseRequest();
            mManualPause = true;
        }

        @Override
        public void onSkipToNext() {
            handleSkipRequest(ACTION_PLAY_NEXT);
        }

        @Override
        public void onSkipToPrevious() {
            handleSkipRequest(ACTION_PLAY_PREV);
        }

        @Override
        public void onStop() {
            handleStopRequest();
        }

        @Override
        public void onSeekTo(long pos) {
            mPlayback.onSeekTo((int) pos);
        }

        @Override
        public void onCustomAction(@NonNull String action, @Nullable Bundle extras) {
            if (action.equals(ACTION_LOAD_LAST_TRACK) && extras != null)
                handleLoadLastTrack(extras);
        }
    };

    public PlaybackManager(Context context, Playback playback, PlaybackServiceCallback serviceCallback) {
        mContext = context;
        mPlayback = playback;
        mTrackManager = TrackManager.getInstance();
        mServiceCallback = serviceCallback;
        mPlayback.setCallback(this);
    }

    public MediaSession.Callback getSessionCallbacks() {
        return mMediaSessionCallback;
    }

    private void handlePlayRequest() {
        mServiceCallback.onPlaybackStart();
        mPlayback.onPlay(0, true);
    }

    private void handlePauseRequest() {
        mServiceCallback.onPlaybackStopped();
        mPlayback.onPause();
    }

    private void handleStopRequest() {
        mServiceCallback.onPlaybackStopped();
        mPlayback.onStop(true);
    }

    private void handleSkipRequest(short di) {
        if (mTrackManager.canSkipTrack(di)) handlePlayRequest();
        else handlePauseRequest();
    }

    private void handleLoadLastTrack(@NonNull Bundle bundle) {
        mServiceCallback.onPlaybackStart();
        final MusicModel trackItem = (MusicModel) bundle.getSerializable(TRACK_ITEM);
        if (trackItem == null) return;
        final int resumePosition = bundle.getInt(PLAYBACK_POSITION);
        mTrackManager.addToActiveQueue(trackItem);
        mTrackManager.setActiveIndex(0);
        mPlayback.onPlay(resumePosition, false);
    }

    private void updatePlaybackState(int currentState) {
        mStateBuilder.setState(currentState, mPlayback.getCurrentStreamingPosition(), currentState == PlaybackState.STATE_PLAYING ? 1 : 0);
        mStateBuilder.setActions(getActions(currentState));
        mServiceCallback.onPlaybackStateChanged(mStateBuilder.build());

        if (currentState == PlaybackState.STATE_PLAYING) {
            mServiceCallback.onStartNotification();
        } else if (currentState == PlaybackState.STATE_STOPPED) {
            mServiceCallback.onStopNotification();
        }
    }

    private Bitmap loadAlbumArt(String path, int albumId) {
        // We know that manually selected tracks have negative album id
        if (albumId < 0)
            return MediaArtHelper.getDefaultAlbumArtBitmap(mContext, albumId);
        try {
            Uri uri = Uri.parse(path);
            InputStream is = mContext.getContentResolver().openInputStream(uri);
            return BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            return MediaArtHelper.getDefaultAlbumArtBitmap(mContext, albumId);
        }
    }

    private long getActions(int state) {
        long actions;
        if (state == PlaybackState.STATE_PLAYING) {
            actions = PlaybackState.ACTION_PAUSE;
        } else {
            actions = PlaybackState.ACTION_PLAY;
        }
        return PlaybackState.ACTION_SKIP_TO_PREVIOUS | PlaybackState.ACTION_SKIP_TO_NEXT | actions | PlaybackState.ACTION_SEEK_TO;
    }

    @Override
    public void onFocusChanged(boolean resumePlayback) {
        if (mManualPause) return;
        if (resumePlayback) handlePlayRequest();
        else handlePauseRequest();
    }

    @Override
    public void onPlaybackCompletion() {
        handleSkipRequest(ACTION_PLAY_NEXT);
    }

    @Override
    public void onPlaybackStateChanged(int state) {
        updatePlaybackState(state);
    }

    @Override
    public void onTrackChanged(@NonNull MusicModel trackItem) {
        // Track has changed, need to update metadata
        MediaMetadata.Builder metadataBuilder = new MediaMetadata.Builder();
        metadataBuilder.putLong(MediaMetadata.METADATA_KEY_DURATION, trackItem.getTrackDuration());
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE, trackItem.getTrackName());
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_ARTIST, trackItem.getArtist());
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_ALBUM, trackItem.getAlbum());
        metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, loadAlbumArt(trackItem.getAlbumArtUrl(), trackItem.getAlbumId()));
        mServiceCallback.onMetaDataChanged(metadataBuilder.build());

        // Do not save any media that was picked by user
        // All data might not available to work with such tracks when building
        // HistoryRecords and or TopAlbums/TopArtist
        if (trackItem.getAlbumId() >= 0)
            ProviderManager.getHistoryProvider().addToHistory(trackItem);
    }

    public interface PlaybackServiceCallback {
        void onPlaybackStart();

        void onPlaybackStopped();

        void onStartNotification();

        void onStopNotification();

        void onPlaybackStateChanged(PlaybackState newState);

        void onMetaDataChanged(MediaMetadata newMetaData);
    }
}