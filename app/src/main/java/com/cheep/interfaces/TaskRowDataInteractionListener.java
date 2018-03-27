package com.cheep.interfaces;

import com.cheep.databinding.RowUpcomingTaskBinding;
import com.cheep.model.BannerImageModel;
import com.cheep.model.JobCategoryModel;
import com.cheep.model.TaskDetailModel;

public interface TaskRowDataInteractionListener {
    void onTaskDelete(int which, TaskDetailModel exploreDataModel, RowUpcomingTaskBinding mRowUpcomingTaskBinding);

    void onTaskReschedule(int which, TaskDetailModel exploreDataModel, RowUpcomingTaskBinding mRowUpcomingTaskBinding);

    void onViewQuotesClick(int which, TaskDetailModel exploreDataModel);

    void onTaskRowFragListItemClicked(int which, TaskDetailModel exploreDataModel); //, RowTaskBinding mRawTaskBinding

    void onFavClicked(TaskDetailModel providerModel, boolean isAddToFav, int position);

    void onMigrateTaskFromPendingToPast(TaskDetailModel model);

    void onCallClicked(TaskDetailModel providerModel);

    void onBookSimilarTaskClicked(JobCategoryModel jobCategoryModel, BannerImageModel bannerImageModel);

    //    void onTaskRowFragListItemClicked(int which, TaskChatModel exploreDataModel); //, RowTabChatBinding mRawTabChatBinding
    void onCreateNewTask();

}