package sun.geoffery.uploadpic;

import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/**
 * All rights Reserved, Designed By GeofferySun 
 * @Title: 	MainActivity.java 
 * @Package sun.geoffery.uploadpic 
 * @Description:��ҳ��
 * @author:	GeofferySun   
 * @date:	2015��1��15�� ����12:24:13 
 * @version	V1.0
 */
public class MainActivity extends Activity implements OnClickListener {
	private Context mContext;
	private CircleImg avatarImg;// ͷ��ͼƬ
	private Button loginBtn;// ҳ��ĵ�¼��ť
	private SelectPicPopupWindow menuWindow; // �Զ����ͷ��༭������
	// �ϴ���������·����һ�㲻Ӳ���뵽�����С�
	private String imgUrl = "";
    private static final String IMAGE_FILE_NAME = "avatarImage.jpg";// ͷ���ļ�����
    private String urlpath;			// ͼƬ����·��
    private String resultStr = "";	// ����˷��ؽ����
    private static ProgressDialog pd;// �ȴ�����Ȧ
    private static final int REQUESTCODE_PICK = 0;		// ���ѡͼ���
    private static final int REQUESTCODE_TAKE = 1;		// ������ձ��
    private static final int REQUESTCODE_CUTTING = 2;	// ͼƬ���б��

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mContext = MainActivity.this;
		
