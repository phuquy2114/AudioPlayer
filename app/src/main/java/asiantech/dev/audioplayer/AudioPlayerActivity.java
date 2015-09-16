package asiantech.dev.audioplayer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import asiantech.dev.audioplayer.controls.Controls;
import asiantech.dev.audioplayer.service.SongService;
import asiantech.dev.audioplayer.util.PlayerConstants;
import asiantech.dev.audioplayer.util.UtilFunctions;
import asiantech.dev.audioplayer.util.Utilities;


public class AudioPlayerActivity extends Activity implements SeekBar.OnSeekBarChangeListener {

    static ImageView mImgBack;
    static ImageView mImgPause;
    static ImageView mImgPlay;
    static ImageView mImgNext;
    static TextView textNowPlaying;
    static TextView textAlbumArtist;
    static TextView textComposer;
    static LinearLayout linearLayoutPlayer;
    static Context context;
    TextView textBufferDuration, textDuration;
    private SeekBar mSeekBar;
    private Utilities utils;
    private Handler mHandler = new Handler();


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().hide();
        setContentView(R.layout.activity_full_play);
        utils = new Utilities();
        context = this;
        init();
    }

    private void init() {
        getViews();
        setListeners();
        PlayerConstants.PROGRESSBAR_HANDLER = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Integer i[] = (Integer[]) msg.obj;
                textBufferDuration.setText(UtilFunctions.getDuration(i[0]));
                textDuration.setText(UtilFunctions.getDuration(i[1]));
            }
        };

        // set Progress bar values
        mSeekBar.setProgress(0);
        mSeekBar.setMax(100);
        updateProgressBar();
    }

    private void setListeners() {
        mImgBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Controls.previousControl(getApplicationContext());
            }
        });

        mImgPause.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Controls.pauseControl(getApplicationContext());
            }
        });

        mImgPlay.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Controls.playControl(getApplicationContext());
            }
        });

        mImgNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Controls.nextControl(getApplicationContext());
            }
        });

        mSeekBar.setOnSeekBarChangeListener(this);
    }

    public static void changeUI() {
        updateUI();
        changeButton();
    }


    private void getViews() {
        mImgBack = (ImageView) findViewById(R.id.imgBack);
        mImgPause = (ImageView) findViewById(R.id.imgPause);
        mImgNext = (ImageView) findViewById(R.id.imgNext);
        mImgPlay = (ImageView) findViewById(R.id.imgPlay);
        textNowPlaying = (TextView) findViewById(R.id.textNowPlaying);
        linearLayoutPlayer = (LinearLayout) findViewById(R.id.linearLayoutPlayer);
        textAlbumArtist = (TextView) findViewById(R.id.textAlbumArtist);
        textComposer = (TextView) findViewById(R.id.textComposer);
        textBufferDuration = (TextView) findViewById(R.id.textBufferDuration);
        textDuration = (TextView) findViewById(R.id.textDuration);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        textNowPlaying.setSelected(true);
        textAlbumArtist.setSelected(true);
        Log.d("xxx", SongService.mp.getCurrentPosition() + "");
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isServiceRunning = UtilFunctions.isServiceRunning(SongService.class.getName(), getApplicationContext());
        if (isServiceRunning) {
            updateUI();
        }
        changeButton();
    }

    public static void changeButton() {
        if (PlayerConstants.SONG_PAUSED) {
            mImgPause.setVisibility(View.GONE);
            mImgPlay.setVisibility(View.VISIBLE);
        } else {
            mImgPause.setVisibility(View.VISIBLE);
            mImgPlay.setVisibility(View.GONE);
        }
    }

    private void updateProgressBar() {

        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    /**
     * Background Runnable thread
     */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = SongService.mp.getDuration();
            long currentDuration = SongService.mp.getCurrentPosition();
            // Updating progress bar
            int progress = (int) (utils.getProgressPercentage(currentDuration, totalDuration));
            //Log.d("Progress", ""+progress);
            mSeekBar.setProgress(progress);
            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };


    private static void updateUI() {
        try {
            String songName = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getTitle();
            String artist = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getArtist();
            String album = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getAlbum();
            String composer = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getComposer();
            textNowPlaying.setText(songName);
            textAlbumArtist.setText(artist + " - " + album);
            if (composer != null && composer.length() > 0) {
                textComposer.setVisibility(View.VISIBLE);
                textComposer.setText(composer);
            } else {
                textComposer.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            long albumId = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getAlbumId();
            Bitmap albumArt = UtilFunctions.getAlbumart(context, albumId);
            if (albumArt != null) {
                linearLayoutPlayer.setBackgroundDrawable(new BitmapDrawable(albumArt));
            } else {
                linearLayoutPlayer.setBackgroundDrawable(new BitmapDrawable(UtilFunctions.getDefaultAlbumArt(context)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = SongService.mp.getDuration();
        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

        // forward or backward to certain seconds
        SongService.mp.seekTo(currentPosition);

        // update timer progress again
        updateProgressBar();
    }
}
