package com.tenth.space.ui.activity;

import android.app.Activity;
import android.content.Intent;

import com.tenth.space.BitherjSettings;
import com.tenth.space.R;

import com.tenth.space.imservice.manager.IMLoginManager;
import com.tenth.space.ui.base.DialogWithActions;
import com.tenth.space.utils.IMUIHelper;
import com.tenth.space.utils.Logger;

import java.util.ArrayList;
import java.util.List;


public class DialogTopOptActivity extends DialogWithActions {

    private Activity activity;
    protected static Logger logger = Logger.getLogger(DialogTopOptActivity.class);

    public DialogTopOptActivity(Activity context) {
        super(context);
        this.activity = context;
    }

    @Override
    protected List<Action> getActions() {
        ArrayList<Action> actions = new ArrayList<Action>();
        actions.add(new Action(R.string.scan,
                new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(activity, ScanActivity.class);//TransferAmountActivity  //ScanActivity
                       //activity.startActivity(intent);
                        activity.startActivityForResult(intent, BitherjSettings.INTENT_REF.SCAN_REQUEST_CODE);
                    }
                }));
        actions.add(new Action(R.string.createDiscussionGroup,new Runnable() {
            @Override
            public void run() {
                IMUIHelper.openGroupMemberSelectActivity(activity,"1_"+ IMLoginManager.instance().getLoginId(), 2);//wystan modify to only create tmp group
            }
        }));

        return actions;
    }
}