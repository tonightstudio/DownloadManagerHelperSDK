package group.tonight.downloadmanagerhelpersdk;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import group.tonight.downloadmanagerhelper.DownloadManagerHelper;

public class MainActivity extends AppCompatActivity {

    private DownloadManagerHelper mDownloadManagerHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDownloadManagerHelper = new DownloadManagerHelper(this);
    }

    public void update(View view) {
        mDownloadManagerHelper.enqueue("http://192.168.0.226/app-release.apk");
    }
}
