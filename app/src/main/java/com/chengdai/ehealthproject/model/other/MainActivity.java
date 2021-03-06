package com.chengdai.ehealthproject.model.other;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.amap.api.location.AMapLocation;
import com.chengdai.ehealthproject.R;
import com.chengdai.ehealthproject.base.AbsBaseActivity;
import com.chengdai.ehealthproject.base.BaseLocationActivity;
import com.chengdai.ehealthproject.databinding.ActivityMainBinding;
import com.chengdai.ehealthproject.model.common.model.EventBusModel;
import com.chengdai.ehealthproject.model.common.model.IntroductionInfoModel;
import com.chengdai.ehealthproject.model.common.model.LocationModel;
import com.chengdai.ehealthproject.model.dataadapters.ViewPagerAdapter;
import com.chengdai.ehealthproject.model.healthcircle.HealthCircleFragment;
import com.chengdai.ehealthproject.model.healthmanager.fragments.HealthManagerFragment;
import com.chengdai.ehealthproject.model.healthstore.HealthStoreFragment;
import com.chengdai.ehealthproject.model.tabmy.MyFragment;
import com.chengdai.ehealthproject.model.tabsurrounding.SurroundingFragment;
import com.chengdai.ehealthproject.model.tabtourism.TourismFragment;
import com.chengdai.ehealthproject.model.user.LoginActivity;
import com.chengdai.ehealthproject.uitls.AppUtils;
import com.chengdai.ehealthproject.uitls.LogUtil;
import com.chengdai.ehealthproject.uitls.StringUtils;
import com.chengdai.ehealthproject.uitls.nets.RetrofitUtils;
import com.chengdai.ehealthproject.uitls.nets.RxTransformerHelper;
import com.chengdai.ehealthproject.uitls.nets.RxTransformerListHelper;
import com.chengdai.ehealthproject.weigit.appmanager.MyConfig;
import com.chengdai.ehealthproject.weigit.appmanager.SPUtilHelpr;
import com.chengdai.ehealthproject.weigit.dialog.CommonDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 主页面
 */

public class MainActivity extends BaseLocationActivity {

    private ActivityMainBinding mainBinding;

    private int mTabIndex = 1;//记录用户点击下标 用于未登录时恢复状态

    private int mShowIndex = 1;//显示相应页面
    private CommonDialog commonDialog;


    /**
     * 打开当前页面
     *
     * @param context
     */
    public static void open(Context context, int select) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, MainActivity.class);

        intent.putExtra("select", select);

        context.startActivity(intent);
    }


    @Override
    protected void locationSuccessful(AMapLocation aMapLocation) {
        LocationModel locationModel = new LocationModel(aMapLocation.getCountry(),
                aMapLocation.getProvince(), aMapLocation.getCity(), aMapLocation.getDistrict(), aMapLocation.getLatitude() + "", aMapLocation.getLongitude() + "");
        SPUtilHelpr.saveLocationInfo(StringUtils.getJsonToString(locationModel));
        SPUtilHelpr.saveRestLocationInfo("");

        EventBus.getDefault().post(aMapLocation);
        LogUtil.E("定位成功 Main" + aMapLocation.getErrorCode() + aMapLocation.getErrorInfo());
    }

    @Override
    protected void locationFailure(AMapLocation aMapLocation) {
        SPUtilHelpr.saveLocationInfo("");

        EventBusModel eventBusModel = new EventBusModel();
        eventBusModel.setTag("locationFailure");
        EventBus.getDefault().post(eventBusModel);

    }

    @Override
    protected void onNegativeButton() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
//        mainBinding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.activity_main, null, false);;
//
//        addMainView(mainBinding.getRoot());

