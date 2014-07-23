package com.score.senzors.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import com.score.senzors.R;

/**
 * Created by eranga on 6/6/14.
 */
public class SplashActivity extends Activity {
    private final int SPLASH_DISPLAY_LENGTH = 3000;

    /** Called when the activity is first created */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.splash_layout);

        Typeface typefaceThin = Typeface.createFromAsset(getAssets(), "fonts/vegur_2.otf");
        TextView appName = (TextView) findViewById(R.id.splash_text);
        appName.setTypeface(typefaceThin, Typeface.BOLD);

        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // determine where to go
                // check user registration status
//                if(PreferenceUtils.haveRegisteredUser(SplashActivity.this)) {
//                    Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    SplashActivity.this.startActivity(intent);
//                    SplashActivity.this.finish();
//                } else {
//                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    SplashActivity.this.startActivity(intent);
//                    SplashActivity.this.finish();
//                }
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                SplashActivity.this.startActivity(intent);
                SplashActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}