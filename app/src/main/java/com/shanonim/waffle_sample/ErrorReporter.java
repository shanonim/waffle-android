package com.shanonim.waffle_sample;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

/**
 * Created by shinohara-hiromitsu on 2017/02/15.
 */

public class ErrorReporter implements Thread.UncaughtExceptionHandler {

    //※SDカードのルートに保存される。ルートは実機によって「/sdcard」や「/mnt/sdcard」など異なる。
    static final File errorReportFile = new File(Environment.getExternalStorageDirectory().getPath()
            + File.separator + "error_report.txt");    //※ファイル名は任意

    //コンストラクタ
    public ErrorReporter() {
    }

    //catch されなかった例外を受け取るハンドラ
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        PrintWriter pw = null;
        try {
            //※ここでエラーにならないように注意(要 SD カード[マウント])
            pw = new PrintWriter(new FileOutputStream(errorReportFile));
            ex.printStackTrace(pw);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pw.close();
            } catch (Exception e2) {
            }
            pw = null;
        }
    }
}
