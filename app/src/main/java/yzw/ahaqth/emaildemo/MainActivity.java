package yzw.ahaqth.emaildemo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

public class MainActivity extends AppCompatActivity {
    private String TAG = "MainActivitySendEmail";
    private EditText emailSubjectET;
    private EditText emailContentET;
    private EditText toEmailDress;
    private Button sendTextEmail;
    private Button sendMultiEmail;
    private Button receiveEmail;
    private TextView receivedText;
    private String emailContent;
    private String emailSubject;
    private String fileName;
    private String toAdress;
    // 邮件发送协议
    private final static String PROTOCOL = "smtp";
    //SMTP邮件服务器
    private final static String HOST = "smtp.163.com";
    //SMTP邮件服务器默认端口
    private final static String PORT = "25";
    //是否要求身份验证
    private final static String IS_AUTH = "true";
    // 是否启用调试模式（启用调试模式可打印客户端与服务器交互过程时一问一答的响应消息）  
    private final static String IS_ENABLED_DEBUG_MOD = "true";
    // 发件人  
    private static String from = "specialrecorder@163.com";

    //收件人
    private static String to = "yinzongwang@163.com";

    //初始化连接邮件服务器的会话信息 
    private static Properties props = null;

    private void initialProps() {
        props = new Properties();
        props.setProperty("mail.transport.protocol", PROTOCOL);
        props.setProperty("mail.smtp.host", HOST);
        props.setProperty("mail.smtp.port", PORT);
        props.setProperty("mail.smtp.auth", IS_AUTH);
        props.setProperty("mail.debug", "false");
    }

    private void showToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            if (msg.what == 123)
                showToast("success!");
            else if (msg.what == 456) {
                Bundle data = msg.getData();
                String val = data.getString("value");
                showToast(val);
            } else if (msg.what == 789) {
                Bundle data = msg.getData();
                String val = data.getString("value");
                receivedText.setText(val);
            }
        }
    };

    private void initialView() {
        emailSubjectET = findViewById(R.id.emialSubject);
        emailContentET = findViewById(R.id.emailContent);
        toEmailDress = findViewById(R.id.toEmailDress);
        sendTextEmail = findViewById(R.id.sendTextEmail);
        sendTextEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            sendTextEmailClick();
                            handler.sendEmptyMessage(123);
                        } catch (MessagingException e) {
                            Log.d(TAG, "run: " + e.getMessage());
                            android.os.Message msg = new android.os.Message();
                            Bundle data = new Bundle();
                            data.putString("value", e.getMessage());
                            msg.setData(data);
                            handler.sendMessage(msg);
                        }
                    }
                }).start();
            }
        });
        sendMultiEmail = findViewById(R.id.sendMultiEmail);
        sendMultiEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            sendSendMultiEmailClick();
                            handler.sendEmptyMessage(123);
                        } catch (MessagingException | IOException e) {
                            Log.d(TAG, "run: " + e.getMessage());
                            android.os.Message msg = new android.os.Message();
                            Bundle data = new Bundle();
                            data.putString("value", e.getMessage());
                            msg.setData(data);
                            msg.what = 456;
                            handler.sendMessage(msg);
                        }
                    }
                }).start();
            }
        });
        receiveEmail = findViewById(R.id.receiveEmailBT);
        receiveEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(NetWorkTest.isNetworkConnected(MainActivity.this)){
                    final List<File> files = new ArrayList<>();
                    DowloadDialogFragment dowloadDialogFragment = DowloadDialogFragment.newInstant();
                    EmailDowloader emailDowloader = new EmailDowloader(getCacheDir().getAbsolutePath(),dowloadDialogFragment,files);
                    emailDowloader.setOnDowloadFinish(new EmailDowloader.OnDowloadFinish() {
                        @Override
                        public void onFinish(boolean isSuccess,String message) {
                            if(isSuccess){
                                String s = files.get(0).getName()+"\n"+message;
                                receivedText.setText(s);
                                showToast("下载成功！");
                            }else{
                                receivedText.setText(message);
                                showToast("下载失败！");
                            }
                        }
                    });
                    dowloadDialogFragment.show(getSupportFragmentManager(),"dowsloading");
                    emailDowloader.execute();
                }else
                    showToast("no network");

