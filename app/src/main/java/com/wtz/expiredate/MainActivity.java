package com.wtz.expiredate;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.wtz.expiredate.adapter.ListAdapter;
import com.wtz.expiredate.data.GoodsItem;
import com.wtz.expiredate.utils.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {
    private final static String TAG = MainActivity.class.getSimpleName();

    private ImageView ivAdd;
    private ListView lvList;

    private ListAdapter mListAdapter;
    private DatabaseHelper mDatabaseHelper;

    private String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private List<String> mPermissionList = new ArrayList<>();
    private final static int REQUEST_PERMISSIONS_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 23) {
            judgePermission();
        }

        ivAdd = (ImageView) this.findViewById(R.id.iv_add);
        ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
                intent.putExtra(AddItemActivity.OP_TYPE_KEY, AddItemActivity.OP_TYPE_ADD);
                startActivityForResult(intent, 0);
            }
        });

        lvList = (ListView) this.findViewById(R.id.lv_list);
        lvList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick...position=" + position);
                GoodsItem item = (GoodsItem) parent.getItemAtPosition(position);
                Log.d(TAG, "onItemClick...item.getId=" + item.getId());
                Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
                intent.putExtra(AddItemActivity.OP_TYPE_KEY, AddItemActivity.OP_TYPE_UPDATE);
                intent.putExtra(AddItemActivity.ITEM_ID_KEY, item.getId());
                startActivityForResult(intent, 0);
            }
        });
        mListAdapter = new ListAdapter(MainActivity.this, null);
        lvList.setAdapter(mListAdapter);

        updateListView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult...requestCode=" + requestCode
                + ", resultCode=" + resultCode + ", data=" + data);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            updateListView();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void judgePermission() {
        mPermissionList.clear();
        for (int i = 0; i < permissions.length; i++) {
            if (checkSelfPermission(permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);
            }
        }
        if (mPermissionList.isEmpty()) {//未授予的权限为空，表示都授予了
            // TODO: 2017/10/9
        } else {//请求权限方法
            String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);//将List转为数组
            this.requestPermissions(permissions, REQUEST_PERMISSIONS_CODE);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSIONS_CODE:
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        //判断是否勾选禁止后不再询问
                        boolean showRequestPermission = this.shouldShowRequestPermissionRationale(permissions[i]);
                        if (showRequestPermission) {//
//                            judgePermission();//重新申请权限
//                            return;
                        } else {
                            //已经禁止
                        }
                    }
                }
                // TODO: 2017/9/29 Do something
                break;
            default:
                break;
        }
    }

    private void updateListView() {
        if (mDatabaseHelper == null) {
            mDatabaseHelper = new DatabaseHelper(MainActivity.this);
        }
        List<GoodsItem> list =  mDatabaseHelper.queryList();

        if (mListAdapter != null) {
            mListAdapter.update(list);
        }
    }

}
