package eu.toldi.infinityforlemmy.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.WindowManager;

import eu.toldi.infinityforlemmy.R;

public class SplashScreenActivity extends Activity {

    private static final long SPLASH_SCREEN_DELAY = 500; // Splash Screen 표시 시간 (2초)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Splash Screen 표시 후 다이얼로그 띄우기
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showSettingsDialog();
            }
        }, SPLASH_SCREEN_DELAY);
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ANR 설정");
        builder.setMessage(" \"예\" 선택시 초기 ANR 발생 후 종료");
        builder.setCancelable(false);

        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 예 버튼 처리
                // 설정값을 설정요고 이후 처리 로직 작성

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            // 무한 루프를 실행하여 메인 스레드를 차단
                        }
                    }
                });
//                try {
//                    Thread.sleep(15000);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//                    finishAffinity();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 아니오 버튼 처리
                // 설정값을 설정하지 않고 이후 처리 로직 작성
                startMainActivity();
                dialog.dismiss(); // 다이얼로그 닫기
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                // 다이얼로그 위치를 중앙으로 설정
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(dialog.getWindow().getAttributes());
                layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                layoutParams.gravity = Gravity.CENTER;
                dialog.getWindow().setAttributes(layoutParams);
            }
        });
        dialog.show();
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish(); // Splash Screen 액티비티 종료
    }
}
