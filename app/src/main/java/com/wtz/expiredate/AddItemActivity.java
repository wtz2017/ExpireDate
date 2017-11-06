package com.wtz.expiredate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.wtz.expiredate.data.GoodsItem;
import com.wtz.expiredate.utils.DatabaseHelper;
import com.wtz.expiredate.utils.DateTimePickDialogUtil;
import com.wtz.expiredate.utils.DateTimeUtil;
import com.wtz.expiredate.utils.FileUtil;
import com.wtz.expiredate.utils.ImagePicker;

import java.io.File;
import java.util.Date;

public class AddItemActivity extends Activity {
    private final static String TAG = AddItemActivity.class.getSimpleName();

    private EditText etName;
    private EditText etCount;
    private EditText etExpireDate;
    private ImageView ivIcon;
    private Button btnSetDate;
    private Button btnSetIcon;
    private Button btnSave;
    private Button btnDelete;
    private Button btnCancel;

    private String mInitIconPath;
    public final static String ITEM_ID_KEY = "item_id_key";
    private int mItemId = -1;
    public final static String OP_TYPE_KEY = "op_type_key";
    public final static int OP_TYPE_ADD = 0;
    public final static int OP_TYPE_UPDATE = 1;
    private int operateType = OP_TYPE_ADD;

    private String mPhotoPath;
    private Bitmap mGallaryBitmap;
    private final static int IMAGE_TYPE_PHOTO = 0;
    private final static int IMAGE_TYPE_GALLARY = 1;
    private int imageType = IMAGE_TYPE_PHOTO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        operateType = getIntent().getIntExtra(OP_TYPE_KEY, OP_TYPE_ADD);
        mItemId = getIntent().getIntExtra(ITEM_ID_KEY, -1);
        Log.d(TAG, "operateType=" + operateType + ", mItemId=" + mItemId);

        setContentView(R.layout.activity_add_item);

        etName = (EditText) this.findViewById(R.id.et_set_name);
        etCount = (EditText) this.findViewById(R.id.et_set_count);
        etExpireDate = (EditText) this.findViewById(R.id.et_set_date);
        ivIcon = (ImageView) this.findViewById(R.id.iv_icon);
        btnSetDate = (Button) this.findViewById(R.id.btn_set_date);
        btnSetIcon = (Button) this.findViewById(R.id.btn_set_icon);
        btnSave = (Button) this.findViewById(R.id.btn_save);
        btnDelete = (Button) this.findViewById(R.id.btn_delete);

        if (operateType == OP_TYPE_ADD) {
            btnSave.setText(R.string.add);
        } else {
            btnSave.setText(R.string.update);
            btnDelete.setVisibility(View.VISIBLE);

            GoodsItem item = new DatabaseHelper(this).queryItem(mItemId);
            if (item != null) {
                etName.setText(item.getName());
                etCount.setText(String.valueOf(item.getCount()));
                String dateStr = DateTimeUtil.getSpecifiedDateTime(new Date(item.getExpireDate()), this.getString(R.string.default_date_time_format));
                etExpireDate.setText(dateStr);
                if (!TextUtils.isEmpty(item.getIconPath())) {
                    mInitIconPath = item.getIconPath();
                    Picasso.with(this)
                            .load(new File(item.getIconPath()))
                            .placeholder(R.mipmap.unkown)
                            .into(ivIcon);
                }
            }
        }

        btnCancel = (Button) this.findViewById(R.id.btn_cancel);

        btnSetDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DateTimePickDialogUtil dateTimePicKDialog = new DateTimePickDialogUtil();
                dateTimePicKDialog.createDialog(etExpireDate);
            }
        });

        btnSetIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLogoDialog();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoodsItem item = saveRecord();
                switch (operateType) {
                    case OP_TYPE_ADD:
                        if (item != null) {
                            addRecord(item);
                            setResult(RESULT_OK);
                            finish();
                        }
                        return;
                    case OP_TYPE_UPDATE:
                        if (item != null) {
                            updateRecord(item);
                            setResult(RESULT_OK);
                            finish();
                        }
                        return;
                }

            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater lf = LayoutInflater.from(AddItemActivity.this);
                TextView tvInfo = (TextView) lf.inflate(R.layout.delete_dialog_content, null);
                tvInfo.setText("" + etName.getText());
                new AlertDialog.Builder(AddItemActivity.this)
                        .setTitle(getString(R.string.is_confirm_delete_record))
                        .setView(tvInfo)
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                deleteRecord(mItemId);
                                setResult(RESULT_OK);
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // do nothing
                            }
                        }).show();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
    }

    private void cancel() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void setLogoDialog() {
        final String iconPath = getIconSavePath();
        new AlertDialog.Builder(AddItemActivity.this)
                .setTitle("设置LOGO")
                .setNegativeButton("相册", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ImagePicker.pickFromGallery(AddItemActivity.this);
                    }
                })
                .setPositiveButton("拍照", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mPhotoPath = iconPath;
                        ImagePicker.pickByCamera(AddItemActivity.this, new File(iconPath));
                    }
                }).show();
    }

    private String getIconSavePath() {
        String subDir = this.getPackageName() + File.separator + "icon";
        File fullDir = FileUtil.getStorageDir(this, subDir);
        return fullDir.getAbsolutePath() + File.separator + String.valueOf(System.currentTimeMillis() + ".jpg");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult...requestCode=" + requestCode
                + ", resultCode=" + resultCode);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ImagePicker.ACTIVITY_REQUESTCODE_CAMERA:   // 调用相机拍照
                    imageType = IMAGE_TYPE_PHOTO;
                    Bitmap bmp = ImagePicker.getBitmapFromFile(mPhotoPath, 128, 128);
                    FileUtil.bitmapToFile(bmp, mPhotoPath, false);//缩小分辨率
                    ivIcon.setImageBitmap(bmp);
                    break;
                case ImagePicker.ACTIVITY_REQUESTCODE_GALLERY:  // 直接从相册获取
                    imageType = IMAGE_TYPE_GALLARY;
                    mGallaryBitmap = ImagePicker.getBitmapFromUri(data.getData(), this, 128, 128);
                    ivIcon.setImageBitmap(mGallaryBitmap);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private GoodsItem saveRecord() {
        if (etName == null || TextUtils.isEmpty(etName.getText())) {
            Log.d(TAG, "etName == NULL or getText == null");
            Toast.makeText(this, "名字不能为空", Toast.LENGTH_SHORT)
                    .show();
            return null;
        }
        if (etCount == null || TextUtils.isEmpty(etCount.getText())) {
            Log.d(TAG, "etCount == NULL or getText == null");
            Toast.makeText(this, "数量不能为空", Toast.LENGTH_SHORT)
                    .show();
            return null;
        }
        if (etExpireDate == null || TextUtils.isEmpty(etExpireDate.getText())) {
            Log.d(TAG, "etExpireDate == NULL or getText == null");
            Toast.makeText(this, "截止日期不能为空", Toast.LENGTH_SHORT)
                    .show();
            return null;
        }

        GoodsItem item = new GoodsItem();
        if (operateType == OP_TYPE_UPDATE) {
            item.setId(mItemId);
        }
        item.setName(String.valueOf(etName.getText()));
        item.setCount(Integer.valueOf(String.valueOf(etCount.getText())));

        String dateTimeString = String.valueOf(etExpireDate.getText());
        long timeMillis = DateTimeUtil.getDateTimeMillis(dateTimeString,
                this.getString(R.string.default_date_time_format));
        item.setExpireDate(timeMillis);

        if (imageType == IMAGE_TYPE_PHOTO) {
            if (!TextUtils.isEmpty(mPhotoPath)) {
                item.setIconPath(mPhotoPath);
            }
        } else {
            if (mGallaryBitmap != null) {
                String path = getIconSavePath();
                FileUtil.bitmapToFile(mGallaryBitmap, path, false);
                item.setIconPath(path);
            }
        }
        if (TextUtils.isEmpty(item.getIconPath())) {
            if (operateType == OP_TYPE_UPDATE && !TextUtils.isEmpty(mInitIconPath)) {
                item.setIconPath(mInitIconPath);
            }
        }

        return item;
    }

    private void addRecord(GoodsItem record) {
        new DatabaseHelper(this).insert(record);
    }

    private void deleteRecord(int id) {
        new DatabaseHelper(this).deleteById(id);
    }

    private void updateRecord(GoodsItem record) {
        new DatabaseHelper(this).update(record);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown...keyCode=" + keyCode);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            cancel();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
