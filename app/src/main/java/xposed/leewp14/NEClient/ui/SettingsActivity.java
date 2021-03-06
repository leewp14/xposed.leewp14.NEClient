package xposed.leewp14.NEClient.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.io.File;

import xposed.leewp14.NEClient.BuildConfig;
import xposed.leewp14.NEClient.R;
import xposed.leewp14.NEClient.Utility;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    private int isModuleActive() {
        return 0;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWorldReadable();
        addPreferencesFromResource(R.xml.pref_general);

        checkState();
        checkIcon();
    }


    private void checkState() {
        if (isModuleActive() == 0)
            showNotActive();
    }


    private void showNotActive() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage(R.string.hint_reboot_not_active)
                .setPositiveButton(R.string.active_now, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        openXposed();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();

    }

    private void openXposed() {
        if (Utility.isAppInstalled(this, "de.robv.android.xposed.installer")) {
            Intent intent = new Intent("de.robv.android.xposed.installer.OPEN_SECTION");
            if (getPackageManager().queryIntentActivities(intent, 0).isEmpty()) {
                intent = getPackageManager().getLaunchIntentForPackage("de.robv.android.xposed.installer");
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra("section", "modules")
                    .putExtra("fragment", 1)
                    .putExtra("module", BuildConfig.APPLICATION_ID);
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.xposed_installer_not_installed, Toast.LENGTH_SHORT).show();
        }
    }

    private void checkIcon() {
        if (Utility.isAppInstalled(this, "de.robv.android.xposed.installer")) {
            final ComponentName aliasName = new ComponentName(this, SettingsActivity.this.getClass().getName() + "-Alias");
            final PackageManager packageManager = getPackageManager();
            if (packageManager.getComponentEnabledSetting(aliasName) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setMessage(R.string.hint_hide_icon)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                packageManager.setComponentEnabledSetting(
                                        aliasName,
                                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                        PackageManager.DONT_KILL_APP);
                            }
                        }).show();
            }
        }
    }


    @SuppressWarnings({"deprecation", "ResultOfMethodCallIgnored"})
    @SuppressLint("SetWorldReadable")
    private void setWorldReadable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            File dataDir = new File(getApplicationInfo().dataDir);
            File prefsDir = new File(dataDir, "shared_prefs");
            File prefsFile = new File(prefsDir, getPreferenceManager().getSharedPreferencesName() + ".xml");
            if (prefsFile.exists()) {
                for (File file : new File[]{dataDir, prefsDir, prefsFile}) {
                    file.setReadable(true, false);
                    file.setExecutable(true, false);
                }
            }
        } else {
            getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Toast.makeText(this, R.string.hint_reboot_setting_changed, Toast.LENGTH_SHORT).show();
    }
}
