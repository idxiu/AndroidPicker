package com.github.cqrframe.toolkit;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

/**
 * Receive the activity result directly after the startActivityForResult
 * See https://github.com/florent37/InlineActivityResult
 * <p>
 * Created by florentchampigny on 09/11/2018
 *
 * @see OnActivityResultListener
 */
public class CqrActivityResult {
    private static final String TAG = "cqr_result_fragment";
    private static final int REQUEST_CODE = 200;

    public static void start(FragmentActivity activity, @NonNull final Intent intent, OnActivityResultListener listener) {
        final FragmentManager fragmentManager = activity.getSupportFragmentManager();
        InternalFragment oldFragment = (InternalFragment) fragmentManager.findFragmentByTag(TAG);
        if (oldFragment != null) {
            oldFragment.setResultListener(listener);
            oldFragment.startActivityForResult(intent, REQUEST_CODE);
        } else {
            final InternalFragment newFragment = InternalFragment.newInstance(intent);
            newFragment.setResultListener(listener);
            //在主线程执行：fragmentManager.executePendingTransactions();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fragmentManager.beginTransaction().add(newFragment, TAG).commitNowAllowingStateLoss();
                    newFragment.startActivityForResult(intent, REQUEST_CODE);
                }
            });
        }
    }

    public static void startAppDetailsSettings(FragmentActivity activity, OnActivityResultListener listener) {
        String packageName = activity.getApplicationContext().getPackageName();
        start(activity, new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null)), listener);
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static void startAppUnknownSources(FragmentActivity activity, OnActivityResultListener listener) {
        String packageName = activity.getApplicationContext().getPackageName();
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + packageName));
        start(activity, intent, listener);
    }

    public static void startImageCapture(FragmentActivity activity, OnActivityResultListener listener) {
        start(activity, new Intent(MediaStore.ACTION_IMAGE_CAPTURE), listener);
    }

    public static void startImageChoose(FragmentActivity activity, OnActivityResultListener listener) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        start(activity, intent, listener);
    }

    public interface OnActivityResultListener {
        void onActivityResult(int requestCode, int resultCode, Intent data);
    }

    /**
     * DO NOT USE THIS FRAGMENT DIRECTLY!
     * It's only here because fragments have to be public
     */
    public static class InternalFragment extends Fragment {
        private static final String INTENT_KEY_TO_START = "intent_key_to_start";
        @Nullable
        private OnActivityResultListener listener;
        @Nullable
        private Intent intent;

        public InternalFragment() {
            setRetainInstance(true);
        }

        public static InternalFragment newInstance(@NonNull Intent intent) {
            Bundle args = new Bundle();
            args.putParcelable(INTENT_KEY_TO_START, intent);
            InternalFragment fragment = new InternalFragment();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle arguments = getArguments();
            if (arguments != null) {
                intent = arguments.getParcelable(INTENT_KEY_TO_START);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            if (intent != null) {
                startActivityForResult(intent, REQUEST_CODE);
            } else {
                // this shouldn't happen, but just to be sure
                requireFragmentManager().beginTransaction().remove(this).commitAllowingStateLoss();
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (listener == null || requestCode != REQUEST_CODE) {
                return;
            }
            listener.onActivityResult(requestCode, resultCode, data);
            requireFragmentManager().beginTransaction().remove(this).commitAllowingStateLoss();
        }

        public void setResultListener(@Nullable OnActivityResultListener listener) {
            if (listener != null) {
                this.listener = listener;
            }
        }

    }

}
