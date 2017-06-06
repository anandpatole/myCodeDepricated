package com.cheep.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cheep.R;
import com.cheep.databinding.RowTaskBinding;
import com.cheep.databinding.RowTaskGroupBinding;
import com.cheep.databinding.RowUpcomingTaskBinding;
import com.cheep.fragment.TaskFragment;
import com.cheep.interfaces.TaskRowDataInteractionListener;
import com.cheep.model.MessageEvent;
import com.cheep.model.TaskDetailModel;
import com.cheep.utils.LoadMoreSwipeRecyclerAdapter;
import com.cheep.utils.SuperCalendar;
import com.cheep.utils.Utility;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by pankaj on 9/27/16.
 */
public class TaskRecyclerViewAdapter extends LoadMoreSwipeRecyclerAdapter<TaskRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "TaskRecyclerViewAdapter";
    public static final int VIEW_TYPE_UPCOMING = 1;
    public static final int VIEW_TYPE_INDIVIDUAL = 2;
    public static final int VIEW_TYPE_GROUP = 3;

    private TaskRowDataInteractionListener listener;
    private Context context;

    ArrayList<TaskDetailModel> mList;
    int whichFrag;
    Calendar startDateTimeCalendar;
    SuperCalendar superStartDateTimeCalendar;

    ArrayList<String> textlist=new ArrayList<>();

    public TaskRecyclerViewAdapter(int whichFrag, TaskRowDataInteractionListener listener) {
        this.mList = new ArrayList<>();
        this.whichFrag = whichFrag;
        this.listener = listener;
        for(int i=1;i<=10;i++) {
            textlist.add("Lorem Ipsum is simply dummy text of the printing and typesetting industry." + i);
        }
    }

    public TaskRecyclerViewAdapter(ArrayList<TaskDetailModel> mList, TaskRowDataInteractionListener listener, int whichFrag) {
        this.mList = mList;
        this.listener = listener;
        this.whichFrag = whichFrag;
    }

    public void setItem(ArrayList<TaskDetailModel> mList) {
        this.mList = mList;
        notifyDataSetChanged();
    }

    public void addItem(ArrayList<TaskDetailModel> mList) {
        if (mList == null) {
            mList = new ArrayList<>();
        }
        this.mList.addAll(mList);
        notifyDataSetChanged();
    }

    @Override
    public int getActualItemViewType(int position)
    {
        if (whichFrag == TaskFragment.TAB_PAST_TASK) {
            if (mList.get(position).selectedProvider == null) {
                return VIEW_TYPE_GROUP;
            } else {
                return VIEW_TYPE_INDIVIDUAL;
            }
        }
        else
        {
           return VIEW_TYPE_UPCOMING;
        }
    }


    @Override
    public ViewHolder onActualCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        startDateTimeCalendar = Calendar.getInstance();
        superStartDateTimeCalendar = SuperCalendar.getInstance();
        if(viewType == VIEW_TYPE_UPCOMING)
        {
            ViewDataBinding mRowTaskBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_upcoming_task, parent, false);
            return new ViewHolder(mRowTaskBinding);
        }
        else if (viewType == VIEW_TYPE_GROUP)
        {
            ViewDataBinding mRowTaskBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_task_group, parent, false);
            return new ViewHolder(mRowTaskBinding);
        }
        else
        {
            ViewDataBinding mRowTaskBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_task, parent, false);
            return new ViewHolder(mRowTaskBinding);
        }
    }

    @Override
    public void onActualBindViewHolder(final ViewHolder holder, int position) {
        final TaskDetailModel model = mList.get(holder.getAdapterPosition());

        int viewType = getItemViewType(holder.getAdapterPosition());
        if(viewType== VIEW_TYPE_UPCOMING)
        {
            superStartDateTimeCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);
            superStartDateTimeCalendar.setTimeInMillis(Long.parseLong(model.taskStartdate));
            superStartDateTimeCalendar.setLocaleTimeZone();

            Glide.with(context).load(R.drawable.gif_live).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).centerCrop().into(holder.mUpcomingTaskBinding.imgLive);
            if(model.selectedProvider==null)
            {
                holder.mUpcomingTaskBinding.layoutIndividualProfile.setVisibility(View.GONE);
                holder.mUpcomingTaskBinding.layoutGroupProfile.setVisibility(View.VISIBLE);
                holder.mUpcomingTaskBinding.tvProviderName.setText(model.categoryName);
                holder.mUpcomingTaskBinding.tvVerified.setVisibility(View.GONE);
                holder.mUpcomingTaskBinding.ratingBar.setVisibility(View.GONE);
                holder.mUpcomingTaskBinding.imgBadge.setVisibility(View.GONE);
                holder.mUpcomingTaskBinding.tvViewTask.setVisibility(View.GONE);
                holder.mUpcomingTaskBinding.tvViewQuotes.setVisibility(View.VISIBLE);
                holder.mUpcomingTaskBinding.tvTaskResponseStatus.setVisibility(View.VISIBLE);
            }
            else
            {
                holder.mUpcomingTaskBinding.layoutIndividualProfile.setVisibility(View.VISIBLE);
                holder.mUpcomingTaskBinding.layoutGroupProfile.setVisibility(View.GONE);

                Utility.showCircularImageView(holder.mUpcomingTaskBinding.imgProfilePic.getContext(), TAG, holder.mUpcomingTaskBinding.imgProfilePic, model.selectedProvider.profileUrl, Utility.DEFAULT_PROFILE_SRC);
                if(Utility.BOOLEAN.YES.equals(model.selectedProvider.isFavourite))
                    holder.mUpcomingTaskBinding.imgFav.setSelected(true);
                else
                    holder.mUpcomingTaskBinding.imgFav.setSelected(false);
                holder.mUpcomingTaskBinding.tvProviderName.setText(model.selectedProvider.userName);
                if (Utility.BOOLEAN.YES.equalsIgnoreCase(model.selectedProvider.isVerified))
                {
                    holder.mUpcomingTaskBinding.tvVerified.setVisibility(View.VISIBLE);
                    holder.mUpcomingTaskBinding.tvVerified.setText(context.getString(R.string.label_verified).toLowerCase());
                } else {
                    holder.mUpcomingTaskBinding.tvVerified.setVisibility(View.GONE);
                }
                // Show Rating
                holder.mUpcomingTaskBinding.ratingBar.setVisibility(View.VISIBLE);
                Utility.showRating(model.selectedProvider.rating, holder.mUpcomingTaskBinding.ratingBar);

                holder.mUpcomingTaskBinding.imgBadge.setVisibility(View.VISIBLE);
                if(model.selectedProvider.pro_level.equals(Utility.PRO_LEVEL.PLATINUM))
                    holder.mUpcomingTaskBinding.imgBadge.setImageResource(R.drawable.icon_badge_platinum);
                else if(model.selectedProvider.equals(Utility.PRO_LEVEL.GOLD))
                    holder.mUpcomingTaskBinding.imgBadge.setImageResource(R.drawable.icon_badge_gold);
                else if(model.selectedProvider.pro_level.equals(Utility.PRO_LEVEL.SILVER))
                    holder.mUpcomingTaskBinding.imgBadge.setImageResource(R.drawable.icon_badge_silver);
                else if(model.selectedProvider.pro_level.equals(Utility.PRO_LEVEL.BRONZE))
                    holder.mUpcomingTaskBinding.imgBadge.setImageResource(R.drawable.icon_badge_bronze);

                holder.mUpcomingTaskBinding.tvViewTask.setVisibility(View.VISIBLE);
                holder.mUpcomingTaskBinding.tvViewQuotes.setVisibility(View.GONE);

                holder.mUpcomingTaskBinding.tvTaskResponseStatus.setVisibility(View.GONE);
            }

            if (model.providerCount.equals("0"))
            {
                holder.mUpcomingTaskBinding.tvTaskResponseStatus.setText(holder.mView.getContext().getString(R.string.label_awaiting_response));
                holder.mUpcomingTaskBinding.tvViewQuotes.setBackground(ContextCompat.getDrawable(holder.mView.getContext(), R.drawable.img_grey_rounded));
                holder.mUpcomingTaskBinding.tvViewQuotes.setEnabled(false);

            } else {
                int providerCount = Integer.parseInt(model.providerCount);
                holder.mUpcomingTaskBinding.tvTaskResponseStatus.setText(holder.mView.getContext().getResources().getQuantityText(R.plurals.getResponseReceivedString, providerCount));
                holder.mUpcomingTaskBinding.tvViewQuotes.setBackground(ContextCompat.getDrawable(holder.mView.getContext(), R.drawable.img_blue_rounded));
                holder.mUpcomingTaskBinding.tvViewQuotes.setEnabled(true);
            }

            holder.mUpcomingTaskBinding.tvSubCategoryName.setText(model.subCategoryName);
            holder.mUpcomingTaskBinding.tvDesc.setText(model.taskDesc);

            String mBookingDate = holder.mView.getContext().getString(R.string.format_task_book_date
                    , superStartDateTimeCalendar.format(Utility.DATE_FORMAT_DD_MMM_HH_MM_AM));
            holder.mUpcomingTaskBinding.tvTaskBookedDateTime.setText(mBookingDate);

            String mStartTime = holder.mView.getContext().getString(R.string.format_task_start_time
                    , Utility.getDateDifference(superStartDateTimeCalendar.format(Utility.DATE_FORMAT_FULL_DATE)));
            holder.mUpcomingTaskBinding.tvTaskStartedTime.setText(mStartTime);

            holder.mUpcomingTaskBinding.textDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    if (Utility.TASK_STATUS.PROCESSING.equalsIgnoreCase(model.taskStatus) || Utility.TASK_STATUS.COMPLETION_REQUEST.equalsIgnoreCase(model.taskStatus)) {
                        Utility.showToast(context, context.getString(R.string.msg_processing_task_cancelled));
                        holder.mUpcomingTaskBinding.swipeLayout.close(true);
                    } else {
                        listener.onTaskDelete(whichFrag, model, holder.mUpcomingTaskBinding);
                    }
                }
            });

            holder.mUpcomingTaskBinding.textReschedule.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Utility.TASK_STATUS.PROCESSING.equalsIgnoreCase(model.taskStatus)
                            || Utility.TASK_STATUS.COMPLETION_REQUEST.equalsIgnoreCase(model.taskStatus)
                            || Utility.TASK_STATUS.ADDITIONAL_PAYMENT_REQUESTED.equalsIgnoreCase(model.taskStatus)) {
                        Utility.showToast(context, context.getString(R.string.msg_processing_task_reschduled));
                        holder.mUpcomingTaskBinding.swipeLayout.close(true);
                    } else {
                        listener.onTaskReschedule(whichFrag, model, holder.mUpcomingTaskBinding);
                    }
                }
            });

            holder.mUpcomingTaskBinding.textDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onTaskDelete(whichFrag, model, holder.mUpcomingTaskBinding);
                }
            });

            holder.mUpcomingTaskBinding.textReschedule.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onTaskReschedule(whichFrag, model, holder.mUpcomingTaskBinding);
                    }
                }
            });

            holder.mUpcomingTaskBinding.tvViewQuotes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    if (listener != null)
                        listener.onViewQuotesClick(whichFrag, model);
                }
            });

            holder.mUpcomingTaskBinding.swipeLayout.getSurfaceView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onTaskRowFragListItemClicked(whichFrag, model);
                }
            });
            //So swipe adapter (lib method) close any previous opened swipe menu when current swipe is done.
            mItemManger.bindView(holder.itemView, position);
        }
        else if (viewType == VIEW_TYPE_GROUP)
        {
            superStartDateTimeCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);
            superStartDateTimeCalendar.setTimeInMillis(Long.parseLong(model.taskStartdate));
            superStartDateTimeCalendar.setLocaleTimeZone();

            String date_time = holder.mView.getContext().getString(R.string.format_date_time
                    , superStartDateTimeCalendar.format(Utility.DATE_FORMAT_DD_MMM)
                    , superStartDateTimeCalendar.format(Utility.DATE_FORMAT_HH_MM_AM));

            holder.mRowTaskGroupBinding.textDateTime.setText(date_time);

            holder.mRowTaskGroupBinding.textDesc.setText(model.taskDesc);
            holder.mRowTaskGroupBinding.textCategoryName.setText(model.categoryName);
            holder.mRowTaskGroupBinding.textSubCategoryName.setText(model.subCategoryName);

            if (model.providerCount.equals("0"))
            {
                holder.mRowTaskGroupBinding.textResponseCounter.setText(String.valueOf(model.providerCount));
                holder.mRowTaskGroupBinding.textTaskResponseStatus.setText(holder.mView.getContext().getString(R.string.label_responses));
            } else {
                int providerCount = Integer.parseInt(model.providerCount);
                if (providerCount == 1) {
                    holder.mRowTaskGroupBinding.textResponseCounter.setText(String.valueOf(providerCount));
                } else {
                    holder.mRowTaskGroupBinding.textResponseCounter.setText("+" + String.valueOf(providerCount - 1));
                }
                holder.mRowTaskGroupBinding.textTaskResponseStatus.setText(holder.mView.getContext().getString(R.string.label_responses));
            }
            if (Utility.TASK_STATUS.CANCELLED_CUSTOMER.equalsIgnoreCase(model.taskStatus))
            {
                holder.mRowTaskGroupBinding.lnTaskStatusWithQuote.setVisibility(View.VISIBLE);
                holder.mRowTaskGroupBinding.textTaskStatus.setText(context.getString(R.string.label_cancelled));
                holder.mRowTaskGroupBinding.textTaskApprovedQuote.setVisibility(View.GONE);
            }
            else if (Utility.TASK_STATUS.CANCELLED_SP.equalsIgnoreCase(model.taskStatus))
            {
                holder.mRowTaskGroupBinding.lnTaskStatusWithQuote.setVisibility(View.VISIBLE);
                holder.mRowTaskGroupBinding.textTaskStatus.setText(context.getString(R.string.label_cancelled));
                holder.mRowTaskGroupBinding.textTaskApprovedQuote.setVisibility(View.GONE);
            }
            else if (Utility.TASK_STATUS.ELAPSED.equalsIgnoreCase(model.taskStatus))
            {
                holder.mRowTaskGroupBinding.lnTaskStatusWithQuote.setVisibility(View.VISIBLE);
                holder.mRowTaskGroupBinding.textTaskStatus.setText(context.getString(R.string.label_lapsed));
                holder.mRowTaskGroupBinding.textTaskApprovedQuote.setVisibility(View.GONE);
            }
            else if (Utility.TASK_STATUS.DISPUTED.equalsIgnoreCase(model.taskStatus))
            {
                holder.mRowTaskGroupBinding.lnTaskStatusWithQuote.setVisibility(View.VISIBLE);
                holder.mRowTaskGroupBinding.textTaskStatus.setText(context.getString(R.string.label_disputed));
                holder.mRowTaskGroupBinding.textTaskApprovedQuote.setVisibility(View.GONE);
            }
            else
            {
                holder.mRowTaskGroupBinding.lnTaskStatusWithQuote.setVisibility(View.GONE);
            }
            holder.mRowTaskGroupBinding.swipeLayout.setSwipeEnabled(false);
            mItemManger.bindView(holder.itemView, position);
        } else {
            //======Individual item(when sp is selected for task)=====

            superStartDateTimeCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);
            superStartDateTimeCalendar.setTimeInMillis(Long.parseLong(model.taskStartdate));
            superStartDateTimeCalendar.setLocaleTimeZone();

            String date_time = holder.mView.getContext().getString(R.string.format_date_time
                    , superStartDateTimeCalendar.format(Utility.DATE_FORMAT_DD_MMM)
                    , superStartDateTimeCalendar.format(Utility.DATE_FORMAT_HH_MM_AM));
            holder.mRowTaskBinding.textDateTime.setText(date_time);

            holder.mRowTaskBinding.textDesc.setText(model.taskDesc);

            Utility.showCircularImageView(holder.mRowTaskBinding.imgProfile.getContext(), TAG, holder.mRowTaskBinding.imgProfile, model.selectedProvider.profileUrl, Utility.DEFAULT_PROFILE_SRC);
            holder.mRowTaskBinding.textProviderName.setText(model.selectedProvider.userName);
            if (!TextUtils.isEmpty(model.subCategoryName))
                holder.mRowTaskBinding.textSubCategoryName.setText(model.subCategoryName);
            holder.mRowTaskBinding.imgProfile.setVisibility(View.VISIBLE);

            if (Utility.BOOLEAN.YES.equalsIgnoreCase(model.selectedProvider.isVerified)) {
                holder.mRowTaskBinding.textVerified.setVisibility(View.VISIBLE);
                holder.mRowTaskBinding.textVerified.setText(context.getString(R.string.label_verified).toLowerCase());
            } else {
                holder.mRowTaskBinding.textVerified.setVisibility(View.GONE);
            }

            // Show Rating
            Utility.showRating(model.selectedProvider.rating, holder.mRowTaskBinding.ratingBar);
            holder.mRowTaskBinding.swipeLayout.close(true);

            holder.mRowTaskBinding.lnTaskStatusWithQuote.setVisibility(View.VISIBLE);

            holder.mRowTaskBinding.textTaskApprovedQuote.setText(holder.mRowTaskBinding.imgProfile.getContext().getString(R.string.ruppe_symbol_x_space, Utility.getActualPrice(model.taskPaidAmount, model.selectedProvider.quotePrice)));
            if (Utility.TASK_STATUS.CANCELLED_CUSTOMER.equalsIgnoreCase(model.taskStatus)) {
                holder.mRowTaskBinding.textTaskStatus.setText(context.getString(R.string.label_cancelled));
            } else if (Utility.TASK_STATUS.RESCHEDULE_REQUEST_REJECTED.equalsIgnoreCase(model.taskStatus)) {

                holder.mRowTaskBinding.textTaskStatus.setText(context.getString(R.string.label_reschedule_rejected));
            } else if (Utility.TASK_STATUS.CANCELLED_SP.equalsIgnoreCase(model.taskStatus)) {
                holder.mRowTaskBinding.textTaskStatus.setText(context.getString(R.string.label_cancelled));
            } else if (Utility.TASK_STATUS.DISPUTED.equalsIgnoreCase(model.taskStatus)) {
                holder.mRowTaskBinding.textTaskStatus.setText(context.getString(R.string.label_disputed));
            } else if (Utility.TASK_STATUS.COMPLETION_CONFIRM.equalsIgnoreCase(model.taskStatus)) {
                holder.mRowTaskBinding.textTaskStatus.setText(context.getString(R.string.label_completed));
            } else {
                holder.mRowTaskBinding.textTaskStatus.setText(context.getString(R.string.label_completed));
            }
            holder.mRowTaskBinding.swipeLayout.getSurfaceView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onTaskRowFragListItemClicked(whichFrag, model);
                }
            });
            holder.mRowTaskBinding.swipeLayout.setSwipeEnabled(false);
            mItemManger.bindView(holder.itemView, position);
        }
    }

    @Override
    public int onActualItemCount() {
        return mList.size();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe_layout;
    }

    public ArrayList<TaskDetailModel> getmList() {
        return mList;
    }

    public void updateFavStatus(String id, String isFav) {
        if (mList != null) {
            boolean isUpdated = false;
            //we are not breaking this loop because we may need to change status of same selectedProvider in another rows
            for (TaskDetailModel providerModel : mList) {
                if (providerModel.selectedProvider != null && providerModel.selectedProvider.providerId.equalsIgnoreCase(id)) {
                    providerModel.selectedProvider.isFavourite = isFav;
                    isUpdated = true;
//                    break;
                }
            }
            if (isUpdated)
                notifyDataSetChanged();
        }
    }

    public void updateRatedStatus(String id) {
        if (mList != null) {
            for (TaskDetailModel providerModel : mList) {
                if (providerModel.taskId.equalsIgnoreCase(id)) {
                    providerModel.ratingDone = Utility.BOOLEAN.YES;
                    notifyDataSetChanged();
                    break;
                }
            }
        }
    }

    // Update the list in case new quote requested by any of the PRO
    public void updateOnNewQuoteRequested(String task_id, String max_quote_price, String sp_counts) {
        if (mList != null) {
            for (TaskDetailModel providerModel : mList) {
                if (providerModel.taskId.equalsIgnoreCase(task_id)) {
                    providerModel.maxQuotePrice = max_quote_price;
                    providerModel.providerCount = sp_counts;
                    notifyDataSetChanged();
                    break;
                }
            }
        }
    }

    public int cancelTask(String id, String status) {
        if (mList != null) {
            int i = 0;
            for (TaskDetailModel taskDetailModel : mList) {
                if (taskDetailModel.taskId.equalsIgnoreCase(id)) {
                    taskDetailModel.taskStatus = status;
                    mList.remove(i);
                    notifyItemRemoved(i);

                    //Need to call listener for getting callback for listener
                    listener.onMigrateTaskFromPendingToPast(taskDetailModel);
                    break;
                }
                i++;
            }
        }
        return mList.size();
    }

    public void addData(TaskDetailModel model) {
        mList.add(model);
        notifyDataSetChanged();
    }

    public void rescheduleTask(String taskId, String start_datetime) {
        if (mList != null) {
            int i = 0;
            for (TaskDetailModel taskDetailModel : mList) {
                if (taskDetailModel.taskId.equalsIgnoreCase(taskId)) {
//                    taskDetailModel.taskStartdate = start_datetime;
                    taskDetailModel.taskStatus = Utility.TASK_STATUS.RESCHEDULE_REQUESTED;
                    notifyItemChanged(i);
                    break;
                }
                i++;
            }
        }
    }

    public void updateTaskStatus(MessageEvent event) {
        if (mList != null) {
            int i = 0;
            for (TaskDetailModel taskDetailModel : mList) {
                if (taskDetailModel.taskId.equalsIgnoreCase(event.id)) {
                    taskDetailModel.taskStatus = event.taskStatus;
                    notifyItemChanged(i);
                    Log.i(TAG, "updateTaskStatus: Task Status Got Changed!!");
                    break;
                }
                i++;
            }
        }
    }

    public void updateOnAdditionalPaymentRequested(MessageEvent event) {
        if (mList != null) {
            int i = 0;
            for (TaskDetailModel taskDetailModel : mList) {
                if (taskDetailModel.taskId.equalsIgnoreCase(event.id)) {
                    taskDetailModel.taskStatus = event.taskStatus;
                    taskDetailModel.additionalQuoteAmount = event.additional_quote_amount;
                    notifyItemChanged(i);
                    Log.i(TAG, "updateTaskStatus: Task Status Got Changed!!");
                    break;
                }
                i++;
            }
        }
    }

    public void updateOnDetailRequestRejected(MessageEvent event) {
        Log.d(TAG, "updateOnDetailRequestRejected() called with: event = [" + event + "]");
        if (mList != null) {
            int i = 0;
            for (TaskDetailModel taskDetailModel : mList) {
                if (taskDetailModel.taskId.equalsIgnoreCase(event.id)) {
                    taskDetailModel.providerCount = String.valueOf(Integer.parseInt(taskDetailModel.providerCount) - 1);
                    notifyItemChanged(i);
                    break;
                }
                i++;
            }
        }
    }

    public void updateOnNewDetailRequested(String id, String sp_counts) {
        if (mList != null) {
            for (TaskDetailModel providerModel : mList) {
                if (providerModel.taskId.equalsIgnoreCase(id)) {
                    providerModel.providerCount = sp_counts;
                    notifyDataSetChanged();
                    break;
                }
            }
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        RowTaskBinding mRowTaskBinding;
        RowTaskGroupBinding mRowTaskGroupBinding;
        RowUpcomingTaskBinding mUpcomingTaskBinding;

        public ViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            if(binding instanceof RowUpcomingTaskBinding)
            {
                mUpcomingTaskBinding= (RowUpcomingTaskBinding) binding;
            }
            else if (binding instanceof RowTaskBinding) {
                mRowTaskBinding = (RowTaskBinding) binding;
            } else {
                mRowTaskGroupBinding = (RowTaskGroupBinding) binding;
            }
        }
    }
}
