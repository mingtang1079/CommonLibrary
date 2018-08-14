package com.appbaselib.presenter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.appbaselib.utils.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.pangu.appbaselibrary.R;

import java.util.ArrayList;
import java.util.List;

//封装刷新相关逻辑
public class RefreshPresenter<T> {

    Context mContext;
    public RecyclerView mRecyclerview;
    public SwipeRefreshLayout mSwipeRefreshLayout;

    public List<T> mList;
    public BaseQuickAdapter mAdapter;
    public RecyclerView.LayoutManager mLayoutManager;
    public boolean isReReresh = true;//重新刷新 清楚数据
    public int pageNo = 1;  //当前页
    public boolean isFirstReresh = true;
    public int pageSize = 10; //每页条数
    public boolean isLoadmore = false; //是否开启加载更多
    public boolean isLoadmoreIng = false;  //是否正在加载更多
    LoadDataListener mLoadDataListener;


    public RefreshPresenter(RecyclerView mRecyclerview, SwipeRefreshLayout mSwipeRefreshLayout, BaseQuickAdapter mAdapter, RecyclerView.LayoutManager mLayoutManager) {
        this.mRecyclerview = mRecyclerview;
        this.mSwipeRefreshLayout = mSwipeRefreshLayout;
        this.mAdapter = mAdapter;
        this.mLayoutManager = mLayoutManager;
        mContext = mRecyclerview.getContext();
        if (mContext instanceof LoadDataListener) {
            mLoadDataListener = (LoadDataListener) mContext;
        } else {
            new IllegalStateException("该activity/Fragment请实现LoadDataListener接口");
        }
        init();
    }

    private void init() {

        View mView = ((Activity) mContext).getLayoutInflater().inflate(R.layout.view_empty, (ViewGroup) mRecyclerview.getParent(), false);
        mList = new ArrayList<>();
        mRecyclerview.setLayoutManager(mLayoutManager);
        if (mAdapter == null)
            throw new NullPointerException("adapter is null");
        mAdapter.setEmptyView(mView);
        mSwipeRefreshLayout.setColorSchemeColors(mContext.getResources().getColor(R.color.colorAccent));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isLoadmoreIng)
                    refreshData(false);
                else
                    mSwipeRefreshLayout.setRefreshing(false);
            }
        });

    }


    public void setLoadMoreListener() {
        isLoadmore = true;
        mAdapter.setEnableLoadMore(false);
        mAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {

                if (!mSwipeRefreshLayout.isRefreshing()) {
                    isLoadmoreIng = true;
                    if (mLoadDataListener != null) {
                        mLoadDataListener.onLoadData();
                    }
                } else {

                }

            }
        }, mRecyclerview);

    }


    //重新刷新数据
    public void refreshData(boolean isShow) {

        //   mRecyclerview.scrollToPosition(0);
        isReReresh = true;
        pageNo = 1;
        if (isShow)
            mSwipeRefreshLayout.setRefreshing(true);
        if (mLoadDataListener != null) {
            mLoadDataListener.onLoadData();
        }
        if (isLoadmore) {
            mAdapter.setEnableLoadMore(false);
            //重新刷新,重新设置加载更多的逻辑
        }

    }

    public void loadComplete(List<? extends T> mData) {

        if (isFirstReresh) {
            mRecyclerview.setAdapter(mAdapter);  //如果一开始就设置，会导致 先出现  空数据 再加载数据
        }
        if (isReReresh) {
            mList.clear();
        }
        if (mData != null && mData.size() != 0) {

            pageNo++;
            mAdapter.addData(mData);


            if (isFirstReresh || isReReresh) {

                isFirstReresh = false;
                isReReresh = false;
                //当数据不满一页的时候，取消加载更多
                if (isLoadmore) {

                    //延时操作，避免 lastitem为 -1
//                mRecyclerview.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        int lastItem = ((LinearLayoutManager) mRecyclerview.getLayoutManager()).findLastCompletelyVisibleItemPosition();
//                        int all_item = mAdapter.requestData().size() + mAdapter.getHeaderLayoutCount() + mAdapter.getFooterLayoutCount();
//
//                        if (lastItem == all_item - 1)   //表示数据不满一页
//                            mAdapter.setEnableLoadMore(false);
//                        else {
//                            mAdapter.notifyDataSetChanged();
//                            mAdapter.setEnableLoadMore(true);
//                            mAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
//                                @Override
//                                public void onLoadMoreRequested() {
//
//                                    if (!mSwipeRefreshLayout.isRefreshing()) {
//                                        isLoadmoreIng = true;
//                                        requestData();
//                                    } else {
//
//                                    }
//
//                                }
//                            }, mRecyclerview);
//
//                        }
//
//                    }
//                }, 300);

                    mAdapter.disableLoadMoreIfNotFullPage(mRecyclerview);
                }
            }


        } else {
            mAdapter.notifyDataSetChanged();//清空视图
        }
        mSwipeRefreshLayout.setRefreshing(false);
        //     toggleShowLoading(false);
        if (isLoadmore && isLoadmoreIng) {
            isLoadmoreIng = false;
            if (mData == null || mData.size() == 0)
                mAdapter.loadMoreEnd();
            else
                mAdapter.loadMoreComplete();
        }

    }


    public void loadError(String mes) {

        ToastUtils.showToast(mContext, mes, Toast.LENGTH_SHORT);
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(false);

        if (isLoadmoreIng) {
            isLoadmoreIng = false;
            mAdapter.loadMoreFail();
        }

    }

    //===================================================我是分隔符=========================================================

    public interface LoadDataListener {
        void onLoadData();
    }

}