		initViews();
	}
	
	/**
	 * ��ʼ��ҳ��ؼ�
	 */
	private void initViews() {
		avatarImg = (CircleImg) findViewById(R.id.avatarImg);
		loginBtn = (Button) findViewById(R.id.loginBtn);
		
		avatarImg.setOnClickListener(this);
		loginBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.avatarImg:// ����ͷ�����¼�
			menuWindow = new SelectPicPopupWindow(mContext, itemsOnClick);  
			menuWindow.showAtLocation(findViewById(R.id.mainLayout), 
					Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0); 
			break;
		case R.id.loginBtn://��¼��ť��ת�¼�
			startActivity(new Intent(mContext, UploadActivity.class));
			break;

		default:
			break;
		}
	}
	
	//Ϊ��������ʵ�ּ�����  
	private OnClickListener itemsOnClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			menuWindow.dismiss();
			switch (v.getId()) {
			// ����
			case R.id.takePhotoBtn:
				Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				//�������ָ������������պ����Ƭ�洢��·��
				takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, 
						Uri.fromFile(new File(Environment.getExternalStorageDirectory(), IMAGE_FILE_NAME)));
				startActivityForResult(takeIntent, REQUESTCODE_TAKE);
				break;
			// ���ѡ��ͼƬ
			case R.id.pickPhotoBtn:
				Intent pickIntent = new Intent(Intent.ACTION_PICK, null);
				// ���������Ҫ�����ϴ�����������ͼƬ����ʱ����ֱ��д�磺"image/jpeg �� image/png�ȵ�����"
				pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
				startActivityForResult(pickIntent, REQUESTCODE_PICK);
				break;
			default:
				break;
			}
		}
	}; 
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		switch (requestCode) {
		case REQUESTCODE_PICK:// ֱ�Ӵ�����ȡ
			try {
				startPhotoZoom(data.getData());
			} catch (NullPointerException e) {
				e.printStackTrace();// �û����ȡ������
			}
			break;
		case REQUESTCODE_TAKE:// �����������
			File temp = new File(Environment.getExternalStorageDirectory() + "/" + IMAGE_FILE_NAME);
			startPhotoZoom(Uri.fromFile(temp));
			break;
		case REQUESTCODE_CUTTING:// ȡ�òü����ͼƬ
			if (data != null) {
				setPicToView(data);
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
		
	/**
	 * �ü�ͼƬ����ʵ��
	 * @param uri
	 */
	public void startPhotoZoom(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// crop=true�������ڿ�����Intent��������ʾ��VIEW�ɲü�
		intent.putExtra("crop", "true");
		// aspectX aspectY �ǿ�ߵı���
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY �ǲü�ͼƬ���
		intent.putExtra("outputX", 300);
		intent.putExtra("outputY", 300);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, REQUESTCODE_CUTTING);
	}
		
	/**
	 * ����ü�֮���ͼƬ����
	 * @param picdata
	 */
	private void setPicToView(Intent picdata) {
		Bundle extras = picdata.getExtras();
		if (extras != null) {
			// ȡ��SDCardͼƬ·������ʾ
			Bitmap photo = extras.getParcelable("data");
			Drawable drawable = new BitmapDrawable(null, photo);
			urlpath = FileUtil.saveFile(mContext, "temphead.jpg", photo);
			avatarImg.setImageDrawable(drawable);

			// ���̺߳�̨�ϴ������
			pd = ProgressDialog.show(mContext, null, "�����ϴ�ͼƬ�����Ժ�...");
			new Thread(uploadImageRunnable).start();
		}
	}

	/**
	 * ʹ��HttpUrlConnectionģ��post�������ļ�
	 * �ϴ�ƽʱ����ʹ�ã��Ƚ��鷳
	 * ԭ���ǣ� �����ļ��ϴ������ݸ�ʽ��Ȼ����ݸ�ʽ������Ӧ�ķ��͸����������ַ�����
	 */
	Runnable uploadImageRunnable = new Runnable() {
		@Override
		public void run() {
			
			if(TextUtils.isEmpty(imgUrl)){
				Toast.makeText(mContext, "��û�������ϴ���������·����", Toast.LENGTH_SHORT).show();
				return;
			}
			
			Map<String, String> textParams = new HashMap<String, String>();
			Map<String, File> fileparams = new HashMap<String, File>();
			
			try {
				// ����һ��URL����
				URL url = new URL(imgUrl);
				textParams = new HashMap<String, String>();
				fileparams = new HashMap<String, File>();
				// Ҫ�ϴ���ͼƬ�ļ�
				File file = new File(urlpath);
				fileparams.put("image", file);
				// ����HttpURLConnection����������л�ȡ��ҳ����
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				// �������ӳ�ʱ���ǵ��������ӳ�ʱ,������粻��,Androidϵͳ�ڳ���Ĭ��ʱ����ջ���Դ�жϲ�����
				conn.setConnectTimeout(5000);
				// �����������������POST��������������������
				conn.setDoOutput(true);
				// ����ʹ��POST�ķ�ʽ����
				conn.setRequestMethod("POST");
				// ���ò�ʹ�û��棨���׳������⣩
				conn.setUseCaches(false);
				conn.setRequestProperty("Charset", "UTF-8");//���ñ���   
				// �ڿ�ʼ��HttpURLConnection�����setRequestProperty()����,��������HTML�ļ�ͷ
				conn.setRequestProperty("ser-Agent", "Fiddler");
				// ����contentType
				conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + NetUtil.BOUNDARY);
				OutputStream os = conn.getOutputStream();
				DataOutputStream ds = new DataOutputStream(os);
				NetUtil.writeStringParams(textParams, ds);
				NetUtil.writeFileParams(fileparams, ds);
				NetUtil.paramsEnd(ds);
				// ���ļ���������,Ҫ�ǵü�ʱ�ر�
				os.close();
				// ���������ص���Ӧ��
				int code = conn.getResponseCode(); // ��Internet��ȡ��ҳ,��������,����ҳ��������ʽ������
				// ����Ӧ������ж�
				if (code == 200) {// ���ص���Ӧ��200,�ǳɹ�
					// �õ����緵�ص�������
					InputStream is = conn.getInputStream();
					resultStr = NetUtil.readString(is);
				} else {
					Toast.makeText(mContext, "����URLʧ�ܣ�", Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			handler.sendEmptyMessage(0);// ִ�к�ʱ�ķ���֮��������handler
		}
	};
	
	Handler handler = new Handler(new Handler.Callback() {
		
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				pd.dismiss();
				
				try {
					// ��������ʾ������������ͺ�̨��������
					// {"status":"1","statusMessage":"�ϴ��ɹ�","imageUrl":"http://120.24.219.49/726287_temphead.jpg"}
					JSONObject jsonObject = new JSONObject(resultStr);
					
					// ��������ַ�����1����Ϊ�����ɹ����
					if (jsonObject.optString("status").equals("1")) {
						BitmapFactory.Options option = new BitmapFactory.Options();
						// ѹ��ͼƬ:��ʾ����ͼ��СΪԭʼͼƬ��С�ļ���֮һ��1Ϊԭͼ��3Ϊ����֮һ
						option.inSampleSize = 1;
						
						// ����˷��ص�JsonObject��������ȡ��ͼƬ������URL·��
						String imageUrl = jsonObject.optString("imageUrl");
						Toast.makeText(mContext, imageUrl, Toast.LENGTH_SHORT).show();
					}else{
						Toast.makeText(mContext, jsonObject.optString("statusMessage"), Toast.LENGTH_SHORT).show();
					}
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				break;
				
			default:
				break;
			}
			return false;
		}
	});
}
