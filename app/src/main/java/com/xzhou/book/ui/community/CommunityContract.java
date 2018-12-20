package com.xzhou.book.ui.community;

import com.xzhou.book.models.Entities;
import com.xzhou.book.ui.common.BaseContract;

import java.util.List;

public interface CommunityContract {

    interface Presenter extends BaseContract.BasePresenter {



    }

    interface View extends BaseContract.BaseView<Presenter> {
        void onInitData(List<Entities.ItemClick> list);
    }

}
