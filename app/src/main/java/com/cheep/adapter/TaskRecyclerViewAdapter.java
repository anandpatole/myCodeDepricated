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

import com.cheep.R;
import com.cheep.databinding.RowTaskBinding;
import com.cheep.databinding.RowTaskGroupBinding;
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
    public static final int VIEW_TYPE_INDIVIDUAL = 1;
    public static final int VIEW_TYPE_GROUP = 2;

    private TaskRowDataInteractionListener listener;
    private Context context;

    ArrayList<TaskDetailModel> mList;
    int whichFrag;
    Calendar startDateTimeCalendar;
    SuperCalendar superStartDateTimeCalendar;

    public TaskRecyclerViewAdapter(int whichFrag, TaskRowDataInteractionListener listener) {
        this.mList = new ArrayList<>();
        this.whichFrag = whichFrag;
        this.listener = listener;
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
    public int getActualItemViewType(int position) {
        if (mList.get(position).selectedProvider == null) {
            return VIEW_TYPE_GROUP;
        } else {
            return VIEW_TYPE_INDIVIDUAL;
        }
    }


    @Override
    public ViewHolder onActualCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        startDateTimeCalendar = Calendar.getInstance();
        superStartDateTimeCalendar = SuperCalendar.getInstance();
        if (viewType == VIEW_TYPE_GROUP) {
            ViewDataBinding mRowTaskBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_task_group, parent, false);
            return new ViewHolder(mRowTaskBinding);
        } else {
            ViewDataBinding mRowTaskBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_task, parent, false);
            return new ViewHolder(mRowTaskBinding);
        }
    }

    @Override
    public void onActualBindViewHolder(final ViewHolder holder, int position) {
        final TaskDetailModel model = mList.get(holder.getAdapterPosition());

        int viewType = getItemViewType(holder.getAdapterPosition());

        if (viewType == VIEW_TYPE_GROUP) {
            superStartDateTimeCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);
            superStartDateTimeCalendar.setTimeInMillis(Long.parseLong(model.taskStartdate));
            superStartDateTimeCalendar.setLocaleTimeZone();
            /*holder.mRowTaskGroupBinding.textDate.setText(superStartDateTimeCalendar.format(Utility.DATE_FORMAT_DD_MMM));
            holder.mRowTaskGroupBinding.textTime.setText(superStartDateTimeCalendar.format(Utility.DATE_FORMAT_HH_MM_AM));*/
            String date_time = holder.mView.getContext().getString(R.string.format_date_time
                    , superStartDateTimeCalendar.format(Utility.DATE_FORMAT_DD_MMM)
                    , superStartDateTimeCalendar.format(Utility.DATE_FORMAT_HH_MM_AM));
            holder.mRowTaskGroupBinding.textDateTime.setText(date_time);

            holder.mRowTaskGroupBinding.textDesc.setText(model.taskDesc);
            holder.mRowTaskGroupBinding.textCategoryName.setText(model.categoryName);
            holder.mRowTaskGroupBinding.textSubCategoryName.setText(model.subCategoryName);

            /*//this is for marquee
            holder.mRowTaskGroupBinding.textCategoryName.setSingleLine(true);
            holder.mRowTaskGroupBinding.textCategoryName.setSelected(true);
            holder.mRowTaskGroupBinding.textCategoryName.setAllCaps(false);*/

            /*holder.mRowTaskGroupBinding.textResponseCount.setText(String.valueOf(model.providerCount));
            holder.mRowTaskGroupBinding.textResponseCount.setVisibility(View.VISIBLE);*/

            if (model.providerCount.equals("0")) {
                holder.mRowTaskGroupBinding.textResponseCounter.setText(String.valueOf(model.providerCount));
                holder.mRowTaskGroupBinding.textTaskResponseStatus.setText(holder.mView.getContext().getString(R.string.label_awaiting_response));
                holder.mRowTaskGroupBinding.textViewQuotes.setBackground(ContextCompat.getDrawable(holder.mView.getContext(), R.drawable.img_grey_rounded));
            } else {
                int providerCount = Integer.parseInt(model.providerCount);
                if (providerCount == 1) {
                    holder.mRowTaskGroupBinding.textResponseCounter.setText(String.valueOf(providerCount));
                } else {
                    holder.mRowTaskGroupBinding.textResponseCounter.setText("+" + String.valueOf(providerCount - 1));
                }
                holder.mRowTaskGroupBinding.textTaskResponseStatus.setText(holder.mView.getContext().getResources().getQuantityText(R.plurals.getResponseReceivedString, providerCount));
                holder.mRowTaskGroupBinding.textViewQuotes.setBackground(ContextCompat.getDrawable(holder.mView.getContext(), R.drawable.img_blue_rounded));
            }
            int pix_in_8_dp = (int) Utility.convertDpToPixel(8, holder.mView.getContext());
            int pix_in_5_dp = (int) Utility.convertDpToPixel(5, holder.mView.getContext());
            holder.mRowTaskGroupBinding.textViewQuotes.setPadding(pix_in_8_dp, pix_in_5_dp, pix_in_8_dp, pix_in_5_dp);

            holder.mRowTaskGroupBinding.textDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onTaskDelete(whichFrag, model, holder.mRowTaskBinding);
                }
            });

            holder.mRowTaskGroupBinding.textReschedule.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onTaskReschedule(whichFrag, model, holder.mRowTaskBinding);
                    }
                }
            });

            if (Utility.TASK_STATUS.CANCELLED_CUSTOMER.equalsIgnoreCase(model.taskStatus)) {
               /* holder.mRowTaskGroupBinding.textName.setTextColor(ContextCompat.getColor(context, R.color.red));
                holder.mRowTaskGroupBinding.textName.setText(context.getString(R.string.msg_task_cancelled_title));*/
                holder.mRowTaskGroupBinding.lnTaskStatusWithQuote.setVisibility(View.VISIBLE);
                holder.mRowTaskGroupBinding.textViewQuotes.setVisibility(View.GONE);
                holder.mRowTaskGroupBinding.textTaskStatus.setText(context.getString(R.string.label_cancelled));
                holder.mRowTaskGroupBinding.textTaskApprovedQuote.setVisibility(View.GONE);
            } else if (Utility.TASK_STATUS.CANCELLED_SP.equalsIgnoreCase(model.taskStatus)) {
               /* holder.mRowTaskGroupBinding.textName.setTextColor(ContextCompat.getColor(context, R.color.red));
                holder.mRowTaskGroupBinding.textName.setText(context.getString(R.string.msg_task_cancelled_title));*/
                holder.mRowTaskGroupBinding.lnTaskStatusWithQuote.setVisibility(View.VISIBLE);
                holder.mRowTaskGroupBinding.textViewQuotes.setVisibility(View.GONE);
                holder.mRowTaskGroupBinding.textTaskStatus.setText(context.getString(R.string.label_cancelled));
                holder.mRowTaskGroupBinding.textTaskApprovedQuote.setVisibility(View.GONE);
            } else if (Utility.TASK_STATUS.ELAPSED.equalsIgnoreCase(model.taskStatus)) {
               /* holder.mRowTaskGroupBinding.textName.setTextColor(ContextCompat.getColor(context, R.color.red));
                holder.mRowTaskGroupBinding.textName.setText(context.getString(R.string.msg_task_cancelled_title));*/
                holder.mRowTaskGroupBinding.lnTaskStatusWithQuote.setVisibility(View.VISIBLE);
                holder.mRowTaskGroupBinding.textViewQuotes.setVisibility(View.GONE);
                holder.mRowTaskGroupBinding.textTaskStatus.setText(context.getString(R.string.label_lapsed));
                holder.mRowTaskGroupBinding.textTaskApprovedQuote.setVisibility(View.GONE);
            } else if (Utility.TASK_STATUS.DISPUTED.equalsIgnoreCase(model.taskStatus)) {
            /*    holder.mRowTaskGroupBinding.textName.setTextColor(ContextCompat.getColor(context, R.color.red));
                holder.mRowTaskGroupBinding.textName.setText(context.getString(R.string.msg_task_disputed_title));*/
                holder.mRowTaskGroupBinding.lnTaskStatusWithQuote.setVisibility(View.VISIBLE);
                holder.mRowTaskGroupBinding.textViewQuotes.setVisibility(View.GONE);
                holder.mRowTaskGroupBinding.textTaskStatus.setText(context.getString(R.string.label_disputed));
                holder.mRowTaskGroupBinding.textTaskApprovedQuote.setVisibility(View.GONE);
            } else {
                /*holder.mRowTaskGroupBinding.textName.setTextColor(ContextCompat.getColor(context, R.color.grey_varient_2)); //initaly black
                holder.mRowTaskGroupBinding.textName.setText(context.getString(R.string.label_select_service_provider));*/
                holder.mRowTaskGroupBinding.textViewQuotes.setVisibility(View.VISIBLE);
                holder.mRowTaskGroupBinding.lnTaskStatusWithQuote.setVisibility(View.GONE);
//                holder.mRowTaskGroupBinding.textTaskStatus.setText(context.getString(R.string.msg_task_cancelled_title));
            }

            //Checking if it is past task fragment then disable swipe feature else enable it
            if (whichFrag == TaskFragment.TAB_PAST_TASK) {
                holder.mRowTaskGroupBinding.swipeLayout.setSwipeEnabled(false);
            } else {
                holder.mRowTaskGroupBinding.swipeLayout.setSwipeEnabled(true);
            }

            holder.mRowTaskGroupBinding.swipeLayout.getSurfaceView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onTaskRowFragListItemClicked(whichFrag, model);
                }
            });

            // Click Event of ViewQuote
            holder.mRowTaskGroupBinding.textViewQuotes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (model.providerCount.equals("0")) {
                        // Return as click event should not be handled.
                        return;
                    }
                    if (listener != null)
                        listener.onViewQuotesClick(whichFrag, model);
                }
            });

            //So swipe adapter (lib method) close any previous opened swipe menu when current swipe is done.
            mItemManger.bindView(holder.itemView, position);
        } else {

            //======Individual item(when sp is selected for task)=====

            superStartDateTimeCalendar.setTimeZone(SuperCalendar.SuperTimeZone.GMT.GMT);
            superStartDateTimeCalendar.setTimeInMillis(Long.parseLong(model.taskStartdate));
            superStartDateTimeCalendar.setLocaleTimeZone();

            /*holder.mRowTaskBinding.textDate.setText(superStartDateTimeCalendar.format(Utility.DATE_FORMAT_DD_MMM));
            holder.mRowTaskBinding.textTime.setText(superStartDateTimeCalendar.format(Utility.DATE_FORMAT_HH_MM_AM));*/
            String date_time = holder.mView.getContext().getString(R.string.format_date_time
                    , superStartDateTimeCalendar.format(Utility.DATE_FORMAT_DD_MMM)
                    , superStartDateTimeCalendar.format(Utility.DATE_FORMAT_HH_MM_AM));
            holder.mRowTaskBinding.textDateTime.setText(date_time);

            holder.mRowTaskBinding.textDesc.setText(model.taskDesc);
//            holder.mRowTaskBinding.textCategoryName.setText(model.categoryName);
            //this is for marquee
//            holder.mRowTaskBinding.textCategoryName.setSingleLine(true);
//            holder.mRowTaskBinding.textCategoryName.setSelected(true);
//            holder.mRowTaskBinding.textCategoryName.setAllCaps(false);

            Utility.showCircularImageView(holder.mRowTaskBinding.imgProfile.getContext(), TAG, holder.mRowTaskBinding.imgProfile, model.selectedProvider.profileUrl, Utility.DEFAULT_PROFILE_SRC);
            holder.mRowTaskBinding.textProviderName.setText(model.selectedProvider.userName);
            if (!TextUtils.isEmpty(model.subCategoryName))
                holder.mRowTaskBinding.textSubCategoryName.setText(model.subCategoryName);
            holder.mRowTaskBinding.imgProfile.setVisibility(View.VISIBLE);
            /*holder.mRowTaskBinding.textExpectedTime.setText(model.selectedProvider.distance);
            holder.mRowTaskBinding.textPrice.setText(holder.mRowTaskBinding.imgProfile.getContext().getString(R.string.ruppe_symbol_x_space, Utility.getActualPrice(model.taskPaidAmount, model.selectedProvider.quotePrice)));*/

            if (Utility.BOOLEAN.YES.equalsIgnoreCase(model.selectedProvider.isVerified)) {
                holder.mRowTaskBinding.textVerified.setVisibility(View.VISIBLE);
                holder.mRowTaskBinding.textVerified.setText(context.getString(R.string.label_verified).toLowerCase());
            } else {
                holder.mRowTaskBinding.textVerified.setVisibility(View.GONE);
            }

            // Show Rating
            Utility.showRating(model.selectedProvider.rating, holder.mRowTaskBinding.ratingBar);

            /*holder.mRowTaskBinding.imgFav.setSelected(Utility.BOOLEAN.YES.equalsIgnoreCase(model.selectedProvider.isFavourite));
            holder.mRowTaskBinding.textTotalJobs.setVisibility(View.VISIBLE);
            holder.mRowTaskBinding.textTotalJobs.setText(Utility.getJobs(context, model.selectedProvider.jobsCount));*/
            holder.mRowTaskBinding.textDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Utility.TASK_STATUS.PROCESSING.equalsIgnoreCase(model.taskStatus) || Utility.TASK_STATUS.COMPLETION_REQUEST.equalsIgnoreCase(model.taskStatus)) {
                        Utility.showToast(context, context.getString(R.string.msg_processing_task_cancelled));
                        holder.mRowTaskBinding.swipeLayout.close(true);
                    } else {
                        listener.onTaskDelete(whichFrag, model, holder.mRowTaskBinding);
                    }
                }
            });
            holder.mRowTaskBinding.textReschedule.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Utility.TASK_STATUS.PROCESSING.equalsIgnoreCase(model.taskStatus)
                            || Utility.TASK_STATUS.COMPLETION_REQUEST.equalsIgnoreCase(model.taskStatus)
                            || Utility.TASK_STATUS.ADDITIONAL_PAYMENT_REQUESTED.equalsIgnoreCase(model.taskStatus)) {
                        Utility.showToast(context, context.getString(R.string.msg_processing_task_reschduled));
                        holder.mRowTaskBinding.swipeLayout.close(true);
                    } else {
                        listener.onTaskReschedule(whichFrag, model, holder.mRowTaskBinding);
                    }
                   /* if (listener != null) {
                        listener.onTaskReschedule(whichFrag, model, holder.mRowTaskBinding);
                    }*/
                }
            });

            /*holder.mRowTaskBinding.imgFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.mRowTaskBinding.imgFav.setSelected(!holder.mRowTaskBinding.imgFav.isSelected());
                    model.selectedProvider.isFavourite = holder.mRowTaskBinding.imgFav.isSelected() ? Utility.BOOLEAN.YES : Utility.BOOLEAN.NO;

                    //To update other fields for same selectedProvider
                    updateFavStatus(model.selectedProvider.providerId, model.selectedProvider.isFavourite);

                    if (listener != null) {
                        listener.onFavClicked(model, holder.mRowTaskBinding.imgFav.isSelected(), holder.getAdapterPosition());
                    }
                }
            });*/

            /*holder.mRowTaskBinding.imgCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onCallClicked(model);
                    }
                }
            });*/

            /*
            * Added by @sanjay
            * 24 feb 2017
            * */
            /*holder.mRowTaskBinding.imgChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TaskChatModel taskChatModel = new TaskChatModel();
                    taskChatModel.categoryName = model.categoryName;
                    taskChatModel.taskDesc = model.taskDesc;
                    taskChatModel.taskId = model.taskId;
                    taskChatModel.receiverId = FirebaseUtils.getPrefixSPId(model.selectedProvider.providerId);
                    taskChatModel.participantName = model.selectedProvider.userName;
                    taskChatModel.participantPhotoUrl = model.selectedProvider.profileUrl;
                    ChatActivity.newInstance(context, taskChatModel);
                }
            });*/

            holder.mRowTaskBinding.swipeLayout.setClickToClose(true);
            if (whichFrag == TaskFragment.TAB_PENDING_TASK) {
                holder.mRowTaskBinding.swipeLayout.setSwipeEnabled(true);
//                holder.mRowTaskBinding.textExpectedTime.setVisibility(View.VISIBLE);
//                holder.mRowTaskBinding.textRate.setVisibility(View.GONE);
//                holder.mRowTaskBinding.imgChat.setVisibility(View.VISIBLE);
//                holder.mRowTaskBinding.imgCall.setVisibility(View.VISIBLE);
//                holder.mRowTaskBinding.textStatus.setVisibility(View.GONE);
                holder.mRowTaskBinding.lnTaskStatusWithQuote.setVisibility(View.GONE);
                holder.mRowTaskBinding.textViewTask.setVisibility(View.VISIBLE);
            } else if (whichFrag == TaskFragment.TAB_PAST_TASK) {
                holder.mRowTaskBinding.swipeLayout.setSwipeEnabled(false);
//                holder.mRowTaskBinding.textExpectedTime.setVisibility(View.GONE);

               /* if (Utility.BOOLEAN.YES.equalsIgnoreCase(model.ratingDone)) {
                    holder.mRowTaskBinding.textRate.setVisibility(View.INVISIBLE);
                } else {
                    holder.mRowTaskBinding.textRate.setVisibility(View.VISIBLE);
                }*/

                /*holder.mRowTaskBinding.imgChat.setVisibility(View.GONE);
                holder.mRowTaskBinding.imgCall.setVisibility(View.GONE);
                holder.mRowTaskBinding.textStatus.setVisibility(View.VISIBLE);*/

                //Changing text of "paid" or "quote" depends on task payment status
                /*if (Utility.BOOLEAN.YES.equalsIgnoreCase(model.taskPaymentStatus) && !TextUtils.isEmpty(model.selectedProvider.quotePrice)) {
                    holder.mRowTaskBinding.textLabelPaid.setText(context.getString(R.string.label_paid));
                } else if (!TextUtils.isEmpty(model.selectedProvider.quotePrice)) {
                    holder.mRowTaskBinding.textLabelPaid.setText(context.getString(R.string.label_quote));
                }*/

                holder.mRowTaskBinding.lnTaskStatusWithQuote.setVisibility(View.VISIBLE);
                holder.mRowTaskBinding.textViewTask.setVisibility(View.GONE);

                holder.mRowTaskBinding.textTaskApprovedQuote.setText(holder.mRowTaskBinding.imgProfile.getContext().getString(R.string.ruppe_symbol_x_space, Utility.getActualPrice(model.taskPaidAmount, model.selectedProvider.quotePrice)));
                if (Utility.TASK_STATUS.CANCELLED_CUSTOMER.equalsIgnoreCase(model.taskStatus)) {
                    holder.mRowTaskBinding.textTaskStatus.setText(context.getString(R.string.label_cancelled));
//                    holder.mRowTaskBinding.textStatus.setTextColor(ContextCompat.getColor(context, R.color.red));
                } else if (Utility.TASK_STATUS.RESCHEDULE_REQUEST_REJECTED.equalsIgnoreCase(model.taskStatus)) {

                    holder.mRowTaskBinding.textTaskStatus.setText(context.getString(R.string.label_reschedule_rejected));
//                    holder.mRowTaskBinding.textStatus.setTextColor(ContextCompat.getColor(context, R.color.red));
//                    holder.mRowTaskBinding.textStatus.setSelected(true);
                } else if (Utility.TASK_STATUS.CANCELLED_SP.equalsIgnoreCase(model.taskStatus)) {
                    holder.mRowTaskBinding.textTaskStatus.setText(context.getString(R.string.label_cancelled));
//                    holder.mRowTaskBinding.textStatus.setTextColor(ContextCompat.getColor(context, R.color.red));
                } else if (Utility.TASK_STATUS.DISPUTED.equalsIgnoreCase(model.taskStatus)) {
                    holder.mRowTaskBinding.textTaskStatus.setText(context.getString(R.string.label_disputed));
//                    holder.mRowTaskBinding.textStatus.setTextColor(ContextCompat.getColor(context, R.color.red));
                } else if (Utility.TASK_STATUS.COMPLETION_CONFIRM.equalsIgnoreCase(model.taskStatus)) {
//                    holder.mRowTaskBinding.textLabelPaid.setText(context.getString(R.string.label_paid));
                    holder.mRowTaskBinding.textTaskStatus.setText(context.getString(R.string.label_completed));
//                    holder.mRowTaskBinding.textStatus.setTextColor(ContextCompat.getColor(context, R.color.black));
                } else {
                    holder.mRowTaskBinding.textTaskStatus.setText(context.getString(R.string.label_completed));
//                    holder.mRowTaskBinding.textStatus.setTextColor(ContextCompat.getColor(context, R.color.black));
                }

                /*holder.mRowTaskBinding.textRate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (listener != null) {
                            listener.onRateClick(whichFrag, model, holder.mRowTaskBinding);
                        }
                    }
                });*/
            }

            holder.mRowTaskBinding.swipeLayout.getSurfaceView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onTaskRowFragListItemClicked(whichFrag, model);
                }
            });
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

        public ViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            if (binding instanceof RowTaskBinding) {
                mRowTaskBinding = (RowTaskBinding) binding;
            } else {
                mRowTaskGroupBinding = (RowTaskGroupBinding) binding;
            }
        }
    }
}
