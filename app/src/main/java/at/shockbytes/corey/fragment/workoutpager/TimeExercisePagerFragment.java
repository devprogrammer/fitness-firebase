package at.shockbytes.corey.fragment.workoutpager;


import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import at.shockbytes.corey.R;
import at.shockbytes.corey.fragment.dialogs.TimeExerciseCountdownDialogFragment;
import at.shockbytes.corey.workout.model.TimeExercise;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


public class TimeExercisePagerFragment extends Fragment {

    private static final String ARG_EXERCISE = "arg_exercise";

    private TimeExercise exercise;

    private Vibrator vibrator;
    private boolean isVibrationEnabled;

    private int secondsUntilFinish;
    private Observable<Long> timerObservable;
    private Subscription timerSubscription;

    @Bind(R.id.fragment_pageritem_time_exercise_txt_exercise)
    protected TextView txtExercise;

    @Bind(R.id.fragment_pageritem_time_exercise_txt_time)
    protected TextView txtTime;

    @Bind(R.id.fragment_pageritem_time_exercise_progressbar)
    protected ProgressBar progressBar;

    public TimeExercisePagerFragment() {
    }

    public static TimeExercisePagerFragment newInstance(TimeExercise exercise) {
        TimeExercisePagerFragment fragment = new TimeExercisePagerFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_EXERCISE, exercise);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        exercise = getArguments().getParcelable(ARG_EXERCISE);
        vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        isVibrationEnabled = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getBoolean(getString(R.string.prefs_vibrations_key), false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pageritem_time_exercise, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize();
    }

    @OnClick(R.id.fragment_pageritem_time_exercise_btn_start)
    protected void onClickButtonStart() {

        int countdown = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getInt(getString(R.string.prefs_time_countdown_key), 5);

        TimeExerciseCountdownDialogFragment fragment = TimeExerciseCountdownDialogFragment
                .newInstance(countdown);
        fragment.setCountdownCompleteListener(new TimeExerciseCountdownDialogFragment
                .OnCountDownCompletedListener() {
            @Override
            public void onCountdownCompleted() {

                progressBar.setProgress(0);
                timerSubscription = timerObservable.subscribe(new Action1<Long>() {

                    long seconds = 0;

                    @Override
                    public void call(Long aLong) {

                        long toGo = secondsUntilFinish - seconds;
                        progressBar.setProgress(progressBar.getProgress() + 10);

                        // Timer will fire every 10 milliseconds
                        if (aLong % 100 == 0) {
                            displayTime(toGo);
                            seconds++;
                        }

                        if (toGo < 0) {
                            timerSubscription.unsubscribe();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.wtf("Corey", throwable.toString());
                    }
                });
            }
        });
        fragment.show(getFragmentManager(), fragment.getTag());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timerSubscription != null && !timerSubscription.isUnsubscribed()) {
            timerSubscription.unsubscribe();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (timerSubscription != null && !isVisibleToUser) {
            timerSubscription.unsubscribe();
        }
    }

    private void initialize() {

        secondsUntilFinish = exercise.getWorkoutDurationInSeconds();

        progressBar.setMax(secondsUntilFinish * 1000);
        progressBar.setSecondaryProgress(progressBar.getMax());
        txtTime.setText(calculateDisplayString(secondsUntilFinish));
        txtExercise.setText(exercise.getDisplayName(getContext()));

        timerObservable = Observable.interval(10, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
    }

    private String calculateDisplayString(int seconds) {

        int mins = 0;
        while (seconds >= 60) {
            mins++;
            seconds -= 60;
        }
        return mins + ":" + ((seconds >= 10) ? seconds : "0" + seconds);
    }

    private void vibrate(long secondsToGo) {

        if (isVibrationEnabled) {
            int vibrationIntensity = 0;
            if (secondsToGo == 0) {
                vibrationIntensity = 800;
            } else if (secondsToGo % (exercise.getRestDuration() + exercise.getWorkDuration()) == 0) {
                // Full round
                vibrationIntensity = 300;
            } else if (secondsToGo % exercise.getWorkDuration() == 0) {
                // Work done
                vibrationIntensity = 150;
            }
            vibrator.vibrate(vibrationIntensity);
        }
    }

    private void displayTime(long secondsToGo) {

        //Calculate displayable string
        long seconds = secondsToGo;
        long minutes = 0;
        while (seconds >= 60) {
            minutes++;
            seconds -= 60;
        }

        vibrate(secondsToGo);
        txtTime.setText(minutes + ":" + ((seconds >= 10) ? seconds : "0" + seconds));
    }

}
