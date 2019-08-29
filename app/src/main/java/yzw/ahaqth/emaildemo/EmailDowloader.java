package yzw.ahaqth.emaildemo;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;

final class EmailDowloader extends AsyncTask<Void, Integer, Void> {
    @Override
    protected Void doInBackground(Void... voids) {
        publishProgress(0, 0);
        try {
            readEmail();
            isSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
            message = e.getMessage();
            isSuccess = false;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int flag = values[0];
        int value = values[1];
        switch (flag) {
            case 0:
                dowloadDialogFragment.setTitle("正在读取邮件...");
                break;
            case 1:
                dowloadDialogFragment.setTitle("准备下载...");
                break;
            case 2:
                dowloadDialogFragment.setTitle("正在下载...");
                break;
            case 3:
                dowloadDialogFragment.setTitle("准备下载...");
                dowloadDialogFragment.setTotalFileSize(values[2]);
                break;
        }
        dowloadDialogFragment.changeMessage(value);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        dowloadDialogFragment.dismiss();
        onDowloadFinish.onFinish(isSuccess, message);
    }

    public interface OnDowloadFinish {
        void onFinish(boolean isSuccess, String message);
    }

    private String TAG = "yinzongwang";
    private DowloadDialogFragment dowloadDialogFragment;
    private OnDowloadFinish onDowloadFinish;
    private List<File> files;
    private boolean isSuccess;
    private String dir, message;

    public void setOnDowloadFinish(OnDowloadFinish onDowloadFinish) {
        this.onDowloadFinish = onDowloadFinish;
    }

    EmailDowloader(String dir, DowloadDialogFragment dowloadDialogFragment, List<File> files) {
        this.dir = dir;
        this.dowloadDialogFragment = dowloadDialogFragment;
        this.files = files;
        this.message = "";
    }

    private void readEmail() throws Exception {
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "pop3"); // 协议
        props.setProperty("mail.pop3.port", "110"); // 端口
        props.setProperty("mail.pop3.host", "pop3.163.com"); // pop3服务器
        Session session = Session.getInstance(props);
        Store store = session.getStore("pop3");
        store.connect("specialrecorder@163.com", "Erke4187657");
        Folder folder = store.getFolder("INBOX");
        folder.open(Folder.READ_ONLY);//打开收件箱
        Message[] messages = folder.getMessages();
        for (int i = 0, count = messages.length; i < count; i++) {
            MimeMessage msg = (MimeMessage) messages[i];
            String title = ReceiveEmail.getSubject(msg);
            int index = title.lastIndexOf("r") + 1;
            String from = ReceiveEmail.getFrom(msg);
            if (from.contains("yinzongwang") && title.contains("specialrecorder")) {
                publishProgress(1, 0);
                downloadFile(msg);
                StringBuilder builder = new StringBuilder();
                ReceiveEmail.getMailTextContent(msg,builder);
                message = "主题：" + title + "发件人：" + from+builder.toString();
            }
        }
        //释放资源
        folder.close(true);
        store.close();
    }

    private void downloadFile(MimeMessage msg) throws Exception {
        Multipart multipart = (Multipart) msg.getContent();
        int downloadCount = 0;
        BodyPart bodyPart = multipart.getBodyPart(1);
        //某一个邮件体也有可能是由多个邮件体组成的复杂体
        String disp = bodyPart.getDisposition();
        if (disp != null && (disp.equalsIgnoreCase(Part.ATTACHMENT) || disp.equalsIgnoreCase(Part.INLINE))) {
            InputStream is = bodyPart.getInputStream();
            int size = bodyPart.getSize();
            publishProgress(3, 0, size);
            File file = new File(dir, ReceiveEmail.decodeText(bodyPart.getFileName()));
            if (file.exists())
                file.delete();
            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(file));

            byte[] b = new byte[1024];
            int len;
            while ((len = is.read(b)) != -1) {
                bos.write(b, 0, len);
                downloadCount += len;
                publishProgress(2, downloadCount);
                bos.flush();
            }
            files.add(file);
            bos.close();
        }
    }

    private void saveFile(InputStream is, String destDir, String fileName, List<File> fileList)
            throws FileNotFoundException, IOException {
        File file = new File(destDir, fileName);
        BufferedInputStream bis = new BufferedInputStream(is);
//        BufferedOutputStream bos = new BufferedOutputStream(
//                new FileOutputStream(file));
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        byte[] buffer = new byte[1024 * 8];
        int len;
        int count = 0;
        while ((len = bis.read(buffer)) != -1) {
            fileOutputStream.write(buffer);
            count += len;
            publishProgress(2, count);
        }
        fileList.add(file);
        fileOutputStream.close();
        bis.close();
    }
}