//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        String s = "";
//                        try {
//                            s = receiveEmail();
//                        } catch (Exception e) {
//                            Log.d(TAG, "run: " + e.getMessage());
//                            s = e.getMessage();
//                        } finally {
//                            android.os.Message msg = new android.os.Message();
//                            msg.what = 789;
//                            Bundle data = new Bundle();
//                            data.putString("value", s);
//                            msg.setData(data);
//                            handler.sendMessage(msg);
//                        }
//                    }
//                }).start();
            }
        });
        receivedText = findViewById(R.id.receivedText);
    }

    private File getTxtFile() {
        fileName = "filesendat" + System.currentTimeMillis() + ".txt";
        File file = new File(getCacheDir(), fileName);
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(emailContent.getBytes());
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (bos != null)
                    bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    private boolean isEmptyInput() {
        if (TextUtils.isEmpty(emailSubjectET.getText()) || TextUtils.isEmpty(emailContentET.getText()) || TextUtils.isEmpty(toEmailDress.getText()))
            return true;
        emailSubject = emailSubjectET.getText().toString();
        emailContent = emailContentET.getText().toString();
        toAdress = toEmailDress.getText().toString();
        return false;
    }

    private void sendTextEmailClick() throws MessagingException {
        if (isEmptyInput())
            return;

        // 创建Session实例对象  
        Session session = Session.getDefaultInstance(props);
        // 创建MimeMessage实例对象  
        MimeMessage message = new MimeMessage(session);
        // 设置发件人  
        message.setFrom(new InternetAddress(from));
        //设置邮件主题  
        message.setSubject(emailSubject);
        // 设置收件人
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        // 设置发送时间
        message.setSentDate(new Date());
        // 设置纯文本内容为邮件正文
        message.setText(emailContent);
        // 保存并生成最终的邮件内容 
        message.saveChanges();
        // 获得Transport实例对象 
        Transport transport = session.getTransport();
        // 打开连接 
        transport.connect("specialrecorder@163.com", "Erke4187657");
        // 将message对象传递给transport对象，将邮件发送出去 
        transport.sendMessage(message, message.getAllRecipients());
        // 关闭连接 
        transport.close();
    }

    private void sendSendMultiEmailClick() throws MessagingException, IOException {
        if (isEmptyInput())
            return;
        File file = getTxtFile();
        String charset = "utf-8"; // 指定中文编码格式 
        Session session = Session.getInstance(props, new MyAuthenticator());
        // 创建MimeMessage实例对象
        MimeMessage message = new MimeMessage(session);
        // 设置主题 
        message.setSubject(emailSubject);
        // 设置发送人
        message.setFrom(new InternetAddress(from, "163测试邮箱", charset));
        // 设置收件人  
        message.setRecipient(Message.RecipientType.TO, new InternetAddress("yinzongwang@163.com", "yzw", charset));
        // 设置抄送 
        // message.setRecipient(RecipientType.CC, new InternetAddress("xyang0917@gmail.com","王五_gmail",charset));  
        // 设置密送 
        // message.setRecipient(RecipientType.BCC, new InternetAddress("xyang0917@qq.com", "赵六_QQ", charset));  
        // 设置回复人(收件人回复此邮件时,默认收件人) 
        // message.setReplyTo(InternetAddress.parse("\"" + MimeUtility.encodeText("田七") + "\" <417067629@qq.com>")); 
        //  设置优先级(1:紧急   3:普通    5:低) 
        // message.setHeader("X-Priority", "1"); 
        //  要求阅读回执(收件人阅读邮件时会提示回复发件人,表明邮件已收到,并已阅读) 
        // message.setHeader("Disposition-Notification-To", from); 
        //创建一个消息体
        MimeBodyPart msgBodyPart = new MimeBodyPart();
        // 设置纯文本内容为邮件正文
        msgBodyPart.setText(emailContent);

        //创建Multipart增加其他的parts
        Multipart mp = new MimeMultipart();
        mp.addBodyPart(msgBodyPart);

        //创建文件附件
        MimeBodyPart fileBodyPart = new MimeBodyPart();
        fileBodyPart.attachFile(file);
        mp.addBodyPart(fileBodyPart);
//        for (String path : paths) {
//            MimeBodyPart fileBodyPart = new MimeBodyPart();
//            fileBodyPart.attachFile(path);
//            mp.addBodyPart(fileBodyPart);
//        }

        //增加Multipart到消息体中
        message.setContent(mp);
        //设置日期
        message.setSentDate(new Date());
        //设置附件格式
        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
        CommandMap.setDefaultCommandMap(mc);
        //发送消息
        Transport.send(message);
    }



    private String receiveEmail() {
        StringBuilder builder = new StringBuilder();
        builder.append("邮件信息：");
        try {
            // 准备连接服务器的会话信息
            Properties props = new Properties();
            props.setProperty("mail.store.protocol", "pop3"); // 协议
            props.setProperty("mail.pop3.port", "110"); // 端口
            props.setProperty("mail.pop3.host", "pop3.163.com"); // pop3服务器
//            props.setProperty("mail.store.protocol", "imap"); // 协议
//            props.setProperty("mail.imap.host", "143"); // 端口
//            props.setProperty("mail.imap.host", "imap.163.com"); // pop3服务器
            // 创建Session实例对象
            Session session = Session.getInstance(props);
            Store store = session.getStore("pop3");
//            Store store = session.getStore("imap");
            store.connect("specialrecorder@163.com", "Erke4187657");


//            // 获得收件箱
            Folder folder = store.getFolder("INBOX");
//            /* Folder.READ_ONLY：只读权限
//             * Folder.READ_WRITE：可读可写（可以修改邮件的状态）
//             */
            folder.open(Folder.READ_ONLY);//打开收件箱


//            // 由于POP3协议无法获知邮件的状态,所以getUnreadMessageCount得到的是收件箱的邮件总数
//            // folder.getUnreadMessageCount();
//            // 由于POP3协议无法获知邮件的状态,所以下面得到的结果始终都是为0
//            // folder.getDeletedMessageCount();
//            // 获得收件箱中的邮件总数
//            // folder.getMessageCount();
//
            // 得到收件箱中的所有邮件,并解析
            Message[] messages = folder.getMessages();

            // 解析所有邮件
            for (int i = 0, count = messages.length; i < count; i++) {
                MimeMessage msg = (MimeMessage) messages[i];
                //  msg.getMessageNumber();//第几封邮件，序号
                String title = ReceiveEmail.getSubject(msg);
                String from = ReceiveEmail.getFrom(msg);
                if (from.contains("yinzongwang") && title.contains("specialrecorder")) {
                    builder.append("\n主题：");
                    builder.append(title); // 主题
                    builder.append("\n发件人：");
                    builder.append(from);
                    builder.append("\n收件人：");
                    builder.append(ReceiveEmail.getReceiveAddress(msg, null));
                    builder.append("\n时间：");
                    builder.append(ReceiveEmail.getSentDate(msg, "yyyyMMdd"));
                    builder.append("\n内容：");
                    StringBuilder content = new StringBuilder(30);
                    ReceiveEmail.getMailTextContent(msg, content); // 邮件正文
                    builder.append(content.toString());
                    List<File> fileList = new ArrayList<>();
                    ReceiveEmail.saveAttachment(msg,getCacheDir().getAbsolutePath(),fileList);
                    for(File file:fileList){
                        builder.append(file.getName());
                        FileInputStream fileInputStream = new FileInputStream(file);
                        byte[] buffer = new byte[1024 * 8];
                        while ((fileInputStream.read(buffer))!=-1){
                            builder.append(new String(buffer));
                        }
                    }
//                    for(File file:fileList){
//                        if(file.isFile() && file.getName().endsWith(".txt")){
//                            FileInputStream fileInputStream = new FileInputStream(file);
//                            byte[] buffer = new byte[1024 * 8];
//                            int len = -1;
//                            while ((len = fileInputStream.read(buffer))!=-1){
//                                builder.append(new String(buffer));
//                            }
//                        }
//                    }
                }
            }
            //释放资源
            folder.close(true);
            store.close();
        } catch (Exception e) {
            e.printStackTrace();
//            if (builder.length() > 0)
//                builder.delete(0,builder.length() - 1);
            builder.append(e.getMessage());
        }
        return builder.toString();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialProps();
        emailContent = "";
        emailSubject = "";
        toAdress = "";
        initialView();
    }

    /**
     *  
     *  向邮件服务器提交认证信息 
     */
    static class MyAuthenticator extends Authenticator {
        private String username = "specialrecorder@163.com";
        private String password = "Erke4187657";

        MyAuthenticator() {
            super();
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password);
        }
    }


}
