package com.cheep.adapter;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cheep.R;
import com.cheep.activity.TaskSummaryForMultiCatActivity;
import com.cheep.activity.ZoomImageActivity;
import com.cheep.databinding.RowChatHeaderBinding;
import com.cheep.databinding.RowChatReceiverMediaBinding;
import com.cheep.databinding.RowChatReceiverMessageBinding;
import com.cheep.databinding.RowChatReceiverMoneyBinding;
import com.cheep.databinding.RowChatSenderMediaBinding;
import com.cheep.databinding.RowChatSenderMessageBinding;
import com.cheep.databinding.RowChatSenderMoneyBinding;
import com.cheep.databinding.RowFooterProgressBinding;
import com.cheep.firebase.DateUtils;
import com.cheep.firebase.FirebaseUtils;
import com.cheep.firebase.model.TaskChatMessageModel;
import com.cheep.utils.GlideUtility;
import com.cheep.utils.PreferenceUtility;
import com.cheep.utils.SharedElementTransitionHelper;
import com.cheep.utils.Utility;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by sanjay on 21/10/16.
 */
public class ChatMessageRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_DATE_HEADER = 0;
    private final int VIEW_TYPE_SENDER_MESSAGE = 1;
    private final int VIEW_TYPE_RECEIVER_MESSAGE = 2;
    private final int VIEW_TYPE_SENDER_MEDIA = 3;
    private final int VIEW_TYPE_RECEIVER_MEDIA = 4;
    private final int VIEW_TYPE_SENDER_MONEY = 5;
    private final int VIEW_TYPE_RECEIVER_MONEY = 6;
    private final int VIEW_TYPE_PROGRESS = 7;

    /*
    * This is a test senderId
    * */
    private String senderId = "";

    ArrayList<TaskChatMessageModel> mList = new ArrayList<>();
    private Activity mContext;

    private Calendar calendar;

    public ChatMessageRecyclerViewAdapter(Activity mActivity) {
        this.mContext = mActivity;
        this.senderId = FirebaseUtils.getPrefixUserId(PreferenceUtility.getInstance(mContext).getUserDetails().userID);
        this.calendar = Calendar.getInstance();
    }

    public void add(TaskChatMessageModel taskChatMessageModel) {
        this.mList.add(taskChatMessageModel);
        notifyDataSetChanged();
    }

    /**
     * Used to add new message on the top of list
     *
     * @param messages pass the new message object
     */
    public void appendNewMessageOnTop(List<TaskChatMessageModel> messages) {
        if (messages != null && messages.size() > 0) {
            for (int i = 0; i < messages.size(); i++) {
                TaskChatMessageModel message = messages.get(i);
                if (message != null) {
                    Boolean isnewDate = false;
                    if (mList.isEmpty()) {
                        isnewDate = true;
                    } else {
                        TaskChatMessageModel preMsg = mList.get(0);
                        if (!DateUtils.isEqual(preMsg.timestamp, message.timestamp)) {
                            isnewDate = true;
                        } else if (preMsg.messageType.equalsIgnoreCase(Utility.CHAT_TYPE_DATE)) {
                            mList.remove(preMsg);
                            isnewDate = true;
                        } else {
                            mList.add(0, message);
                        }
                    }
                    if (isnewDate) {
                        mList.add(0, message);
                        TaskChatMessageModel hederMsg = new TaskChatMessageModel();
                        hederMsg.messageType = Utility.CHAT_TYPE_DATE;
                        hederMsg.timestamp = message.timestamp;
                        mList.add(0, hederMsg);
                    }
                }
            }
            notifyDataSetChanged();
        }
    }

    /**
     * append new message on the last of list
     *
     * @param message pass the new message object
     */
    public void appendNewMessage(TaskChatMessageModel message) {
        if (message != null) {
            if (mList.size() == 0) {
                TaskChatMessageModel msg = new TaskChatMessageModel();
                msg.messageType = Utility.CHAT_TYPE_DATE;
                msg.timestamp = message.timestamp;
                mList.add(msg);
            } else {
                TaskChatMessageModel preMsg = mList.get(mList.size() - 1);
                if (!DateUtils.isEqual(preMsg.timestamp, message.timestamp)) {
                    TaskChatMessageModel hederMsg = new TaskChatMessageModel();
                    hederMsg.messageType = Utility.CHAT_TYPE_DATE;
                    hederMsg.timestamp = message.timestamp;
                    mList.add(hederMsg);
                }
            }
            mList.add(message);
            notifyItemInserted(mList.size() - 1);
        }
    }

    /**
     * Used to update message
     *
     * @param message pass the updated message object
     */
    public void updateMessage(TaskChatMessageModel message) {
        if (message != null && !TextUtils.isEmpty(message.messageId)) {
            for (int i = 0; i < mList.size(); i++) {
                if (!TextUtils.isEmpty(mList.get(i).messageId) && mList.get(i).messageId.equalsIgnoreCase(message.messageId)) {
                    mList.set(i, message);
                    notifyItemChanged(i);
                    break;
                }
            }
        }
    }

    /**
     * Used to delete message
     */
    public void deleteMessage(TaskChatMessageModel message) {
        if (message != null && !TextUtils.isEmpty(message.messageId)) {
            for (int i = 0; i < mList.size(); i++) {
                if (!TextUtils.isEmpty(mList.get(i).messageId) && mList.get(i).messageId.equalsIgnoreCase(message.messageId)) {
                    mList.remove(i);
                    notifyItemRemoved(i);
                    break;
                }
            }
        }
    }

    /**
     * Used to clear message list
     */
    public void clearAll() {
        mList.clear();
        notifyDataSetChanged();
    }

    /**
     * Used to get first message timestamp from messages
     *
     * @return
     */
    public Long getFirstTimestamp() {
        if (mList.size() > 0)
            return mList.get(0).timestamp;
        else
            return 0l;
    }

    /**
     * Used to get last message timestamp from messages
     *
     * @return
     */
    public Long getLastTimestamp() {
        if (mList.size() > 0)
            return mList.get(mList.size() - 1).timestamp;
        else
            return 0l;
    }

    /**
     * used to show progressbar
     */
    public void showProgressBar() {
        this.mList.add(0, null);
        notifyItemInserted(0);
    }

    /**
     * Used to hide progressbar
     */
    public void hideProgressBar() {
        if (this.mList.size() > 0) {
            TaskChatMessageModel taskChatMessageModel = this.mList.get(0);
            if (taskChatMessageModel == null) {
                this.mList.remove(0);
                notifyItemRemoved(0);
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType) {
            case VIEW_TYPE_PROGRESS:
                RowFooterProgressBinding rowFooterProgressBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_footer_progress, parent, false);
                viewHolder = new ViewProgressHolder(rowFooterProgressBinding);
                break;
            case VIEW_TYPE_DATE_HEADER:
                RowChatHeaderBinding rowChatHeaderBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_chat_header, parent, false);
                viewHolder = new ViewDateHolder(rowChatHeaderBinding);
                break;
            case VIEW_TYPE_SENDER_MESSAGE:
                RowChatSenderMessageBinding rowChatSenderMessageBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_chat_sender_message, parent, false);
                viewHolder = new ViewSenderMessageHolder(rowChatSenderMessageBinding);
                break;
            case VIEW_TYPE_RECEIVER_MESSAGE:
                RowChatReceiverMessageBinding rowChatReceiverMediaBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_chat_receiver_message, parent, false);
                viewHolder = new ViewReceiverMessageHolder(rowChatReceiverMediaBinding);
                break;
            case VIEW_TYPE_SENDER_MEDIA:
                RowChatSenderMediaBinding rowChatSenderMediaBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_chat_sender_media, parent, false);
                viewHolder = new ViewSenderMediaHolder(rowChatSenderMediaBinding);
                break;
            case VIEW_TYPE_RECEIVER_MEDIA:
                RowChatReceiverMediaBinding rowChatReceiverMediaBinding1 = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_chat_receiver_media, parent, false);
                viewHolder = new ViewReceiverMediaHolder(rowChatReceiverMediaBinding1);
                break;
            case VIEW_TYPE_SENDER_MONEY:
                RowChatSenderMoneyBinding rowChatSenderMoneyBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_chat_sender_money, parent, false);
                viewHolder = new ViewSenderMoneyHolder(rowChatSenderMoneyBinding);
                break;
            case VIEW_TYPE_RECEIVER_MONEY:
                RowChatReceiverMoneyBinding receiverMoneyBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_chat_receiver_money, parent, false);
                viewHolder = new ViewReceiverMoneyHolder(receiverMoneyBinding);
                break;
        }
        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        TaskChatMessageModel taskChatMessageModel = mList.get(position);
        if (taskChatMessageModel != null) {
            if (taskChatMessageModel.messageType == null) {
                return VIEW_TYPE_PROGRESS;
            }

            if (taskChatMessageModel.messageType.equalsIgnoreCase(Utility.CHAT_TYPE_DATE)) {
                return VIEW_TYPE_DATE_HEADER;
            } else if (taskChatMessageModel.messageType.equalsIgnoreCase(Utility.CHAT_TYPE_MESSAGE)) {
                if (taskChatMessageModel.senderId.equalsIgnoreCase(senderId)) {
                    return VIEW_TYPE_SENDER_MESSAGE;
                } else {
                    return VIEW_TYPE_RECEIVER_MESSAGE;
                }
            } else if (taskChatMessageModel.messageType.equalsIgnoreCase(Utility.CHAT_TYPE_MEDIA)) {
                if (taskChatMessageModel.senderId.equalsIgnoreCase(senderId)) {
                    return VIEW_TYPE_SENDER_MEDIA;
                } else {
                    return VIEW_TYPE_RECEIVER_MEDIA;
                }
            } else if (taskChatMessageModel.messageType.equalsIgnoreCase(Utility.CHAT_TYPE_MONEY)) {
                if (taskChatMessageModel.senderId.equalsIgnoreCase(senderId)) {
                    return VIEW_TYPE_SENDER_MONEY;
                } else {
                    return VIEW_TYPE_RECEIVER_MONEY;
                }
            }
        } else {
            return VIEW_TYPE_PROGRESS;
        }
        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final TaskChatMessageModel model = mList.get(holder.getAdapterPosition());

        if (holder instanceof ViewDateHolder) {
            ViewDateHolder viewHolder = (ViewDateHolder) holder;
            if (DateUtils.isEqual(calendar.getTimeInMillis(), mList.get(position).timestamp)) {
                viewHolder.mRowChatHeaderBinding.tvHeader.setText(mContext.getString(R.string.label_today));
            } else if (DateUtils.isYesterdayDate(mList.get(position).timestamp)) {
                viewHolder.mRowChatHeaderBinding.tvHeader.setText(mContext.getString(R.string.label_yesterday));
            } else {
                viewHolder.mRowChatHeaderBinding.tvHeader.setText(DateUtils.getFormatedDate(mList.get(position).timestamp, DateUtils.DATE_FORMATE_CHAT_HEADER));
            }
        } else if (holder instanceof ViewSenderMessageHolder) {
            ViewSenderMessageHolder viewHolder = (ViewSenderMessageHolder) holder;
            viewHolder.mRowChatSenderMessageBinding.textMessage.setText(model.message);
            viewHolder.mRowChatSenderMessageBinding.textTime.setText(DateUtils.getFormatedDate(model.timestamp, DateUtils.DATE_FORMATE_CHAT_HH_MM_AA));
        } else if (holder instanceof ViewReceiverMessageHolder) {
            ViewReceiverMessageHolder viewHolder = (ViewReceiverMessageHolder) holder;
            viewHolder.mRowChatReceiverMessageBinding.textMessage.setText(model.message);
            viewHolder.mRowChatReceiverMessageBinding.textTime.setText(DateUtils.getFormatedDate(model.timestamp, DateUtils.DATE_FORMATE_CHAT_HH_MM_AA));
        } else if (holder instanceof ViewSenderMediaHolder) {
            final ViewSenderMediaHolder viewHolder = (ViewSenderMediaHolder) holder;
            viewHolder.mRowChatSenderMediaBinding.progressBar.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(model.mediaThumbUrl)) {
                viewHolder.mRowChatSenderMediaBinding.progressBar.setVisibility(View.VISIBLE);
                GlideUtility.loadImageView(mContext, viewHolder.mRowChatSenderMediaBinding.imgMedia, model.mediaThumbUrl, 0, new RequestListener() {
                    @Override
                    public boolean onException(Exception e, Object obj, Target target, boolean isFirstResource) {
                        viewHolder.mRowChatSenderMediaBinding.progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Object resource, Object obj, Target target, boolean isFromMemoryCache, boolean isFirstResource) {
                        viewHolder.mRowChatSenderMediaBinding.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                });
            } else if (!TextUtils.isEmpty(model.mediaUrl)) {
                viewHolder.mRowChatSenderMediaBinding.progressBar.setVisibility(View.VISIBLE);
                GlideUtility.loadImageView(mContext, viewHolder.mRowChatSenderMediaBinding.imgMedia, model.mediaUrl, 0, new RequestListener() {
                    @Override
                    public boolean onException(Exception e, Object model, Target target, boolean isFirstResource) {
                        viewHolder.mRowChatSenderMediaBinding.progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Object resource, Object model, Target target, boolean isFromMemoryCache, boolean isFirstResource) {
                        viewHolder.mRowChatSenderMediaBinding.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                });
            } else if (!TextUtils.isEmpty(model.localMediaUrl)) {
                viewHolder.mRowChatSenderMediaBinding.progressBar.setVisibility(View.VISIBLE);
                GlideUtility.loadImageView(mContext, viewHolder.mRowChatSenderMediaBinding.imgMedia, model.localMediaUrl, 0);
            }

            viewHolder.mRowChatSenderMediaBinding.imgMedia.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!TextUtils.isEmpty(model.mediaUrl) || !TextUtils.isEmpty(model.mediaThumbUrl)) {
                        SharedElementTransitionHelper sharedElementTransitionHelper = new SharedElementTransitionHelper(mContext);
                        sharedElementTransitionHelper.put(viewHolder.mRowChatSenderMediaBinding.imgMedia, R.string.transition_image_view);
                        if (!TextUtils.isEmpty(model.mediaUrl)) {
                            ZoomImageActivity.newInstance(mContext, sharedElementTransitionHelper.getBundle(), model.mediaUrl);
                        } else {
                            ZoomImageActivity.newInstance(mContext, sharedElementTransitionHelper.getBundle(), model.mediaThumbUrl);
                        }
                    }
                }
            });

            viewHolder.mRowChatSenderMediaBinding.textTime.setText(DateUtils.getFormatedDate(model.timestamp, DateUtils.DATE_FORMATE_CHAT_HH_MM_AA));
        } else if (holder instanceof ViewReceiverMediaHolder) {
            final ViewReceiverMediaHolder viewHolder = (ViewReceiverMediaHolder) holder;
            viewHolder.mRowChatReceiverMediaBinding.progressBar.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(model.mediaThumbUrl)) {
                viewHolder.mRowChatReceiverMediaBinding.progressBar.setVisibility(View.VISIBLE);
                GlideUtility.loadImageView(mContext, viewHolder.mRowChatReceiverMediaBinding.imgMedia, model.mediaThumbUrl, 0, new RequestListener() {
                    @Override
                    public boolean onException(Exception e, Object obj, Target target, boolean isFirstResource) {
                        viewHolder.mRowChatReceiverMediaBinding.progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Object resource, Object obj, Target target, boolean isFromMemoryCache, boolean isFirstResource) {
                        viewHolder.mRowChatReceiverMediaBinding.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                });
            } else if (!TextUtils.isEmpty(model.mediaUrl)) {
                viewHolder.mRowChatReceiverMediaBinding.progressBar.setVisibility(View.VISIBLE);
                GlideUtility.loadImageView(mContext, viewHolder.mRowChatReceiverMediaBinding.imgMedia, model.mediaUrl, 0, new RequestListener() {
                    @Override
                    public boolean onException(Exception e, Object model, Target target, boolean isFirstResource) {
                        viewHolder.mRowChatReceiverMediaBinding.progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Object resource, Object model, Target target, boolean isFromMemoryCache, boolean isFirstResource) {
                        viewHolder.mRowChatReceiverMediaBinding.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                });
            }

            viewHolder.mRowChatReceiverMediaBinding.imgMedia.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!TextUtils.isEmpty(model.mediaUrl) || !TextUtils.isEmpty(model.mediaThumbUrl)) {
                        SharedElementTransitionHelper sharedElementTransitionHelper = new SharedElementTransitionHelper(mContext);
                        sharedElementTransitionHelper.put(viewHolder.mRowChatReceiverMediaBinding.imgMedia, R.string.transition_image_view);
                        if (!TextUtils.isEmpty(model.mediaUrl)) {
                            ZoomImageActivity.newInstance(mContext, sharedElementTransitionHelper.getBundle(), model.mediaUrl);
                        } else {
                            ZoomImageActivity.newInstance(mContext, sharedElementTransitionHelper.getBundle(), model.mediaThumbUrl);
                        }
                    }
                }
            });

            viewHolder.mRowChatReceiverMediaBinding.textTime.setText(DateUtils.getFormatedDate(model.timestamp, DateUtils.DATE_FORMATE_CHAT_HH_MM_AA));
        } else if (holder instanceof ViewSenderMoneyHolder) {
            ViewSenderMoneyHolder viewHolder = (ViewSenderMoneyHolder) holder;
            viewHolder.mRowChatSenderMoneyBinding.textMoney.setText(mContext.getString(R.string.label_formatted_quote_amount,String.valueOf(model.quoteAmount)));
            viewHolder.mRowChatSenderMoneyBinding.textTime.setText(DateUtils.getFormatedDate(model.timestamp, DateUtils.DATE_FORMATE_CHAT_HH_MM_AA));
        } else if (holder instanceof ViewReceiverMoneyHolder) {
            ViewReceiverMoneyHolder viewHolder = (ViewReceiverMoneyHolder) holder;
            viewHolder.mRowChatReceiverMoneyBinding.textPayNow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   /* TaskDetailModel taskDetailModel = new TaskDetailModel();
                    taskDetailModel.taskId = FirebaseUtils.removePrefixTaskId(model.taskId);

                    ProviderModel providerModel = new ProviderModel();
                    providerModel.providerId = FirebaseUtils.removePrefixSpId(model.senderId);

                    JobSummaryActivity.newInstance(mContext, taskDetailModel, providerModel);*/
                    TaskSummaryForMultiCatActivity.getInstance(mContext, FirebaseUtils.removePrefixTaskId(model.taskId));
                }
            });
            viewHolder.mRowChatReceiverMoneyBinding.textMoney.setText(mContext.getString(R.string.label_formatted_quote_amount,String.valueOf(model.quoteAmount)));
            viewHolder.mRowChatReceiverMoneyBinding.textTime.setText(DateUtils.getFormatedDate(model.timestamp, DateUtils.DATE_FORMATE_CHAT_HH_MM_AA));
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    private class ViewProgressHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final RowFooterProgressBinding rowFooterProgressBinding;

        public ViewProgressHolder(RowFooterProgressBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            rowFooterProgressBinding = binding;
        }
    }

    private class ViewDateHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final RowChatHeaderBinding mRowChatHeaderBinding;

        public ViewDateHolder(RowChatHeaderBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            mRowChatHeaderBinding = binding;
        }
    }

    private class ViewSenderMessageHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final RowChatSenderMessageBinding mRowChatSenderMessageBinding;

        public ViewSenderMessageHolder(RowChatSenderMessageBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            mRowChatSenderMessageBinding = binding;
        }
    }

    private class ViewReceiverMessageHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final RowChatReceiverMessageBinding mRowChatReceiverMessageBinding;

        public ViewReceiverMessageHolder(RowChatReceiverMessageBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            mRowChatReceiverMessageBinding = binding;
        }
    }

    private class ViewSenderMediaHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final RowChatSenderMediaBinding mRowChatSenderMediaBinding;

        public ViewSenderMediaHolder(RowChatSenderMediaBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            mRowChatSenderMediaBinding = binding;
        }
    }

    private class ViewReceiverMediaHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final RowChatReceiverMediaBinding mRowChatReceiverMediaBinding;

        public ViewReceiverMediaHolder(RowChatReceiverMediaBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            mRowChatReceiverMediaBinding = binding;
        }
    }

    private class ViewSenderMoneyHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final RowChatSenderMoneyBinding mRowChatSenderMoneyBinding;

        public ViewSenderMoneyHolder(RowChatSenderMoneyBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            mRowChatSenderMoneyBinding = binding;
        }
    }

    private class ViewReceiverMoneyHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final RowChatReceiverMoneyBinding mRowChatReceiverMoneyBinding;

        public ViewReceiverMoneyHolder(RowChatReceiverMoneyBinding binding) {
            super(binding.getRoot());
            mView = binding.getRoot();
            mRowChatReceiverMoneyBinding = binding;
        }
    }
}
