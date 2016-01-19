package io.github.stevenrudenko.iot.sample.sensor.core.io;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;

import io.github.stevenrudenko.iot.sample.sensor.core.base.BaseIoTSensor;

/** Audio jack button (like Pressy) reader. */
public abstract class MicSensor extends BaseIoTSensor implements Runnable {
    /** Log tag. */
    private static final String TAG = MicSensor.class.getSimpleName();

    /** Audio sample frequency. Used to get buffer for audio encoding/decoding. */
    private static final int AUDIO_SAMPLE_FREQ = 8000;
    /** Buffer used to read data from input device. */
    private static final int READ_BUFFER_SIZE = 1024;

    /** Audio recorder used to read sounds from audio jack input. */
    @Nullable
    private AudioRecord recorder;

    /** Used to read data from audio jack input. */
    private Handler workerThreadHandler;

    public MicSensor(float[] value) {
        super(value);
    }

    @Override
    public boolean prepare(final Context context) {
        if (recorder == null) {
            final int minBufferSize = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_FREQ, 2,
                    AudioFormat.ENCODING_PCM_16BIT);
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    AUDIO_SAMPLE_FREQ, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, minBufferSize);
        }
        return true;
    }

    @Override
    public void start(final OnSensorListener listener) {
        if (isRunning()) {
            return;
        }
        if (workerThreadHandler != null) {
            return;
        }
        if (recorder == null) {
            prepare(null);
        }
        // start listening for audio jack input
        recorder.startRecording();
        // launching reader thread
        final HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        workerThreadHandler = new Handler(thread.getLooper());
        workerThreadHandler.post(this);
        super.start(listener);
    }

    @Override
    public void stop() {
        super.stop();
        if (recorder != null) {
            recorder.stop();
        }
        if (workerThreadHandler != null) {
            workerThreadHandler.getLooper().quit();
            workerThreadHandler = null;
        }
    }

    @Override
    public void release() {
        super.release();
        stop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }

    @Override
    public void run() {
        final short data[] = new short[READ_BUFFER_SIZE];
        while (isRunning()) {
            // gets the voice output from microphone to byte format
            if (recorder == null) {
                break;
            }
            recorder.read(data, 0, READ_BUFFER_SIZE);
            analyze(data);
        }
    }

    /**
     * Analyze read data from mic jack.
     * @param data data read from mic jack.
     * @return {@code true} if data has been processed.
     */
    protected abstract boolean analyze(short data[]);

}
