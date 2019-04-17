package com.dc.exoplayersample.video;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.drm.DrmStore;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dc.exoplayersample.MainActivity;
import com.dc.exoplayersample.R;
import com.dc.exoplayersample.api.ApiClient;
import com.dc.exoplayersample.api.ApiInterface;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoPlayerActivity extends AppCompatActivity {

    private ConstraintLayout constraintlayout;
    private PlayerView simpleExoPlayerView;
    private SimpleExoPlayer player;
    private ImageView screenRotation;
    private TextView title;
    private TextView description;
    private ImageView controllerLock;
    private boolean isControlLocked = false;
    private TextView exo_position;
    private DefaultTimeBar exo_progress;
    private TextView exo_duration;
    private ImageView playPause;
    private ImageView back;

    private boolean isPlaying = true;
    private ConstraintLayout bottomContainer;
    private ConstraintLayout topContainer;
    private TextView counter;
    private AudioManager audioManager;
    private float touchY;
    private float motionDownXPosition;
    private float motionDownYPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        findIds();
        clickListener();
        callVideoApi();

    }

    private void clickListener() {
        screenRotation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setOrientationControl();
            }
        });
        controllerLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLockControl();
            }
        });
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPlayPause();
            }
        });

        simpleExoPlayerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        motionDownXPosition = event.getX();
                        motionDownYPosition = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        controlVolume(event);
                        break;
                    case MotionEvent.ACTION_UP:
                        motionDownXPosition = 0;
                        motionDownYPosition = 0;
                        simpleExoPlayerView.showController();
                        break;
                }
                return true;
            }
        });
    }


    private void findIds() {
        //View customControllerView = LayoutInflater.from(this).inflate(R.layout.video_player_custom_controller, null);
        constraintlayout = findViewById(R.id.constraintLayout);
        simpleExoPlayerView = findViewById(R.id.simpleExoPlayerView);
        description = findViewById(R.id.description);

        bottomContainer = findViewById(R.id.bottomContainer);
        topContainer = findViewById(R.id.topContainer);
        screenRotation = findViewById(R.id.screenRotation);
        controllerLock = findViewById(R.id.controllerLock);
        title = findViewById(R.id.title);
        exo_position = findViewById(R.id.exo_position);
        exo_progress = findViewById(R.id.exo_progress);
        exo_duration = findViewById(R.id.exo_duration);
        playPause = findViewById(R.id.playPause);
        back = findViewById(R.id.back);
        counter = findViewById(R.id.counter);

    }

    private void callVideoApi() {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<VideoResponse> call = apiInterface.getVideo();
        call.enqueue(new Callback<VideoResponse>() {
            @Override
            public void onResponse(@NonNull Call<VideoResponse> call, @NonNull Response<VideoResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        setDataToFields(response.body());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<VideoResponse> call, @NonNull Throwable t) {

            }
        });

    }

    private void setPlayPause() {
        if (isPlaying) {
            player.setPlayWhenReady(false);
            playPause.setImageResource(R.drawable.ic_play);
        } else {
            player.setPlayWhenReady(true);
            playPause.setImageResource(R.drawable.ic_pause);
        }
        isPlaying = !isPlaying;
    }

    private void setOrientationControl() {
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
    }

    private void setLockControl() {
        if (isControlLocked) {
            showControllers();
            controllerLock.setImageResource(R.drawable.ic_lock);
        } else {
            hideControllers();
            controllerLock.setImageResource(R.drawable.ic_unlock);
        }
        isControlLocked = !isControlLocked;
    }

    private void hideControllers() {
        playPause.setVisibility(View.GONE);
        topContainer.setVisibility(View.GONE);
        bottomContainer.setVisibility(View.GONE);
    }

    private void showControllers() {
        playPause.setVisibility(View.VISIBLE);
        topContainer.setVisibility(View.VISIBLE);
        bottomContainer.setVisibility(View.VISIBLE);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        setPlayerConstraints();
        super.onConfigurationChanged(newConfig);
    }

    private void setPlayerConstraints() {
        int orientation = getResources().getConfiguration().orientation;
        ConstraintSet set = new ConstraintSet();
        set.clone(constraintlayout);
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideSystemUI();
            set.connect(simpleExoPlayerView.getId(), ConstraintSet.BOTTOM, constraintlayout.getId(), ConstraintSet.BOTTOM, 0);
            set.constrainHeight(R.id.simpleExoPlayerView, 0);
            set.applyTo(constraintlayout);
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            showSystemUI();
            set.connect(simpleExoPlayerView.getId(), ConstraintSet.TOP, constraintlayout.getId(), ConstraintSet.TOP, 0);
            set.clear(R.id.simpleExoPlayerView, ConstraintSet.BOTTOM);
            int heightDpToPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics());
            set.constrainHeight(R.id.simpleExoPlayerView, heightDpToPx);
            set.applyTo(constraintlayout);
        }
    }

    private void setDataToFields(VideoResponse body) {
        initializePlayer(body.getSources());
        description.setText(body.getDescription());
        title.setText(body.getTitle());
    }

    private void initializePlayer(String sources) {
        player = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector());
        simpleExoPlayerView.setPlayer(player);
        simpleExoPlayerView.setUseController(true);
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "exo-player"));
        MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(sources));
        simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
        player.prepare(mediaSource);
        player.setPlayWhenReady(true);
    }


    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    @Override
    protected void onPause() {
        if (player != null) {
            player.setPlayWhenReady(false);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.setPlayWhenReady(true);
        }
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideSystemUI();
        }
    }


    private void controlVolume(MotionEvent event) {


    }

    private int[] getScreenHeightWidth() {
        int[] heightWidth = new int[2];
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        heightWidth[0] = size.x;
        heightWidth[1] = size.y;
        return heightWidth;
    }

}