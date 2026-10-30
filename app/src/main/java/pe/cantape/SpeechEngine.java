package pe.cantape;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.ArrayDeque;
import java.util.Locale;

/**
 * Speaks announcements through the phone's TTS engine, which is a separate app. Some phones
 * block or freeze that engine while this app is in the background, so every announcement is
 * watched: if speech has not started in time the engine is reconnected and the words retried,
 * and if it still stays silent a notification tone is played so no payment passes unheard.
 */
final class SpeechEngine implements TextToSpeech.OnInitListener {
    private static final long START_TIMEOUT_MS = 5_000;
    private static final long INIT_RETRY_MS = 3_000;
    private static final int MAX_ATTEMPTS = 2;
    private static final long VOLUME_RESTORE_MS = 1_200;
    private static final long TONE_RESTORE_MS = 4_000;

    private static final class Job {
        final String words;
        final String id = "cantape-" + System.nanoTime();
        int tries;
        int watchToken;
        boolean started;
        Job(String words) { this.words = words; }
    }

    private final ArrayDeque<Job> pending = new ArrayDeque<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable reconnectTask = this::reconnect;
    private final Runnable restoreVolumeTask = this::restoreVolumeIfIdle;
    private final AppSettings settings;
    private final AudioManager audio;
    private final Context context;
    private TextToSpeech tts;
    private Job current;
    private boolean ready;
    private boolean closed;
    private int savedVolume = -1;

    SpeechEngine(Context context) {
        this.context = context.getApplicationContext();
        settings = new AppSettings(this.context);
        audio = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);
        connect();
    }

    private void connect() {
        ready = false;
        tts = new TextToSpeech(context, this);
    }

    @Override
    public synchronized void onInit(int status) {
        if (closed || tts == null) return;
        if (status != TextToSpeech.SUCCESS || !configure()) {
            // The engine may be blocked right now; keep trying instead of giving up for good.
            handler.removeCallbacks(reconnectTask);
            handler.postDelayed(reconnectTask, INIT_RETRY_MS);
            return;
        }
        ready = true;
        speakNext();
    }

    private boolean configure() {
        int language = tts.setLanguage(new Locale("es", "PE"));
        if (language == TextToSpeech.LANG_MISSING_DATA || language == TextToSpeech.LANG_NOT_SUPPORTED) {
            language = tts.setLanguage(new Locale("es", "ES"));
        }
        if (language == TextToSpeech.LANG_MISSING_DATA || language == TextToSpeech.LANG_NOT_SUPPORTED) return false;
        tts.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build());
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override public void onStart(String id) { started(id); }
            @Override public void onDone(String id) { finished(id); }
            @Override public void onError(String id) { failed(id); }
        });
        return true;
    }

    synchronized void speak(String words) {
        if (closed) return;
        if (pending.size() >= 5) pending.removeFirst();
        Job job = new Job(words);
        pending.addLast(job);
        watch(job);
        speakNext();
    }

    /** Hands the next queued job to the engine when it is free; must hold the lock. */
    private void speakNext() {
        if (closed || current != null || !ready) return;
        Job job = pending.pollFirst();
        if (job == null) return;
        current = job;
        if (!speakNow(job)) {
            current = null;
            pending.addFirst(job);
            reconnect();
        }
    }

    private void watch(Job job) {
        int token = ++job.watchToken;
        handler.postDelayed(() -> checkStarted(job, token), START_TIMEOUT_MS);
    }

    private synchronized void checkStarted(Job job, int token) {
        if (closed || job.started || token != job.watchToken) return;
        if (current != null && current != job && current.started) {
            watch(job); // Only waiting behind another announcement that is playing.
            return;
        }
        retryOrFallBack(job);
    }

    private void retryOrFallBack(Job job) {
        if (current == job) current = null;
        pending.remove(job);
        job.tries++;
        if (job.tries < MAX_ATTEMPTS) {
            // Engine connection looks stuck: rebuild it and retry these words first.
            pending.addFirst(job);
            watch(job);
            reconnect();
        } else {
            job.watchToken++;
            playFallbackTone();
            speakNext();
            if (current == null) scheduleVolumeRestore(TONE_RESTORE_MS);
        }
    }

    private synchronized void reconnect() {
        if (closed) return;
        handler.removeCallbacks(reconnectTask);
        current = null;
        if (tts != null) {
            try {
                tts.shutdown();
            } catch (RuntimeException ignored) { }
        }
        connect();
    }

    private synchronized void started(String id) {
        if (current != null && current.id.equals(id)) current.started = true;
    }

    private synchronized void finished(String id) {
        if (current == null || !current.id.equals(id)) return;
        current.watchToken++;
        current = null;
        speakNext();
        if (current == null) scheduleVolumeRestore(VOLUME_RESTORE_MS);
    }

    private synchronized void failed(String id) {
        if (current == null || !current.id.equals(id)) return;
        retryOrFallBack(current);
    }

    private boolean speakNow(Job job) {
        setVolume();
        tts.setPitch(settings.isMaleVoice() ? 0.75f : 1.18f);
        Bundle params = new Bundle();
        params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1f);
        return tts.speak(job.words, TextToSpeech.QUEUE_ADD, params, job.id) == TextToSpeech.SUCCESS;
    }

    /** Puts back the volume the user had before the announcement once nothing is left to say. */
    private synchronized void restoreVolumeIfIdle() {
        if (savedVolume < 0 || current != null || !pending.isEmpty()) return;
        try {
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, savedVolume, 0);
        } catch (SecurityException ignored) { }
        savedVolume = -1;
    }

    private void scheduleVolumeRestore(long delayMs) {
        handler.removeCallbacks(restoreVolumeTask);
        handler.postDelayed(restoreVolumeTask, delayMs);
    }

    private void setVolume() {
        handler.removeCallbacks(restoreVolumeTask);
        if (savedVolume < 0) savedVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        int max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int desired = Math.round(max * settings.volumePercent() / 100f);
        try {
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, desired, 0);
        } catch (SecurityException ignored) {
            // Some device policies prevent changing volume; use its existing setting.
        }
    }

    /** Played in this app's own process, so it works even when the TTS engine is blocked. */
    private void playFallbackTone() {
        try {
            Ringtone tone = RingtoneManager.getRingtone(context,
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            if (tone == null) return;
            setVolume();
            tone.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build());
            tone.play();
        } catch (RuntimeException ignored) { }
    }

    synchronized void close() {
        closed = true;
        handler.removeCallbacksAndMessages(null);
        pending.clear();
        current = null;
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        restoreVolumeIfIdle();
    }
}