//        hintTitleView();

        EventBus.getDefault().register(this);

        if (getIntent() != null) {
            mTabIndex = getIntent().getIntExtra("select", 1);
            mShowIndex = mTabIndex - 1;
        }

        initViewState();

        startLocation();

        getUpdateReqeust();

    }

    /**
     * 初始化View状态
     */
    private void initViewState() {
        mainBinding.pagerMain.setPagingEnabled(false);//禁止左右切换

        List<Fragment> fragments = new ArrayList<>(); //设置fragment数据

        fragments.add(new HealthManagerFragment());
        fragments.add(new HealthCircleFragment());
        fragments.add(new TourismFragment());
        fragments.add(new SurroundingFragment());
        fragments.add(new HealthStoreFragment());
        fragments.add(new MyFragment());

        mainBinding.pagerMain.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(), fragments));

        mainBinding.pagerMain.setOffscreenPageLimit(fragments.size());

        mainBinding.layoutMainButtom.radioMainTabManager.setOnClickListener(v -> {
            mainBinding.pagerMain.setCurrentItem(0, false);
            mTabIndex = 1;
        });
        mainBinding.layoutMainButtom.radioMainTabFriend.setOnClickListener(v -> {

            mainBinding.pagerMain.setCurrentItem(1, false);
            mTabIndex = 2;
        });


        mainBinding.layoutMainButtom.radioMainTabTourism.setOnClickListener(v -> {
            mainBinding.pagerMain.setCurrentItem(2, false);
            mTabIndex = 3;
        });

        mainBinding.layoutMainButtom.radioMainTabSurrounding.setOnClickListener(v -> {
            mainBinding.pagerMain.setCurrentItem(3, false);
            mTabIndex = 4;
        });
        mainBinding.layoutMainButtom.radioMainTabShop.setOnClickListener(v -> {
            mainBinding.pagerMain.setCurrentItem(4, false);
            mTabIndex = 5;
        });


        mainBinding.layoutMainButtom.radioMainTabMy.setOnClickListener(v -> {
            if (!SPUtilHelpr.isLoginNoStart()) {

                setTabIndex();

                LoginActivity.open(this, false);
            } else {
                mTabIndex = 6;
                mainBinding.pagerMain.setCurrentItem(6, false);
            }
        });
        setTabIndex();
        mainBinding.pagerMain.setCurrentItem(mShowIndex, false);


/*       mSubscription.add( RxRadioGroup.checkedChanges(mainBinding.layoutMainButtom.radiogroup) //点击切换
               .subscribe(integer -> {
                   switch (integer){
                       case R.id.radio_main_tab_1:
                           mainBinding.pagerMain.setCurrentItem(0,false);
                           break;
                       case R.id.radio_main_tab_2:
                           mainBinding.pagerMain.setCurrentItem(1,false);
                           break;
                       case R.id.radio_main_tab_3:
                           mainBinding.pagerMain.setCurrentItem(2,false);
                           break;
                       case R.id.radio_main_tab_4:
                           mainBinding.pagerMain.setCurrentItem(3,false);
                           break;
                       case R.id.radio_main_tab_5:

                           if(!SPUtilHelpr.isLogin(this)){
                               LoginActivity.open(this,false);
                           }else{
                               mainBinding.pagerMain.setCurrentItem(4,false);
                           }


                           break;
                   }

          },Throwable::printStackTrace));*/

    }

    private void setTabIndex() {
        switch (mTabIndex) {
            case 1:
                mainBinding.layoutMainButtom.radioMainTabManager.setChecked(true);
                break;
            case 2:
                mainBinding.layoutMainButtom.radioMainTabFriend.setChecked(true);
                break;
            case 3:
                mainBinding.layoutMainButtom.radioMainTabTourism.setChecked(true);
                break;
            case 4:
                mainBinding.layoutMainButtom.radioMainTabSurrounding.setChecked(true);
                break;
            case 5:
                mainBinding.layoutMainButtom.radioMainTabShop.setChecked(true);
                break;
            case 6:
                mainBinding.layoutMainButtom.radioMainTabMy.setChecked(true);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (!isFinishing()) {
            new CommonDialog(this).builder().setPositiveBtn("确认", (view) -> {
                logOut();
            }).setNegativeBtn("取消", null).setTitle("提示").setContentMsg("确认退出健康e购？").show();
        } else {
            logOut();
        }
    }

    public void logOut() {
        EventBusModel eventBusModel = new EventBusModel();
        eventBusModel.setTag("AllFINISH");
        EventBus.getDefault().post(eventBusModel); //结束掉所有界面

        EventBusModel eventBusMode2 = new EventBusModel();
        eventBusMode2.setTag("MainActivityFinish");
        EventBus.getDefault().post(eventBusMode2); //结束掉所有界面

        SPUtilHelpr.saveLocationInfo("");             //重置定位数据
        SPUtilHelpr.saveRestLocationInfo("");

        finish();
    }

    /**
     * 1-4设置显示位置
     *
     * @param eventBus
     */
    @Subscribe
    public void MainActivityEvent(EventBusModel eventBus) {
        if (eventBus == null) return;

        if (TextUtils.equals(eventBus.getTag(), "MainSetIndex")) {
            mTabIndex = eventBus.getEvInt();
            mShowIndex = eventBus.getEvInt() - 1;
            mainBinding.pagerMain.setCurrentItem(mShowIndex, false);
            setTabIndex();
        } else if (TextUtils.equals(eventBus.getTag(), "MainActivityFinish")) {
            finish();
        }
    }

    protected void showUpdateDialog(String str, Boolean isFouceUpdate, CommonDialog.OnPositiveListener onPositiveListener) {

        if (isFinishing()) {
            return;
        }
        if (isFouceUpdate) {
            commonDialog = new CommonDialog(this).builder()
                    .setTitle("更新提醒").setContentMsg(str)
                    .setPositiveBtn("立刻升级", onPositiveListener);

            commonDialog.show();
            return;
        }
        commonDialog = new CommonDialog(this).builder()
                .setTitle("更新提醒").setContentMsg(str)
                .setPositiveBtn("立刻升级", onPositiveListener)
                .setNegativeBtn("稍后提醒", null, false);

        commonDialog.show();
    }


    public void getUpdateReqeust() {

        Map<String, String> map = new HashMap<>();
        map.put("type", "3");
        map.put("systemCode", MyConfig.SYSTEMCODE);
        map.put("companyCode", MyConfig.COMPANYCODE);
        map.put("start", "1");
        map.put("limit", "30");

        RetrofitUtils.getLoaderServer().getInfoByUpdate("807715", StringUtils.getJsonToString(map))
                .compose(RxTransformerHelper.applySchedulerResult(this))
                .filter(introductionInfoModels -> introductionInfoModels != null &&
                        introductionInfoModels.getList() != null && introductionInfoModels.getList().size() > 0)
                .subscribe(s -> {
                    showUpdateState(s.getList());
                }, Throwable::printStackTrace);
    }

    /**
     * 根据参数判断是否需要更新
     */
    private void showUpdateState(List<IntroductionInfoModel> datas) {
        String loadURL = "";
        String loademark = "";
        Boolean isLoad = false;
        Boolean isforceUpdate = false;

        for (IntroductionInfoModel model : datas) {
            if (model == null) continue;

            if (TextUtils.equals(model.getCkey(), "androidDownload")) {//获取下载连接

                loadURL = model.getCvalue();

            } else if (TextUtils.equals(model.getCkey(), "androidNote")) {//获取更新说明

                loademark = model.getCvalue();

            } else if (TextUtils.equals(model.getCkey(), "androidForceUpdate")) {//是否强制更新

                isforceUpdate = TextUtils.equals(model.getCvalue(), "1");

            } else if (TextUtils.equals(model.getCkey(), "androidVersion")) {//是否更新

                isLoad = !TextUtils.equals(model.getCvalue(), AppUtils.getAppVersionName(MainActivity.this));

            }
        }

        if (isLoad) {
            String finalLoadURL = loadURL;
            Boolean finalIsforceUpdate = isforceUpdate;
            showUpdateDialog(loademark, isforceUpdate, view -> {
                AppUtils.startWeb(this, finalLoadURL);
                if (finalIsforceUpdate) {
                    finish();
                }
            });
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        SPUtilHelpr.saveLocationInfo("");
        EventBus.getDefault().unregister(this);
        if (commonDialog != null) {
            commonDialog.closeDialog();
            commonDialog = null;
        }
    }
}
